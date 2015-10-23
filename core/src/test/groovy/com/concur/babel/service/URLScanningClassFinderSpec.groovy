package com.concur.babel.service

import com.concur.babel.test.jarSource.ChildClass
import com.concur.babel.test.jarSource.ChildInterface
import com.concur.babel.test.jarSource.ParentClass
import com.concur.babel.test.jarSource.ParentInterface
import org.apache.bcel.classfile.JavaClass
import spock.lang.Shared
import spock.lang.Specification

import static org.hamcrest.Matchers.*
import static spock.util.matcher.HamcrestSupport.that

/*
    The test class for the URLScanningClassFinder object.
 */
class URLScanningClassFinderSpec extends Specification {

    @Shared mockJavaClass = Mock(JavaClass)
    @Shared mockClassLoader = Mock(ClassLoader)

    /*
        These specs are for the JavaClassInfo object tests...
     */
    def "should require javaClass and classLoader to create a new JavaClassInfo"() {
        when:
        new URLScanningClassFinder.JavaClassInfo(javaClass, classLoader)

        then:
        IllegalArgumentException e = thrown()
        that e.message, containsString("can not be NULL")

        where:
        [javaClass, classLoader] << [
                [null, mockClassLoader],
                [mockJavaClass, null]
        ]
    }

    def "should load class"() {
        given:
        def mockJavaClass = Mock(JavaClass) {
            getClassName() >> ParentClass.class.getName()
        }
        def classLoader = ParentClass.class.getClassLoader()
        def info = new URLScanningClassFinder.JavaClassInfo(mockJavaClass, classLoader)

        when:
        def clazz = info.load();

        then:
        that clazz, is(equalTo(ParentClass.class))
    }

    def "should throw RuntimeException if failure while loading the class occurs"() {
        given:
        def mockJavaClass = Mock(JavaClass) {
            getClassName() >> ParentClass.class.getName()
        }
        def mockClassLoader = Mock(ClassLoader) {
            loadClass(ParentClass.class.getName()) >> {throw new ClassNotFoundException()}
        }
        def info = new URLScanningClassFinder.JavaClassInfo(mockJavaClass, mockClassLoader)

        when:
        info.load();

        then:
        RuntimeException e = thrown()
        that e.message, containsString("Could not load class")
        that e.cause, is(notNullValue())
    }

    /*
        Below are the tests for the URLScanningClassFinder class
     */

    def "should set baseClassLoader if not provided"() {
        when:
        def finder = new URLScanningClassFinder("com.company")

        then:
        that finder.basePackage, is(equalTo("com.company"))
        that finder.baseClassLoader, is(notNullValue())
    }

    def "should scan resources only when needed"() {
        given:
        def fileUrl = new URL("file:/someProject/build/classes/main/")
        def urls = [fileUrl] as URL[]
        def mockClassLoader = Mock(URLClassLoader) {
            getURLs() >> urls
        }
        def finder = new URLScanningClassFinder("com.company", mockClassLoader)
        def uriCount1 = finder.scannedUris.size()

        when:
        finder.findImplementations(ParentInterface.class)

        then:
        that uriCount1, is(equalTo(0))
        that finder.scannedUris.size(), is(equalTo(1))

    }

    def "should scan resources for all classLoaders in the provided classLoader hierarchy"() {
        given:
        def url1 = new URL("file:/someProject/build/classes/main/")
        def urls1 = [url1] as URL[]

        def url2 = new URL("file:/someOtherProject/build/classes/main/")
        def urls2 = [url2] as URL[]

        def mockParentClassLoader = Mock(URLClassLoader)
        def mockClassLoader = Mock(URLClassLoader, constructorArgs: [urls2, mockParentClassLoader])

        def finder = new URLScanningClassFinder("com.company", mockClassLoader)

        when:
        finder.findImplementations(ParentInterface.class)

        then:
        1 * mockClassLoader.getURLs() >> urls1
        1 * mockParentClassLoader.getURLs() >> urls2
    }

