package com.concur.babel.transport.handler;

import java.util.Map;

/**
 * ResponseHandler defines the interface of a processing handler hook to a babel client.
 * The hook allows for client custom processing logic after the remote service call.
 *
 */
public interface ResponseHandler {
	
	/**
	 * Method onSuccess will be called when a remote call from a babel client returns successfully.
	 * 
	 * @param serviceConnection - connection information for the remote service call.
	 * @param headers - the headers on the request.
	 * @param request - the request.
	 * @param response - the response.
	 * @param duration - the duration that the remote service call took.
	 */
	void onSuccess(
		ServiceConnection serviceConnection, 
		Map<String, String> headers, 
		String request, 
		String response, 
		long duration);
	
	/**
	 * Method onFailure will be called when a remote call from a babel client returns unsuccessfully.
	 * 
	 * @param serviceConnection - connection information for the remote service call.
	 * @param headers - the headers on the request.
	 * @param request - the request.
	 * @param response - the response.
	 * @param duration - the duration that the remote service call took.
	 * @param errorCode - the error code that was returned.
	 * @param exception - the exception that caused the failure.
	 */
	void onFailure(
		ServiceConnection serviceConnection, 
		Map<String, String> headers, 
		String request, 
		String response, 
		long duration, 
		Integer errorCode, 
		Exception exception);
	
}
