package com.concur.babel.spring.service

import com.concur.babel.BabelService
import com.concur.babel.processor.ServiceRequestDispatcher
import com.concur.babel.service.BabelServiceDefinition
import com.concur.babel.test.service.ArgConstructorService
import com.concur.babel.test.service.EchoService
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AssignableTypeFilter
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static org.hamcrest.Matchers.*
import static spock.util.matcher.HamcrestSupport.that

class ComponentScanningServiceRegistrarSpec extends Specification {
    @Shared mockDispatcher = Mock(ServiceRequestDispatcher)
    @Shared mockBeanFactory = Mock(BeanFactory)
    @Shared mockScanner = Mock(ClassPathScanningCandidateComponentProvider)

    @Unroll
    def "Should require basePackage, dispatcher and beanFactory"() {
        when:
        new ComponentScanningServiceRegistrar(basePackage, dispatcher, beanFactory)

        then:
        IllegalArgumentException e = thrown()
        that e.message, containsString("can not be NULL")

        where:
        [basePackage, dispatcher, beanFactory] << [
                [null, mockDispatcher, mockBeanFactory],
                ["com.company", null, mockBeanFactory],
                ["com.company", mockDispatcher, null]
        ]
    }

    def "Should require basePackage, dispatcher, beanFactory and scanner"() {
        when:
        new ComponentScanningServiceRegistrar(basePackage, dispatcher, beanFactory, false, scanner)

        then:
        IllegalArgumentException e = thrown()
        that e.message, containsString("can not be NULL")

        where:
        [basePackage, dispatcher, beanFactory, scanner] << [
                [null, mockDispatcher, mockBeanFactory, mockScanner],
                ["com.company", null, mockBeanFactory, mockScanner],
                ["com.company", mockDispatcher, null, mockScanner],
                ["com.company", mockDispatcher, mockBeanFactory, null]
        ]
    }

    def "Should create and configure scanner on creation" () {
        when:
        def registrar = new ComponentScanningServiceRegistrar("com.company", mockDispatcher, mockBeanFactory)

        then:
        that registrar.scanner, is(notNullValue())
        that registrar.scanner.includeFilters.size(), is(equalTo(1))
        that registrar.scanner.excludeFilters.size(), is(equalTo(0))
        that registrar.scanner.includeFilters.get(0), isA(AssignableTypeFilter)
        that registrar.scanner.includeFilters.get(0).targetType, is(equalTo(BabelServiceDefinition.class))
    }

    def "Should configure the provided scanner on creation"() {
        given:
        def mockScanner = Mock(ClassPathScanningCandidateComponentProvider)

        when:
        new ComponentScanningServiceRegistrar("com.company", mockDispatcher, mockBeanFactory, false, mockScanner)

        then:
        1 * mockScanner.resetFilters(false)
        1 * mockScanner.addIncludeFilter(_ as AssignableTypeFilter)
    }

    def "Should require babel service definition to find service instance" () {
        given:
        def registrar = new ComponentScanningServiceRegistrar("com.company", mockDispatcher, mockBeanFactory)

        when:
        registrar.getServiceInstance(null)

        then:
        IllegalArgumentException e = thrown()
        that e.message, containsString("babelServiceDefinition can not be NULL")

    }

    def "Should throw RuntimeException if BeansException occurs when looking up service instance" () {
        given:
        def mockBabelService = Mock(BabelService)
        def mockServiceDefinition = Mock(BabelServiceDefinition) {
            getIfaceClass() >> mockBabelService.class
        }
        def mockBeanFactory = Mock(BeanFactory) {
            getBean(mockBabelService.class) >> {throw new NoSuchBeanDefinitionException("")}
        }
        def mockScanner = Mock(ClassPathScanningCandidateComponentProvider)
        def registrar = new ComponentScanningServiceRegistrar("com.company", mockDispatcher, mockBeanFactory, true, mockScanner)

        when:
        registrar.getServiceInstance(mockServiceDefinition)

        then:
        RuntimeException e = thrown()
        that e.message, containsString("Could not find service instance for ")
        that e.cause, is(notNullValue())

    }

    def "Should log warning if BeansException occurs when looking up service instance" () {
        given:
        def mockBabelService = Mock(BabelService)
        def mockServiceDefinition = Mock(BabelServiceDefinition) {
            getIfaceClass() >> mockBabelService.class
        }
        def mockBeanFactory = Mock(BeanFactory) {
            getBean(mockBabelService.class) >> {throw new NoSuchBeanDefinitionException("")}
        }
        def mockScanner = Mock(ClassPathScanningCandidateComponentProvider)
        def registrar = new ComponentScanningServiceRegistrar("com.company", mockDispatcher, mockBeanFactory, false, mockScanner)

        when:
        registrar.getServiceInstance(mockServiceDefinition)

        then:
        notThrown(RuntimeException)

    }

    def "Should look up service instance from bean factory"() {
        given:
        def mockBabelService = Mock(BabelService)
        def mockServiceDefinition = Mock(BabelServiceDefinition)
        def mockBeanFactory = Mock(BeanFactory)
        def mockScanner = Mock(ClassPathScanningCandidateComponentProvider)
        def registrar = new ComponentScanningServiceRegistrar("com.company", mockDispatcher, mockBeanFactory, true, mockScanner)

        when:
        registrar.getServiceInstance(mockServiceDefinition)

        then:
        1 * mockServiceDefinition.getIfaceClass() >>> mockBabelService.class
        1 * mockBeanFactory.getBean(mockBabelService.class)
    }

    def "Should create a list of BabelServiceDefinitions using the scanner"() {
        given:
        def mockBeanDefinition = Mock(BeanDefinition) {
            getBeanClassName() >> EchoService.class.getName()
        }
        def beanDefinitions = [] << mockBeanDefinition
        def mockScanner = Mock(ClassPathScanningCandidateComponentProvider) {
            findCandidateComponents("com.company") >> beanDefinitions
        }
        def registrar = new ComponentScanningServiceRegistrar("com.company", mockDispatcher, mockBeanFactory, true, mockScanner)

        when:
        def definitions = registrar.getServiceDefinitions()

        then:
        that definitions.size(), is(equalTo(1))
        that definitions.get(0), is(instanceOf(EchoService.class))


    }

    def "Should throw RuntimeException if a ReflectiveOperationException exception occurs"() {
        given:
        def mockBeanDefinition = Mock(BeanDefinition) {
            getBeanClassName() >> ArgConstructorService.class.getName()
        }
        def beanDefinitions = [] << mockBeanDefinition
        def mockScanner = Mock(ClassPathScanningCandidateComponentProvider) {
            findCandidateComponents("com.company") >> beanDefinitions
        }
        def registrar = new ComponentScanningServiceRegistrar("com.company", mockDispatcher, mockBeanFactory, true, mockScanner)

        when:
        registrar.getServiceDefinitions()

        then:
        RuntimeException e = thrown()
        that e.message, containsString("Could not add babel service definition for")
        that e.cause, is(notNullValue())
    }


}
