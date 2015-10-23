package com.concur.babel.service

import com.concur.babel.test.jarSource.ChildClass
import com.concur.babel.test.jarSource.ChildInterface
import com.concur.babel.test.jarSource.ParentClass
import com.concur.babel.test.jarSource.ParentInterface
import spock.lang.Shared
import spock.lang.Specification

import static org.hamcrest.Matchers.*
import static spock.util.matcher.HamcrestSupport.that

class URLScanningClassFinderIntegrationSpec extends Specification {

    @Shared TEST_UTILS_ROOT = "${pathPrefix()}testUtils"
    @Shared BUILD_ROOT = "${TEST_UTILS_ROOT}/build"
    @Shared CLASSES_ROOT = "${BUILD_ROOT}/classes/test"
    @Shared LIBS_ROOT = "${BUILD_ROOT}/libs"
    @Shared JAR_FILE = "Test.jar"
    @Shared SOURCES_ROOT = "${TEST_UTILS_ROOT}/src/main/java/com/concur/babel/test/jarSource"

    // Doing this because the working directory will be different if running from within your ide versus
    // running from command line.  It may be good to look for a better way of doing this...
    def pathPrefix() {
        def prefix = new File(".").getCanonicalPath().endsWith("core") ? "../" : ""
    }
    def setupSpec() {

        new File(CLASSES_ROOT).mkdirs()
        new File(LIBS_ROOT).mkdirs()

        def cmd = "javac -d ${CLASSES_ROOT}"

        new File(SOURCES_ROOT).eachFile { file ->
            if (file.path.endsWith(".java")) {
                cmd += " ${file.path} "
            }
        }

        def proc = cmd.execute() //compile the classes
        proc.waitFor()

        cmd = "jar cfv ${LIBS_ROOT}/${JAR_FILE} -C ${CLASSES_ROOT} ." //jar up the classes
        proc = cmd.execute()
        proc.waitFor()
    }

    def "should find implementations from a jar file"() {
        given:"a URL to a known JAR file"
        def jarUrl = new URL("file:${new File(".").getCanonicalPath()}/${LIBS_ROOT}/${JAR_FILE}")
        def urls = [jarUrl] as URL[]
        and:"a URLClassLoader for the URL"
        def testClassLoader = new TestUrlClassLoader(urls)

        and:"a URLScanningClassFinder for the class loader"
        def finder = new URLScanningClassFinder("com.concur", testClassLoader)

        when:"looking for implementations in the known JAR File"
        def classes = finder.findImplementations(ChildInterface.class)

        then:
        !classes.isEmpty()
    }

    def "should find indirect implementation classes from within a jar file"() {
        given:
        def jarUrl = new URL("file:${new File(".").getCanonicalPath()}/${LIBS_ROOT}/${JAR_FILE}")
        def urls = [jarUrl] as URL[]
        and:"a URLClassLoader for the URL"
        def testClassLoader = new TestUrlClassLoader(urls)

        def finder = new URLScanningClassFinder("com.concur", testClassLoader)

        when:
        def classes = finder.findImplementations(ParentInterface.class)

        then:
        that classes.size(),is(equalTo(2))
        classes.contains(ParentClass.class)
        classes.contains(ChildClass.class)
    }

    def cleanupSpec() {
        //Delete test artifacts
        new File(CLASSES_ROOT).deleteDir()
        new File("${LIBS_ROOT}/${JAR_FILE}").delete()
    }

    class TestUrlClassLoader extends URLClassLoader {
        TestUrlClassLoader(URL[] urls) {
            super(urls)
        }

        @Override
        InputStream getResourceAsStream(String name) {
            Thread.currentThread().getContextClassLoader().getResourceAsStream(name)
        }

        @Override
        Class<?> loadClass(String name) {
            Thread.currentThread().getContextClassLoader().loadClass(name)
        }
    }

}
