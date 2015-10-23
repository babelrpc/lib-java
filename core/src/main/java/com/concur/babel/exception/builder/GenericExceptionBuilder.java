package com.concur.babel.exception.builder;

import static com.concur.babel.ArgValidator.notNull;

import java.util.List;
import java.util.Map;

import com.concur.babel.exception.BabelApplicationException;
import com.concur.babel.exception.BabelException;
import com.concur.babel.transport.server.ServerTransport;

/**
 * GenericExceptionBuilder is a generic implementation of the ExceptionBuilder interface that can
 * be used to setup quick mappings of an exception with simple information without needing to define
 * and implement your own ExceptionBuilder implmentation.  See each constructor for examples..at the
 * commom level an error code is required but optinal messages, tags as well as if the babel 
 * exception should be app or non app is available.
 */
public class GenericExceptionBuilder implements ExceptionBuilder {
	
	protected final String errorCode;
	protected String message;
	protected List<String> tags;	
	protected final boolean isAppException;
	
	/**
	 * Creates an instance of GenericExceptionBuilder.
	 * 
	 * If there is a message associated to the thrown exception then that will be used as the 
	 * message returned to the client of the babel service.
	 * 
	 * @param errorCode - Required error code that will be returned to the client of a babel service.
	 * @param isAppException - True if you want to generate a babel application exception instead of 
	 * a non application exception.
	 */		
	public GenericExceptionBuilder(
		String errorCode,
		boolean isAppException)
	{
		this(errorCode, null, null, isAppException);
	}	
	
	/**
	 * Creates an instance of GenericExceptionBuilder.
	 * 
	 * @param errorCode - Required error code that will be returned to the client of a babel service.
	 * @param message - Optional message to be returned to the client of the babel service, if ont
	 * is not supplied then the builder will attempt to use the message from the thrown exception.
	 * @param isAppException - True if you want to generate a babel application exception instead of 
	 * a non application exception.
	 */	
	public GenericExceptionBuilder(
		String errorCode,
		String message,
		boolean isAppException)
	{
		this(errorCode, message, null, isAppException);
	}
	
	/**
	 * Creates an instance of GenericExceptionBuilder.
	 * 
	 * @param errorCode - Required error code that will be returned to the client of a babel service.
	 * @param message - Optional message to be returned to the client of the babel service, if ont
	 * is not supplied then the builder will attempt to use the message from the thrown exception.
	 * @param tags - Optional tags that can be returns in the error.
	 * @param isAppException - True if you want to generate a babel application exception instead of 
	 * a non application exception.
	 */
	public GenericExceptionBuilder(
		String errorCode,
		String message,
		List<String> tags,
		boolean isAppException)
	{
		notNull("errorCode", errorCode);
		
		this.errorCode = errorCode;
		this.message = message;
		this.tags = tags;
		this.isAppException = isAppException;
	}
	
	public BabelException buildException(
		Throwable exception,
		Map<String, String> requestContext, 
		ServerTransport serverTransport) 
	{
	
		BabelException babelException = this.createBabelException(
			exception, 
			requestContext, 
			serverTransport);	
		this.addExceptionContext(babelException, requestContext, serverTransport);
		this.addTags(babelException, requestContext, serverTransport);
		
		return babelException;
		
	}
	
	protected BabelException createBabelException(
		Throwable exception,
		Map<String, String> requestContext, 
		ServerTransport serverTransport)
	{
		
		String msg = this.message != null ? this.message : exception.getMessage();
		return this.isAppException ?
			new BabelApplicationException(this.errorCode, msg, exception) :
			new BabelException(this.errorCode, msg, exception);			
		
	}

	protected void addExceptionContext(
		BabelException babelException, 
		Map<String, String> requestContext, 
		ServerTransport serverTransport) 
	{
		
		for (String key : requestContext.keySet()) {
			babelException.addContext(key, requestContext.get(key));
		}			
		
	}
	
	protected void addTags(
		BabelException babelException, 
		Map<String, String> requestContext, 
		ServerTransport serverTransport)  
	{
		
		if (this.tags != null) {
			babelException.getServiceError().getTags().addAll(this.tags);
		}		
		
	}
	
}
