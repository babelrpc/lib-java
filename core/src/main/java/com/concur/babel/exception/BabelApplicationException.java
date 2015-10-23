package com.concur.babel.exception;

import java.util.List;

import com.concur.babel.Error;
import com.concur.babel.ServiceError;

/**
 * BabelApplicationException is a runtime exception that case be used by developers to provide
 * known erorr conditions back to the client calling a service.
 */
@SuppressWarnings("serial")
public class BabelApplicationException extends BabelException {	

	public BabelApplicationException(String message) {
		super(message);
	}
	
	public BabelApplicationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public BabelApplicationException(String errorCode, String message) {
		super(errorCode, message);
	}
	
	public BabelApplicationException(String errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);
	}
	
	public BabelApplicationException(String errorCode, String message, List<String> messageParams) {
		super(errorCode, message, messageParams);
	}
	
	public BabelApplicationException(String errorCode, String message, List<String> messageParams, Throwable cause) {
		super(errorCode, message, messageParams, cause);
	}	 
	
	public BabelApplicationException(Error ...errors) {
		super(errors);
	}
	
	public BabelApplicationException(Throwable cause, Error ...errors) {
		super(cause, errors);
	}	
	
	/**
	 * Should not be used outside of the babel framework unless you really know what you are doing.
	 * @param serviceError = An object representing an error that will get serialized across the
	 * wire.
	 */
	public BabelApplicationException(ServiceError serviceError) {
		super(serviceError);
	}	
	
}
