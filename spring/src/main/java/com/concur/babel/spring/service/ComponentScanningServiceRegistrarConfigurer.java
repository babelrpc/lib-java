package com.concur.babel.spring.service;

import com.concur.babel.processor.ServiceRequestDispatcher;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import static com.concur.babel.ArgValidator.notNull;

/**
 * ComponentScanningServiceRegistrarConfigurer is an {@link org.springframework.context.ApplicationListener}
 * implementation that will listen on the {@link org.springframework.context.event.ContextRefreshedEvent}.
 * The event is handled by configuring a {@link ComponentScanningServiceRegistrar} and then using it to dynamically
 * register Babel services.
 */
public class ComponentScanningServiceRegistrarConfigurer
        implements ApplicationListener<ContextRefreshedEvent>, BeanFactoryAware
{

    private String basePackage;
    private String dispatcherReference;
    private BeanFactory beanFactory;
    private ComponentScanningServiceRegistrar componentScanningServiceRegistrar;
    private boolean failWhenNoServiceInstanceFound;

    //TODO - can we get rid of this constructor?
    /**
     * Creates a new RegistrarConfigurer.
     */
    public ComponentScanningServiceRegistrarConfigurer(){}

    /**
     * Sets the base package that is passed to the {@link ComponentScanningServiceRegistrar} to use
     * when scanning for Babel services to dynamically register.
     *
     * @param basePackage base package to scan from.
     */
    public void setBasePackage(String basePackage) {
        notNull("basePackage", basePackage);
        this.basePackage = basePackage;
    }

    /**
     * Sets the reference to the {@link com.concur.babel.processor.ServiceRequestDispatcher} to configure the
     * {@link ComponentScanningServiceRegistrar} with.
     *
     * @param dispatcherReference
     */
    public void setDispatcherReference(String dispatcherReference) {
        this.dispatcherReference = dispatcherReference;
    }

    /**
     * Sets the {@link org.springframework.beans.factory.BeanFactory} to configure the
     * {@link ComponentScanningServiceRegistrar} with.
     *
     * @param beanFactory
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        notNull("beanFactory", beanFactory);
        this.beanFactory = beanFactory;
    }

    /**
     * Sets the configuration value for whether the {@link ComponentScanningServiceRegistrar} will fail if an
     * implementation for a babel service interface cannot be found.
     * @param failWhenNoServiceInstanceFound
     */
    public void setFailWhenNoServiceInstanceFound(boolean failWhenNoServiceInstanceFound) {
        this.failWhenNoServiceInstanceFound = failWhenNoServiceInstanceFound;
    }

    /**
     * Responds to the {@link org.springframework.context.event.ContextRefreshedEvent} by creating a
     * {@link ComponentScanningServiceRegistrar} to dynamically register Babel services with the
     * {@link com.concur.babel.processor.ServiceRequestDispatcher}.
     *
     * @param event the ContextRefreshedEvent to respond to.
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (this.componentScanningServiceRegistrar == null) {
            this.componentScanningServiceRegistrar =
                    new ComponentScanningServiceRegistrar(
                            this.basePackage,
                            this.getDispatcher(),
                            this.beanFactory,
                            this.failWhenNoServiceInstanceFound);
        }

        this.componentScanningServiceRegistrar.registerBabelServices();
    }

    /*
        Package private constructor for injecting the Registrar dependency.
        Useful for unit testing.
     */
    ComponentScanningServiceRegistrarConfigurer(ComponentScanningServiceRegistrar componentScanningServiceRegistrar) {
        notNull("registrar", componentScanningServiceRegistrar);
        this.componentScanningServiceRegistrar = componentScanningServiceRegistrar;
    }

    private ServiceRequestDispatcher getDispatcher() {
        try {
            return (this.dispatcherReference != null && !"".equals(this.dispatcherReference.trim())) ?
                    (ServiceRequestDispatcher)this.beanFactory.getBean(this.dispatcherReference) :
                    this.beanFactory.getBean(ServiceRequestDispatcher.class);
        } catch (BeansException e) {
            throw new RuntimeException("Could not locate ServiceRequestDispatcher bean.", e);
        }
    }

}
