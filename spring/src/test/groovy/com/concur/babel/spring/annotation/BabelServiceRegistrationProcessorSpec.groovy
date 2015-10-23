package com.concur.babel.spring.annotation

import com.concur.babel.spring.service.ComponentScanningServiceRegistrarConfigurer
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.core.type.AnnotationMetadata
import spock.lang.Specification

import static org.hamcrest.Matchers.*
import static spock.util.matcher.HamcrestSupport.that

class BabelServiceRegistrationProcessorSpec extends Specification {

    def "should initialize BeanDefinitionBuilder on creation" () {
        when:
        def processor = new BabelServiceRegistrationProcessor()

        then:
        that processor.builder, is(notNullValue())
        that processor.builder.getBeanDefinition().getBeanClass(), is(equalTo(ComponentScanningServiceRegistrarConfigurer.class))

    }

    def "should register the ComponentScanningServiceRegistrarConfigurer"() {
        given: "Annotation metadata"
        def mockAnnotationAttributes = [:]
        mockAnnotationAttributes.put("basePackage", "com.company")
        mockAnnotationAttributes.put("dispatcherReference", "srdReference")
        mockAnnotationAttributes.put("failWhenNoServiceInstanceFound", false)
        def mockAnnotationMetadata = Mock(AnnotationMetadata) {
            getAnnotationAttributes(BabelServiceRegistration.class.getName()) >> mockAnnotationAttributes
        }

        and: "bean definition builder and registry"
        def mockBeanDefinitionBuilder = Mock(BeanDefinitionBuilder)
        def mockBeanDefinitionRegistry = Mock(BeanDefinitionRegistry)
        def processor = new BabelServiceRegistrationProcessor(mockBeanDefinitionBuilder)

        when: "calling registerBeanDefinitions"
        processor.registerBeanDefinitions(mockAnnotationMetadata, mockBeanDefinitionRegistry)

        then:"the ComponentScanningServiceRegistrarConfigurer is registered with the ComponentScanningServiceRegistrarConfigurer"
        1 * mockBeanDefinitionBuilder.addPropertyValue("basePackage", "com.company")
        1 * mockBeanDefinitionBuilder.addPropertyValue("dispatcherReference", "srdReference")
        1 * mockBeanDefinitionBuilder.addPropertyValue("failWhenNoServiceInstanceFound", false)
        1 * mockBeanDefinitionRegistry.registerBeanDefinition("componentScanningServiceRegistrarConfigurer", mockBeanDefinitionBuilder.getBeanDefinition())

    }
}
