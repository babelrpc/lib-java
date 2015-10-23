package com.concur.babel.exception.builder;

import java.util.Map;

import com.concur.babel.exception.BabelException;
import com.concur.babel.transport.server.ServerTransport;

/**
 * ExceptionBuilder defines an interface for building exceptions in babel.  An exception builder
 * provides a way to allow users of babel to define what type of babel exception and what information
 * is provided when sending errors back to service clients.
 */
public interface ExceptionBuilder {

	/**
	 * Method buildException is used to build a babel excaption and service error details to be
	 * sent back to a babel client.
	 *  
	 * @param exception - The exception that was thrown.
	 * @param requestContext - A map of name/value pairs that can be added to the context of the
	 * exception.
	 * @param serverTransport - An instance of the ServerTransport used for the given request.
	 * 
	 * @return an instance of a BabelException.
	 */
	BabelException buildException(
		Throwable exception, 
		Map<String, String> requestContext, 
		ServerTransport serverTransport);
	
}
