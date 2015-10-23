package com.concur.babel.service

import com.concur.babel.processor.ServiceRequestDispatcher
import com.concur.babel.test.service.ArgConstructorService
import com.concur.babel.test.service.ArgConstructorServiceImpl
import com.concur.babel.test.service.TweetService
import com.concur.babel.test.service.TweetServiceImpl
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static org.hamcrest.Matchers.*
import static spock.util.matcher.HamcrestSupport.that

class URLScanningServiceRegistrarSpec extends Specification {

    @Shared mockDispatcher = Mock(ServiceRequestDispatcher)
    @Shared mockClassFinder = Mock(URLScanningClassFinder)

    @Unroll
    def "should require a basePackage and a ServiceRequestDispatcher"() {
        when:
        new URLScanningServiceRegistrar(basePackage, dispatcher, false, mockClassFinder)

        then:
        IllegalArgumentException e = thrown()
        that e.message, containsString("can not be NULL")

        where:
        [basePackage, dispatcher] << [
                [null, mockDispatcher],
                ["com.company", null]
        ]
    }

    def "should initialize dependencies if not provided" () {
        when:
        def registrar = new URLScanningServiceRegistrar("com.company", mockDispatcher)

        then:
        that registrar.classFinder, is(notNullValue())
        that registrar.failWhenNoServiceInstanceFound, is(equalTo(false))
    }

    def "should use classFinder to get service definitions"() {
        given:
        def implementations = [TweetService.class] as Set
        def mockClassFinder = Mock(URLScanningClassFinder)
        def registrar = new URLScanningServiceRegistrar("com.company", mockDispatcher, false, mockClassFinder)

        when:
        def definitions = registrar.getServiceDefinitions()

        then:
        1 * mockClassFinder.findImplementations(BabelServiceDefinition.class) >> implementations
        that definitions.size(), is(equalTo(1))
        that definitions.get(0), is(instanceOf(TweetService.class))

    }

    def "should throw RuntimeException if ReflectiveOperationException is thrown when getting service definitions"() {
        given:
        def mockClassFinder = Mock(URLScanningClassFinder) {
            findImplementations(BabelServiceDefinition.class) >> {
                [ArgConstructorService.getClass()] as Set
            }
        }
        def registrar = new URLScanningServiceRegistrar("com.company", mockDispatcher, false, mockClassFinder)

        when:
        registrar.getServiceDefinitions()

        then:
        RuntimeException e = thrown()
        that e.message, containsString("Could not load BabelServiceDefinitions")
        that e.cause, is(notNullValue())

    }


    def "should not be able to get service instance for a null definition" () {
        given:
        def registrar = new URLScanningServiceRegistrar("com.company", mockDispatcher, false, mockClassFinder)

        when:
        registrar.getServiceInstance(null)

        then:
        IllegalArgumentException e = thrown()
        that e.message, containsString("babelServiceDefinition can not be NULL")

    }

    def "should return a BabelService when getting a service instance"() {
        given:
        def implementations = [TweetServiceImpl.class, TweetService.Client.class] as Set
        def mockFinder = Mock(URLScanningClassFinder) {
            findImplementations(TweetService.Iface.class) >> implementations
        }

        def mockServiceDefinition = Mock(BabelServiceDefinition) {
            getIfaceClass() >> TweetService.Iface.class
        }

        def registrar = new URLScanningServiceRegistrar("com.company", mockDispatcher, false, mockFinder)

        when:
        def service = registrar.getServiceInstance(mockServiceDefinition)

        then:
        that service, is(instanceOf(TweetServiceImpl.class))

    }

    def "should log a warning when no service instance is found and failWhenNoServiceInstanceFound is false"() {
        given:
        def implementations = [] as Set
        def mockFinder = Mock(URLScanningClassFinder) {
            findImplementations(TweetService.Iface.class) >> implementations
        }

        def mockServiceDefinition = Mock(BabelServiceDefinition) {
            getIfaceClass() >> TweetService.Iface.class
        }

        def registrar = new URLScanningServiceRegistrar("com.company", mockDispatcher, false, mockFinder)

        when:
        def service = registrar.getServiceInstance(mockServiceDefinition)

        then:
        that service, is(nullValue())

    }

    def "should throw an Exception when no service instance is found and failWhenNoServiceInstanceFound is true"() {
        given:
        def implementations = [] as Set
        def mockFinder = Mock(URLScanningClassFinder) {
            findImplementations(TweetService.Iface.class) >> implementations
        }

        def mockServiceDefinition = Mock(BabelServiceDefinition) {
            getIfaceClass() >> TweetService.Iface.class
        }

        def registrar = new URLScanningServiceRegistrar("com.company", mockDispatcher, true, mockFinder)

        when:
        registrar.getServiceInstance(mockServiceDefinition)

        then:
        RuntimeException e = thrown()
        that e.message, containsString("Could not find service instance for")

    }

    def "should throw RuntimeException if ReflectiveOperationException is thrown when getting service instance"() {
        given:
        def mockBabelServiceDefinition = Mock(BabelServiceDefinition) {
            getIfaceClass() >> ArgConstructorService.Iface.class
        }
        def mockClassFinder = Mock(URLScanningClassFinder) {
            findImplementations(mockBabelServiceDefinition.getIfaceClass()) >> {
                [ArgConstructorServiceImpl.class] as Set
            }
        }
        def registrar = new URLScanningServiceRegistrar("com.company", mockDispatcher, false, mockClassFinder)

        when:
        registrar.getServiceInstance(mockBabelServiceDefinition)

        then:
        RuntimeException e = thrown()
        that e.message, containsString("Could not get babel service instance for")
        that e.cause, is(notNullValue())
    }

    def "should filter out BaseClient implementations when getting service definitions"() {
        given:
        def mockBabelServiceDefinition = Mock(BabelServiceDefinition) {
            getIfaceClass() >> TweetService.Iface.class
        }
        def mockClassFinder = Mock(URLScanningClassFinder) {
            findImplementations(mockBabelServiceDefinition.getIfaceClass()) >> {
                [TweetService.Client.class] as Set
            }
        }
        def registrar = new URLScanningServiceRegistrar("com.company", mockDispatcher, false, mockClassFinder)

        when:
        def service = registrar.getServiceInstance(mockBabelServiceDefinition)

        then:
        that service, is(nullValue())

    }

}
