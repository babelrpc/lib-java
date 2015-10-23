package com.concur.babel.spring.service;

import com.concur.babel.BabelService;
import com.concur.babel.processor.ServiceRequestDispatcher;
import com.concur.babel.service.AbstractServiceRegistrar;
import com.concur.babel.service.BabelServiceDefinition;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.concur.babel.ArgValidator.notNull;

/**
 * ComponentScanningServiceRegistrar extends {@link com.concur.babel.service.AbstractServiceRegistrar} and provides
 * a Spring implementation for scanning for Babel generated classes that can be dynamically registered with a
 * {@link com.concur.babel.processor.ServiceRequestDispatcher}.  It uses a Spring
 * {@link org.springframework.beans.factory.BeanFactory} to lookup the concrete Babel service implementation beans.
 */
public class ComponentScanningServiceRegistrar extends AbstractServiceRegistrar {

    protected Logger logger = Logger.getLogger(ComponentScanningServiceRegistrar.class.getName());

    private final BeanFactory beanFactory;
    private final ClassPathScanningCandidateComponentProvider scanner;

    /**
     * Creates a new ComponentScanningServiceRegistrar with the provided basePackage, dispatcher, and beanFactory.
     *
     * @param basePackage the package to look in for {@link com.concur.babel.service.BabelServiceDefinition} and
     *                    {@link com.concur.babel.BabelService}'s to register.
     * @param dispatcher the {@link com.concur.babel.processor.ServiceRequestDispatcher} to register the service with.
     * @param beanFactory the BeanFactory to get {@link com.concur.babel.BabelService} beans from.
     */
    public ComponentScanningServiceRegistrar(
            String basePackage,
            ServiceRequestDispatcher dispatcher,
            BeanFactory beanFactory)
    {
        this(basePackage, dispatcher, beanFactory, false);
    }

    /**
     * Creates a new ComponentScanningServiceRegistrar with the provided basePackage, dispatcher, beanFactory and
     * indicator whether not finding a {@link com.concur.babel.BabelService} bean should be a failure or not.
     * @param basePackage the package to look in for {@link com.concur.babel.service.BabelServiceDefinition} and
     *                    {@link com.concur.babel.BabelService}'s to register.
     * @param dispatcher the {@link com.concur.babel.processor.ServiceRequestDispatcher} to register the service with.
     * @param beanFactory the BeanFactory to get {@link com.concur.babel.BabelService} beans from.
     * @param failWhenNoServiceInstanceFound indicates if a failure to find a {@link com.concur.babel.BabelService}
     *                                       instance for a given {@link com.concur.babel.service.BabelServiceDefinition}
     *                                       should result in a failure or not.
     */
    public ComponentScanningServiceRegistrar(
            String basePackage,
            ServiceRequestDispatcher dispatcher,
            BeanFactory beanFactory,
            boolean failWhenNoServiceInstanceFound)
    {
        this(basePackage,
                dispatcher,
                beanFactory,
                failWhenNoServiceInstanceFound,
                new ClassPathScanningCandidateComponentProvider(false));
    }

    /**
     * Utilizes the configured {@link ClassPathScanningCandidateComponentProvider} to create a list of
     * {@link com.concur.babel.service.BabelServiceDefinition} objects to be registered.
     *
     * @return the list of {@link com.concur.babel.service.BabelServiceDefinition}s that should be registered with the
     * {@link com.concur.babel.processor.ServiceRequestDispatcher}.
     */
    @Override
    protected List<BabelServiceDefinition> getServiceDefinitions() {
        List<BabelServiceDefinition> babelServiceDefinitions = new ArrayList<>();
        for (BeanDefinition beanDefinition : this.scanner.findCandidateComponents(this.basePackage)) {
            try {
                Class<?> serviceDefinitionClass = Class.forName(beanDefinition.getBeanClassName());
                babelServiceDefinitions.add((BabelServiceDefinition) serviceDefinitionClass.newInstance());
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Could not add babel service definition for " +
                        beanDefinition.getBeanClassName(), e);
            }
        }
        return babelServiceDefinitions;
    }

    /**
     * Gets the {@link com.concur.babel.BabelService} instance for the provided {@link com.concur.babel.service.BabelServiceDefinition}.
     *
     * @param babelServiceDefinition the {@link com.concur.babel.service.BabelServiceDefinition} of the {@link com.concur.babel.BabelService}
     *                    that needs to be registered.
     *
     *
     * @return the {@link com.concur.babel.BabelService} instance to register with the
     * {@link com.concur.babel.processor.ServiceRequestDispatcher}
     */
    @Override
    protected BabelService getServiceInstance(final BabelServiceDefinition babelServiceDefinition) {
        notNull("babelServiceDefinition", babelServiceDefinition);
        try {
            return this.beanFactory.getBean(babelServiceDefinition.getIfaceClass());
        } catch (BeansException e) {
            if (this.failWhenNoServiceInstanceFound) {
                throw new RuntimeException("Could not find service instance for " +
                        babelServiceDefinition.getIfaceClass().getName(), e);
            } else {
                this.logger.warning("Could not find service instance for " +
                        babelServiceDefinition.getIfaceClass().getName());
            }
        }
        return null;
    }

    /*
        Package private constructor to inject the ClassPathScanningCandidateComponentProvider dependency.
        Useful for unit testing.
    */
    ComponentScanningServiceRegistrar(
            String basePackage,
            ServiceRequestDispatcher dispatcher,
            BeanFactory beanFactory,
            boolean failWhenNoServiceInstanceFound,
            ClassPathScanningCandidateComponentProvider scanner)
    {
        super(basePackage, dispatcher, failWhenNoServiceInstanceFound);
        notNull("beanFactory", beanFactory);
        notNull("scanner", scanner);
        this.beanFactory = beanFactory;
        this.scanner = scanner;
        this.scanner.resetFilters(false);
        this.scanner.addIncludeFilter(new AssignableTypeFilter(BabelServiceDefinition.class));
    }
}
