package com.concur.babel.spring.service

import com.concur.babel.processor.ServiceRequestDispatcher
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.event.ContextRefreshedEvent
import spock.lang.Specification
import spock.lang.Unroll

import static org.hamcrest.Matchers.*
import static spock.util.matcher.HamcrestSupport.that

class ComponentScanningServiceRegistrarConfigurerSpec extends Specification {

    def "Should require a basePackage" () {
        given:
        def configurer = new ComponentScanningServiceRegistrarConfigurer()

        when:
        configurer.setBasePackage(null)

        then:
        IllegalArgumentException e = thrown()
        that e.message, containsString("basePackage can not be NULL")
    }

    def "Should require a beanFactory"() {
        given:
        def configurer = new ComponentScanningServiceRegistrarConfigurer()

        when:
        configurer.setBeanFactory(null)

        then:
        IllegalArgumentException e = thrown()
        that e.message, containsString("beanFactory can not be NULL")
    }

    def "Should require a ClassPathScanningServiceRegistrar"() {
        when:
        new ComponentScanningServiceRegistrarConfigurer(null)

        then:
        IllegalArgumentException e = thrown()
        that e.message, containsString("registrar can not be NULL")
    }

    def "Should look up dispatcher by reference when reference is provided"() {
        given:
        def mockRegistrar = Mock(ComponentScanningServiceRegistrar)
        def mockBeanFactory = Mock(BeanFactory)
        def configurer = new ComponentScanningServiceRegistrarConfigurer(mockRegistrar)
        configurer.setBeanFactory(mockBeanFactory)
        configurer.setBasePackage("com.company")
        configurer.setDispatcherReference("srd")
        configurer.setFailWhenNoServiceInstanceFound(false)

        when:
        configurer.getDispatcher()

        then:
        1 * mockBeanFactory.getBean("srd")
    }

    def "Should throw RuntimException if a BeansException occurs getting the dispatcher by reference"() {
        given:
        def mockRegistrar = Mock(ComponentScanningServiceRegistrar)
        def mockBeanFactory = Mock(BeanFactory) {
            getBean("srd") >> {throw new NoSuchBeanDefinitionException("")}
        }
        def configurer = new ComponentScanningServiceRegistrarConfigurer(mockRegistrar)
        configurer.setBeanFactory(mockBeanFactory)
        configurer.setBasePackage("com.company")
        configurer.setDispatcherReference("srd")
        configurer.setFailWhenNoServiceInstanceFound(false)

        when:
        configurer.getDispatcher()

        then:
        RuntimeException e = thrown()
        that e.message, containsString("Could not locate ServiceRequestDispatcher bean")
        that e.cause, is(notNullValue())
    }

    @Unroll
    def "Should look up dispatcher by type when no reference is provided"() {
        given:
        def mockRegistrar = Mock(ComponentScanningServiceRegistrar)
        def mockBeanFactory = Mock(BeanFactory)
        def configurer = new ComponentScanningServiceRegistrarConfigurer(mockRegistrar)
        configurer.setBeanFactory(mockBeanFactory)
        configurer.setBasePackage("com.company")
        configurer.setFailWhenNoServiceInstanceFound(false)
        configurer.setDispatcherReference(reference)

        when:
        configurer.getDispatcher()

        then:
        1 * mockBeanFactory.getBean(ServiceRequestDispatcher.class)

        where:
        reference << [null, ""]
    }

    def "Should throw RuntimeException if a BeansException occurs getting the dispatcher by type"() {
        given:
        def mockRegistrar = Mock(ComponentScanningServiceRegistrar)
        def mockBeanFactory = Mock(BeanFactory) {
            getBean(ServiceRequestDispatcher.class) >> {throw new NoSuchBeanDefinitionException("")}
        }
        def configurer = new ComponentScanningServiceRegistrarConfigurer(mockRegistrar)
        configurer.setBeanFactory(mockBeanFactory)
        configurer.setBasePackage("com.company")
        configurer.setFailWhenNoServiceInstanceFound(false)

        when:
        configurer.getDispatcher()

        then:
        RuntimeException e = thrown()
        that e.message, containsString("Could not locate ServiceRequestDispatcher bean")
        that e.cause, is(notNullValue())
    }

    def "Should register babel service on ContextRefreshedEvent"() {
        given:
        def mockContextRefreshEvent = Mock(ContextRefreshedEvent)
        def mockRegistrar = Mock(ComponentScanningServiceRegistrar)
        def configurer = new ComponentScanningServiceRegistrarConfigurer(mockRegistrar)
        configurer.setBasePackage("com.company")

        when:
        configurer.onApplicationEvent(mockContextRefreshEvent)

        then:
        1 * mockRegistrar.registerBabelServices()
    }

    def "should create ComponentScanningServiceRegistrar when one is not provided"() {
        given:
        def mockContextRefreshEvent = Mock(ContextRefreshedEvent)
        def mockDispatcher = Mock(ServiceRequestDispatcher)
        def mockBeanFactory = Mock(BeanFactory) {
            getBean(ServiceRequestDispatcher.class) >> mockDispatcher
        }
        def configurer = new ComponentScanningServiceRegistrarConfigurer()
        configurer.setBasePackage("com.company")
        configurer.setBeanFactory(mockBeanFactory)

        when:
        configurer.onApplicationEvent(mockContextRefreshEvent)

        then:
        that configurer.componentScanningServiceRegistrar, is(notNullValue())
    }
}
