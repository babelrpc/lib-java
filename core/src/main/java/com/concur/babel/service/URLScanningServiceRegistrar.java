package com.concur.babel.service;

import com.concur.babel.BabelService;
import com.concur.babel.processor.ServiceRequestDispatcher;
import com.concur.babel.transport.BaseClient;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.concur.babel.ArgValidator.notNull;

/**
 * URLScanningServiceRegistrar will dynamically register babel service implementations with a
 * {@link com.concur.babel.processor.ServiceRequestDispatcher}.  It uses a {@link URLScanningClassFinder} to
 * find {@link com.concur.babel.service.BabelServiceDefinition} classes, then again to find an implementation class
 * for each one.  It will reflectively create a new instance of the implementation class and register it with
 * the dispatcher.
 *
 */
public class URLScanningServiceRegistrar extends AbstractServiceRegistrar {

    protected Logger logger = Logger.getLogger(URLScanningServiceRegistrar.class.getName());
    private URLScanningClassFinder classFinder;

    /**
     * Creates a new URLScanningServiceRegistrar.
     *
     * @param basePackage the base package look in for {@link com.concur.babel.service.BabelServiceDefinition}'s
     *                    to register.
     * @param dispatcher the {@link com.concur.babel.processor.ServiceRequestDispatcher} to register
     *                   {@link com.concur.babel.BabelService} instances with.
     */
    public URLScanningServiceRegistrar(String basePackage, ServiceRequestDispatcher dispatcher) {
        this(basePackage, dispatcher, false);
    }

    /**
     * Creates a new URLScanningServiceRegistrar.
     *
     * @param basePackage the base package look in for {@link com.concur.babel.service.BabelServiceDefinition}'s
     *                    to register.
     * @param dispatcher the {@link com.concur.babel.processor.ServiceRequestDispatcher} to register
     *                   {@link com.concur.babel.BabelService} instances with.
     * @param failWhenNoServiceInstanceFound whether or not registration should fail if no service instance can be
     *                                       found for a given {@link com.concur.babel.service.BabelServiceDefinition}.
     *                                       If set to false, a warning will be logged, but registration will continue.
     */
    public URLScanningServiceRegistrar(
            String basePackage,
            ServiceRequestDispatcher dispatcher,
            boolean failWhenNoServiceInstanceFound)
    {
        this(basePackage, dispatcher, failWhenNoServiceInstanceFound, new URLScanningClassFinder(basePackage));
    }



    /**
     * Uses a {@link URLScanningClassFinder} to get the list of
     * {@link com.concur.babel.service.BabelServiceDefinition}'s that need to be registered with the
     * {@link com.concur.babel.processor.ServiceRequestDispatcher}.
     *
     * @return the list {@link com.concur.babel.service.BabelServiceDefinition}'s.
     */
    @Override
    protected List<BabelServiceDefinition> getServiceDefinitions() {
        List<BabelServiceDefinition> definitions = new ArrayList<>();
        try {
            for (Class<BabelServiceDefinition> definitionClass : this.classFinder.findImplementations(BabelServiceDefinition.class)) {
                definitions.add(definitionClass.newInstance());
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not load BabelServiceDefinitions.", e);
        }
        return definitions;
    }

    /**
     * Uses a {@link URLScanningClassFinder} to locate the {@link com.concur.babel.BabelService} instance
     * for the provided {@link com.concur.babel.service.BabelServiceDefinition}.
     *
     * @param babelServiceDefinition the definition of the service to find the instance for.
     *
     * @return the {@link com.concur.babel.BabelService} instance
     */
    @Override
    protected BabelService getServiceInstance(BabelServiceDefinition babelServiceDefinition) {
        notNull("babelServiceDefinition", babelServiceDefinition);

        for (Class serviceClass : this.classFinder.findImplementations(babelServiceDefinition.getIfaceClass())) {
            if (!BaseClient.class.isAssignableFrom(serviceClass)) {
                try {
                    return (BabelService)serviceClass.newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Could not get babel service instance for " +
                            babelServiceDefinition.getIfaceClass(), e);
                }
            }
        }

        if (this.failWhenNoServiceInstanceFound) {
            throw new RuntimeException("Could not find service instance for " +
                    babelServiceDefinition.getIfaceClass().getName());
        } else {
            this.logger.warning("Could not find service instance for " +
                    babelServiceDefinition.getIfaceClass().getName());
            return null;
        }
    }

    /*
        Package private constructor to inject dependencies.  Useful for unit testing.
     */
    URLScanningServiceRegistrar(
            String basePackage,
            ServiceRequestDispatcher dispatcher,
            boolean failWhenNoServiceInstanceFound,
            URLScanningClassFinder classFinder)
    {
        super(basePackage, dispatcher, failWhenNoServiceInstanceFound);
        notNull("classFinder", classFinder);
        this.classFinder = classFinder;
    }
}
