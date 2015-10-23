package com.concur.babel.service;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.ClassLoaderRepository;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.concur.babel.ArgValidator.notNull;
import static com.concur.babel.ArgValidator.preCondition;

/*
    The logic in this class to loop through classloader URL's and scan file system and jar files was
    taken (nearly copied) from the Google Guava ClassPath.Scanner class.  The reason that the library itself
    was not used is that it does not provide the necessary level of byte code parsing needed to determine if the class
    needs to be loaded by the class loader before actually loading it.  The apache commons BCEL library is being used to
    provide the necessary level of byte code parsing/access.

    With that said, the scanning code here has limitations that should be understood:
        It only scans classes in jar files or under a file system directory.
        Neither can it scan classes managed by custom class loaders that aren't URLClassLoader.

    Knowing the current way in which Concur builds/deploys applications, these limitations are deemed acceptable for
    non-Spring applications (The babel-spring library has it's own scanning implementation that does not utilize this
    component).

    If these limitations become problems, options are:
        1.  Manually register any Babel services without using the dynamic registration (the only current
            use of this component)
        2.  Use the babel-spring library and the dynamic service registration it provides.
        3.  Contribute a solution to this component.
 */


/**
 * URLScanningClassFinder scans all class flies under a given package for each URL that a ClassLoader has access to.
 * It uses the apache commons BCEL library to evaluate metadata about the classes.  Clients can then 'find' classes
 * that implement given interfaces. This way the class is only loaded if it meets the criteria.
 *
 * Using the URLScanningClassFinder by supplying a ClassLoader will walk the ClassLoader hierarchy and scan URL's for
 * classes for each of the ClassLoaders in the hierarchy.  If no ClassLoader is supplied, the default ClassLoader used
 * will be determined by the following order:
 * <ol>ClassLoader from the current Thread</ol>
 * <ol>ClassLoader that loaded this class</ol>
 * <ol>The System ClassLoader</ol>
 */
public class URLScanningClassFinder {

    protected final Logger logger = Logger.getLogger(URLScanningClassFinder.class.getName());

    private final String basePackage;
    private final ClassLoader baseClassLoader;
    private final List<JavaClassInfo> classInfo = new ArrayList<>();

    // Used to make sure we do not scan the same URI in case multiple class loaders have access to it.
    private final Set<URI> scannedUris = new HashSet<>();

    /**
     * Creates a new URLScanningClassFinder for the provided base package and class loader.
     *
     * @param basePackage the package to scan for classes in.
     * @param classLoader the ClassLoader to determine what resources (URL's) to scan.
     */
    public URLScanningClassFinder(String basePackage, ClassLoader classLoader) {
        notNull("basePackage", basePackage);
        this.basePackage = basePackage;
        this.baseClassLoader = classLoader != null ? classLoader : this.getDefaultClassLoader();
        notNull("baseClassLoader", this.baseClassLoader);
    }

    /**
     * Creates a new URLScanningClassFinder for the provided base package.  The default ClassLoader will be used.
     *
     * @param basePackage the package to scan for classes in.
     */
    public URLScanningClassFinder(String basePackage) {
        this(basePackage, null);
    }