    def "should not allow null when finding implementation classes"() {
        given:
        def finder = new URLScanningClassFinder("com.company", mockClassLoader)

        when:
        finder.findImplementations(null)

        then:
        IllegalArgumentException e = thrown()
        that e.message, containsString("interfaceClass can not be NULL")
    }

    def "should not be able to find an implementation of a non-interface class type"() {
        given:
        def finder = new URLScanningClassFinder("com.concur", mockClassLoader)

        when:
        finder.findImplementations(String.class)

        then:
        IllegalArgumentException e = thrown()
        that e.message, containsString("interfaceClass must be an interface")
    }

    def "should throw RuntimeException if an Exception occurs when finding an implementation"() {
        given:
        def mockJavaClass = Mock(JavaClass) {
            isInterface() >> false
            getAllInterfaces() >> {throw new ClassNotFoundException()}
        }
        def mockJavaClassInfo = Mock(URLScanningClassFinder.JavaClassInfo) {
            getJavaClass() >> mockJavaClass
        }
        def finder = new URLScanningClassFinder("com.concur", mockClassLoader)
        finder.classInfo.add(mockJavaClassInfo)

        when:
        finder.findImplementations(ParentInterface.class)

        then:
        RuntimeException e = thrown()
        that e.message, containsString("Could not find implementations for")
        that e.cause, is(notNullValue())
    }

    def "should find expected implementation classes"() {
        given:
        def finder = new URLScanningClassFinder("com.concur")

        when:
        def classes = finder.findImplementations(ChildInterface.class)

        then:
        that classes.size(), is(equalTo(1))
        classes.contains(ChildClass.class)

    }

    def "should not find classes that are outside of the basePackage"() {
        given:
        def finder = new URLScanningClassFinder("com.concur.babel.exception")

        when:
        def classes = finder.findImplementations(ParentInterface.class)

        then:
        that classes.size(), is(equalTo(0))
    }

    def "should throw Exception if URISytaxException occurs getting a URI from a URL"() {
        given:
        def badURL = new URL("file:/someProject/build/classes/main/[]")
        def urls = [badURL] as URL[]
        def mockClassLoader = Mock(URLClassLoader) {
            getURLs() >> urls
        }
        def finder = new URLScanningClassFinder("com.company", mockClassLoader)

        when:
        finder.findImplementations(ParentInterface.class)

        then:
        RuntimeException e = thrown()
        that e.message, containsString("Could not get URI from URL")
        that e.cause, is(notNullValue())

    }

    def "should only scan unique URI's once"() {
        given:
        def url1 = new URL("file:/someProject/build/classes/main/")
        def urls1 = [url1] as URL[]

        def mockParentClassLoader = Mock(URLClassLoader) {
            getURLs() >> urls1
        }
        def mockClassLoader = Mock(URLClassLoader, constructorArgs: [urls1, mockParentClassLoader]) {
            getURLs() >> urls1
        }

        def finder = new URLScanningClassFinder("com.company", mockClassLoader)

        when:
        finder.findImplementations(ParentInterface.class)

        then:
        that finder.scannedUris.size(), is(equalTo(1))

    }

    def "should not scan uri's that do not have a schema of file"() {
        given:
        def nonFileUrl = new URL("http://google.com")
        def fileUrl = new URL("file:/someProject/build/classes/main/")
        def urls = [nonFileUrl, fileUrl] as URL[]
        def mockClassLoader = Mock(URLClassLoader) {
            getURLs() >> urls
        }
        def finder = new URLScanningClassFinder("com.company", mockClassLoader)

        when:
        finder.findImplementations(ParentInterface.class)

        then:
        that finder.scannedUris.size(), is(equalTo(1))
    }

    def "should find indirect implementation classes"() {
        given:
        def finder = new URLScanningClassFinder("com.concur")
        println("file --> ${new File(".").getCanonicalPath()}")

        when:
        def classes = finder.findImplementations(ParentInterface.class)

        then:
        that classes.size(),is(equalTo(2))
        classes.contains(ParentClass.class)
        classes.contains(ChildClass.class)
    }

}

