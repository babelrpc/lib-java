package com.concur.babel.spring.annotation;

import com.concur.babel.spring.service.ComponentScanningServiceRegistrarConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * BabelServiceRegistrationProcessor is an {@link org.springframework.context.annotation.ImportBeanDefinitionRegistrar} implementation
 * that dynamically registers a {@link com.concur.babel.spring.service.ComponentScanningServiceRegistrarConfigurer} bean.
 */
public class BabelServiceRegistrationProcessor implements ImportBeanDefinitionRegistrar {

    private final BeanDefinitionBuilder builder;

    /**
     * Creates a new BabelServiceRegistrationProcessor and initializes the BeanDefinitionBuilder.
     */
    public BabelServiceRegistrationProcessor() {
        this.builder = BeanDefinitionBuilder.genericBeanDefinition(ComponentScanningServiceRegistrarConfigurer.class);
    }

    /**
     * Dynamically registers a {@link com.concur.babel.spring.service.ComponentScanningServiceRegistrarConfigurer} bean.
     *
     * @param importingClassMetadata {@link org.springframework.core.type.AnnotationMetadata} used to configure a
     *                               {@link com.concur.babel.spring.service.ComponentScanningServiceRegistrarConfigurer}
     *                               before registering it.
     * @param registry registry that the {@link com.concur.babel.spring.service.ComponentScanningServiceRegistrarConfigurer}
     *                 will be registered with.
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes =
                AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(BabelServiceRegistration.class.getName()));
        this.builder.addPropertyValue("basePackage", attributes.getString("basePackage"));
        this.builder.addPropertyValue("dispatcherReference", attributes.getString("dispatcherReference"));
        this.builder.addPropertyValue("failWhenNoServiceInstanceFound", attributes.getBoolean("failWhenNoServiceInstanceFound"));
        registry.registerBeanDefinition("componentScanningServiceRegistrarConfigurer", this.builder.getBeanDefinition());
    }

    /*
        Package private constructor to inject the BeanDefinitionBuilder dependency.
        Useful for unit tests.
    */
    BabelServiceRegistrationProcessor(BeanDefinitionBuilder builder) {
        this.builder = builder;
    }
}
