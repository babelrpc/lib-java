package com.concur.babel.transport;

import java.util.List;
import java.util.Map;

import com.concur.babel.ResponseServiceMethod;
import com.concur.babel.VoidServiceMethod;
import com.concur.babel.transport.handler.ResponseHandler;

/**
 * Transport defines the interface of a transport in babel.
 */
public interface Transport {

	
	/**
	 * Method invoke will invoke a call to a babel service
	 * 
	 * @param serviceMethod The babel service method to invoke
	 * @return T the return type of the service method.
	 */
	<T> T invoke(ResponseServiceMethod<T> serviceMethod);
	
	/**
	 Method invoke will invoke a call to a babel service
	 * 
	 * @param serviceMethod The babel service method to invoke
	 */
	void invoke(VoidServiceMethod serviceMethod);
	
	/**
	 * Method setConnectionTimeout is used to set the connection time out value of a transport.
	 * 
	 * @param timeoutInMillis - the timeout in milliseconds.
	 */
	void setConnectionTimeout(int timeoutInMillis);
	
	/**
	 * Method setReadTimeout is used to set the read time out value of a transport, that is the time
	 * the transport should wait for a response after a successful connection has beed made.
	 * 
	 * @param timeoutInMillis - the timeout in milliseconds.
	 */
	void setReadTimeout(int timeoutInMillis);
	
	/**
	 * Method setHeaders sets property headers to a message being sent by a transport.
	 * 
	 * @param headers - the headers to be added.
	 */
	void setHeaders(Map<String, String> headers);
	
	/**
	 * Method setHeader set a header to the message being sent by a transport.
	 * 
	 * @param key = the key of the new header.
	 * @param value = the value of the new header.
	 */
	void setHeader(String key, String value);
	
	/**
	 * Method addResponseHandlers adds a list of ResponseHandler objects to the transport.
	 * 
	 * @param handlers
	 */
	void addResponseHandlers(List<ResponseHandler> handlers);

	/**
	 * Method addResponseHandler adds a ResponseHandler object to the transport.
	 * 
	 * @param handler
	 */
	void addResponseHandler(ResponseHandler handler);
	
	/**
     * Method setMaxRetries sets the maxRetries on the transport.
     * 
     * @param maxRetries The number of times to retry the service call in the event of a failed call.
     */
	void setMaxRetries(int maxRetries);
	
	/**
     * Method setRetryDelay sets the retryDelay on the transport.
     * 
     * @param retryDelay The amount of time in ms to delay between retry attempts.
     */
	void setRetryDelay(long retryDelay);
		
}
