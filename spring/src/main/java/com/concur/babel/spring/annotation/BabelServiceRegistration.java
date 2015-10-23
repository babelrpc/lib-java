package com.concur.babel.spring.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Import(BabelServiceRegistrationProcessor.class)
/**
 * BabelServiceRegistration is an annotation that can be used to dynamically register Babel service beans with the
 * {@link com.concur.babel.processor.ServiceRequestDispatcher} bean within a Spring application.
 */
public @interface BabelServiceRegistration {
    /**
     *
     * @return the base package to scan for Babel services to dynamically register.
     */
    String basePackage();

    /**
     * Defaults to empty string.
     *
     * @return the reference of the {@link com.concur.babel.processor.ServiceRequestDispatcher} bean.
     */
    String dispatcherReference() default "";

    /**
     *
     * Defaults to false.
     *
     * @return true if registration should fail if an implementation of a babel service instance cannot be found.
     */
    boolean failWhenNoServiceInstanceFound() default false;
}
