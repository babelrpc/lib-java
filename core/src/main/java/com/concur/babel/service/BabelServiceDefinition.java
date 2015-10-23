package com.concur.babel.service;

import com.concur.babel.BabelService;
import com.concur.babel.processor.BaseInvoker;

/**
 * An interface that defines a BabelService.
 */
public interface BabelServiceDefinition {

    /**
     * Creates a new {@link com.concur.babel.processor.BaseInvoker} for the provided {@link com.concur.babel.BabelService}
     * implementation to a babel generated service interface.
     *
     * @param babelServiceImpl the {@link com.concur.babel.BabelService} implementation.
     *
     * @return the {@link com.concur.babel.processor.BaseInvoker} for the babel service.
     */
    public BaseInvoker createInvoker(BabelService babelServiceImpl);

    /**
     * Gets the {@link com.concur.babel.BabelService} interface class for the babel service.
     *
     * @return the {@link com.concur.babel.BabelService} interface class.
     */
    public Class<? extends BabelService> getIfaceClass();
}
