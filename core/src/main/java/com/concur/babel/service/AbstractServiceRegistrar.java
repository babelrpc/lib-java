package com.concur.babel.service;

import com.concur.babel.BabelService;
import com.concur.babel.processor.ServiceRequestDispatcher;

import java.util.List;
import java.util.logging.Logger;

import static com.concur.babel.ArgValidator.notNull;

/**
 * AbstractServiceRegistrar is a base class that will dynamically register babel service implementations with a
 * {@link com.concur.babel.processor.ServiceRequestDispatcher}.  Extending classes must implement logic to get a list
 * of {@link com.concur.babel.service.BabelServiceDefinition} classes that define babel services, and logic to find
 * an instance of a {@link com.concur.babel.BabelService} implementation instance for a given definition.
 *
 * The logic to register the instance with the dispatcher is contained in this class.
 *
 * This class also provides a hook into decorating a service instance prior to registering it.  This can be useful to
 * wrap the service instance in a dynamic proxy for example.
 */
public abstract class AbstractServiceRegistrar {

    protected final String basePackage;
    protected final ServiceRequestDispatcher dispatcher;
    protected final boolean failWhenNoServiceInstanceFound;

    /**
     * Constructor that takes all dependencies required to register babel services.
     *
     * @param basePackage the package to look in for {@link com.concur.babel.service.BabelServiceDefinition} and
     *                    {@link com.concur.babel.BabelService}'s to register.
     * @param dispatcher the {@link com.concur.babel.processor.ServiceRequestDispatcher} to register the service with.
     * @param failWhenNoServiceInstanceFound indicates if a failure to find a {@link com.concur.babel.BabelService}
     *                                       instance for a given {@link com.concur.babel.service.BabelServiceDefinition}
     *                                       should result in a failure or not.
     */
    public AbstractServiceRegistrar(
            String basePackage,
            ServiceRequestDispatcher dispatcher,
            boolean failWhenNoServiceInstanceFound)
    {
        notNull("basePackage", basePackage);
        notNull("dispatcher", dispatcher);
        this.basePackage = basePackage;
        this.dispatcher = dispatcher;
        this.failWhenNoServiceInstanceFound = failWhenNoServiceInstanceFound;
    }

    /**
     * Registers babel service implementations with a {@link com.concur.babel.processor.ServiceRequestDispatcher}.
     *
     */
    public void registerBabelServices() {
        List<BabelServiceDefinition> babelServiceDefinitions = this.getServiceDefinitions();
        if (babelServiceDefinitions != null) {
            BabelService service = null;
            for(BabelServiceDefinition babelServiceDefinition : babelServiceDefinitions) {
                service = this.getServiceInstance(babelServiceDefinition);
                if (service != null) {
                    this.dispatcher.register(
                            babelServiceDefinition.createInvoker(
                                    this.decorateInstance(
                                            (Class<BabelService>)babelServiceDefinition.getIfaceClass(), service)));
                }
            }
        }
    }

    /**
     * Gets a list of {@link com.concur.babel.service.BabelServiceDefinition}'s that will be used to register service
     * implementations with a {@link com.concur.babel.processor.ServiceRequestDispatcher}.
     *
     * @return the list of {@link com.concur.babel.service.BabelServiceDefinition}'s
     */
    protected abstract List<BabelServiceDefinition> getServiceDefinitions();

    /**
     * Gets the {@link com.concur.babel.BabelService} instance to register for the provided
     * {@link com.concur.babel.service.BabelServiceDefinition}
     *
     * @param serviceDefinition the {@link com.concur.babel.service.BabelServiceDefinition} to get a
     * {@link com.concur.babel.BabelService} instance for
     *
     * @return the {@link com.concur.babel.BabelService} instance
     */
    protected abstract BabelService getServiceInstance(BabelServiceDefinition serviceDefinition);

    /**
     * Allows for a {@link com.concur.babel.BabelService} instance to be wrapped in a dynamic proxy.
     * By default this method simply returns the instance.
     * Override this method in an child class to wrap the {@link com.concur.babel.BabelService} instance to meet your needs.
     *
     * @param interfaceClass the interface that the {@link com.concur.babel.BabelService} implements.
     * @param serviceInstance the {@link com.concur.babel.BabelService} instance that will be registered with
     *                        the {@link com.concur.babel.processor.ServiceRequestDispatcher}.
     *
     * @return the {@link com.concur.babel.BabelService} instance that will be registered with the
     *          {@link com.concur.babel.processor.ServiceRequestDispatcher}.
     */
    protected BabelService decorateInstance(Class<BabelService> interfaceClass, BabelService serviceInstance) {
        notNull("interfaceClass", interfaceClass);
        notNull("serviceInstance", serviceInstance);

        return serviceInstance;
    }

}