    /**
     * Finds classes that implement the provided interface, directly or indirectly.
     *
     * @param interfaceClass the interface Class to find implementations for.
     * @param <T>            the type of the interface Class.
     * @return a Set of Classes that implement the provided interface Class.
     */
    public <T> Set<Class<T>> findImplementations(Class<T> interfaceClass) {
        notNull("interfaceClass", interfaceClass);
        preCondition(interfaceClass.isInterface(), "interfaceClass must be an interface");

        if (this.scannedUris.isEmpty()) {
            this.scan();
        }

        Set<Class<T>> implementations = new HashSet<>();
        try {
            for (JavaClassInfo info : this.classInfo) {
                if (info.getJavaClass().isInterface()) {
                    continue;
                }
                for (JavaClass iFace : info.getJavaClass().getAllInterfaces()) {
                    if (interfaceClass.getName().equals(iFace.getClassName())) {
                        implementations.add((Class<T>) info.load());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not find implementations for " + interfaceClass.getName(), e);
        }
        return implementations;
    }

    /*
        Scans the URL's that the provided ClassLoader (and all of its parents) has access to.
     */
    private void scan() {
        boolean logInfo =
                this.logger.getLevel() == null || this.logger.getLevel().intValue() <= Level.INFO.intValue();

        long start = 0;
        if (logInfo) {
            logger.info("Scanning basePackage: " + this.basePackage);
            start = System.currentTimeMillis();
        }

        for (ClassLoader classLoader : this.getAllClassLoaders(this.baseClassLoader)) {
            if (classLoader instanceof URLClassLoader) {
                URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
                ClassLoaderRepository classLoaderRepository = new ClassLoaderRepository(urlClassLoader);
                for (URL entry : urlClassLoader.getURLs()) {
                    URI uri;
                    try {
                        uri = entry.toURI();
                    } catch (URISyntaxException e) {
                        throw new RuntimeException("Could not get URI from URL " + entry.getPath(), e);
                    }
                    if (uri.getScheme().equals("file") && scannedUris.add(uri)) {
                        this.scanFrom(new File(uri), classLoaderRepository, classLoader);
                    }
                }
            }
        }
        if (logInfo) {
            logger.info("Scanning complete.  Duration: " + (System.currentTimeMillis() - start) + "ms");
        }
    }

    /*
        Recursively walk up class loader hierarchy to get them all.
     */
    private List<ClassLoader> getAllClassLoaders(ClassLoader classLoader) {
        List<ClassLoader> loaders = new ArrayList<>();
        ClassLoader parent = classLoader.getParent();
        if (parent != null) {
            loaders.addAll(this.getAllClassLoaders(parent));
        }
        loaders.add(classLoader);
        return loaders;
    }

    private void scanFrom(File file, ClassLoaderRepository repository, ClassLoader classLoader) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            scanDirectory(file, "", repository, classLoader);
        } else {
            scanJar(file, repository, classLoader);
        }
    }

    /*
        Recursively scans the file system for .class files in the provided base package, adding a JavaClassInfo object
        to a list that can be inspected later to find classes based on specified criteria.
     */
    private void scanDirectory(File directory, String packageName, ClassLoaderRepository repository, ClassLoader classLoader) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        String className;
        for (File file : files) {
            if (file.isDirectory()) {
                this.scanDirectory(file, packageName + file.getName() + ".", repository, classLoader);
            } else {
                if (packageName.startsWith(this.basePackage)) {
                    int extIndex = file.getName().lastIndexOf(".class");
                    if (extIndex > 0) {
                        className = packageName + file.getName().substring(0, extIndex);
                        try {
                            this.classInfo.add(new JavaClassInfo(repository.loadClass(className), classLoader));
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException("Could not get class info for " + className);
                        }
                    }
                }
            }
        }
    }

    /*
        Scans a jar for .class files in the provided base package, adding a JavaClassInfo object
        to a list that can be inspected later to find classes based on specified criteria.
    */
    private void scanJar(File file, ClassLoaderRepository repository, ClassLoader classLoader) {
        String basePath = this.basePackage.replace(".", File.separator);
        JarFile jarFile;
        try {
            jarFile = new JarFile(file);
        } catch (IOException e) {
            // Not a jar file
            return;
        }
        try {
            String className;
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || entry.getName().equals(JarFile.MANIFEST_NAME)) {
                    continue;
                }
                if (entry.getName().startsWith(basePath)) {
                    int extIndex = entry.getName().lastIndexOf(".class");
                    if (extIndex > 0) {
                        className = entry.getName().substring(0, extIndex).replace(File.separator, ".");
                        try {
                            this.classInfo.add(new JavaClassInfo(repository.loadClass(className), classLoader));
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException("Could not get class info for " + className, e);
                        }
                    }
                }
            }
        } finally {
            try {
                jarFile.close();
            } catch (IOException ignored) {
            }
        }
    }

    private ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Exception e) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = URLScanningClassFinder.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Exception e) {
                    throw new RuntimeException("Could not get default class loader.", e);
                }
            }
        }
        return cl;
    }

    /*
        Private class to encapsulate the JavaClass object provided by the apache commons BCEL library
        with the ClassLoader that can load the class if needed.
        This class is static so that we can create an instance for testing without having to create
        an instance of URLScanningClassFinder
     */
    private static class JavaClassInfo {
        private final JavaClass javaClass;
        private final ClassLoader classLoader;

        public JavaClassInfo(JavaClass javaClass, ClassLoader classLoader) {
            notNull("javaClass", javaClass);
            notNull("classLoader", classLoader);
            this.javaClass = javaClass;
            this.classLoader = classLoader;
        }

        public JavaClass getJavaClass() {
            return javaClass;
        }

        public Class<?> load() {
            try {
                return this.classLoader.loadClass(this.javaClass.getClassName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Could not load class " + this.javaClass.getClassName(), e);
            }
        }
    }

}
