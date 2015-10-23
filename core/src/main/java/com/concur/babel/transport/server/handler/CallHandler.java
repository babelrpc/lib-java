package com.concur.babel.transport.server.handler;

import com.concur.babel.transport.server.ServerTransport;

/**
 * CallHandler defines the interface of a processing handler hook to a babel hosted service layer.
 * The hook allows for custom processing logic after the service call.
 *
 */
public interface CallHandler {
	
	/**
	 * Method onSuccess will be called when a call to a babel hosted service returns successfully.
	 * @param transport
	 * @param duration - the duration that the service call took.
	 */
	void onSuccess(
		ServerTransport transport,  
		long duration);
	
	/**
	 * Method onFailure will be called when a call to a babel hosted service returns unsuccessfully.
	 * 
	 * @param transport - the ServerTransport object that encapsulates the service container.
	 * @param duration - the duration that the service call took.
	 * @param errorCode - the error code that was returned.
	 * @param exception - the exception that caused the failure.
	 */
	void onFailure(
		ServerTransport transport,  
		long duration, 
		Integer errorCode, 
		Exception exception);

}
