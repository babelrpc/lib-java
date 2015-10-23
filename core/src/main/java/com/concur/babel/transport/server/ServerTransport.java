package com.concur.babel.transport.server;

import java.util.Map;
import java.util.Set;

import com.concur.babel.ServiceMethod;
import com.concur.babel.transport.server.BabelServerTransport.Code;

public interface ServerTransport {

	/**
	 * Method write is used to write/serialize a given object into a message sent back to a client
	 * for a given transport and protocol.
	 * 
	 * @param code - The response code to be used for the response.
	 * @param src - The object to be serialized into the response of the message.
	 */
	void write(Code code, Object src);
	
	/**
	 * Method read is used to read/de-serialize a request message into a ServiceMethod to be be used
	 * to call a service.
	 * 
	 * @param clazz - The class of the ServiceMethod the request message should be serialized to.
	 *
	 * @return an instance of a ServiceMethod.
	 */
	ServiceMethod read(Class<? extends ServiceMethod> clazz);
	
	/**
	 * Method getServiceName is used to get the name of the service to be called from the request
	 * message coming in to be processed.
	 * 
	 * @return the name of the service to call.
	 */
	String getServiceName();
	
	/**
	 * Method getMethodName is used to get the name of the method on a service to be called from the
	 * request message coming in to be processed.
	 * 
	 * @return the name of the method on a service to call.
	 */
	String getMethodName();
	
	/**
	 * Method getMessage is used to get a string representing the message coming in to be processed.
	 * 
	 * @return a string representing the in coming message.
	 */
	String getMessage();
	
	/**
	 * Method getResponseMessage is used to get the response message sent back to a service caller
	 * by the write method of this transport.
	 * 
	 * @return a response message;
	 */
	String getResponseMessage();	
	
	/**
	 * Method getResponseCode get the response code the server transport used when sending a response
	 * to a client calling a hosted babel service.
	 * 
	 * @return response code used for a service response.
	 */
	BabelServerTransport.Code getResponseCode();
	
	/**
	 * Method getHeader is used to return a header value for a given name.
	 * 
	 * @param name - The name of the header.
	 * 
	 * @return the value of the header or null if no header exists for the given name.
	 */
	String getHeader(String name);
	
	/**
	 * Method getHeaderNames is used to get a set of header names available on the incoming request.
	 * 
	 * @return a set of header names.
	 */
	Set<String> getHeaderNames();
	
	/**
	 * Method getLocalHostName is used to get the local host name being used to process a server
	 * request..in some cases this could end up being local IP is the name can not be resolved.
	 * 
	 * @return the local host name.
	 */
	String getLocalHostName();
	
	/**
	 * Method getLocalIpAddress is used to get the local ip being used to process a server request.
	 * 
	 * @return the local host IP.
	 */
	String getLocalIpAddress();
	
	/**
	 * Method getRemoteIpAddress is used to get the remote IP of the client sending the request
	 * to the server.
	 * 
	 * @return the remote host IP.
	 */
	String getRemoteIpAddress();
	
	/**
	 * Method getHeaders is used to get a map of headers available on the incoming request.
	 * 
	 * @return a map of header name value pairs.
	 */
	Map<String, String> getHeaders();
	
	/**
	 * 
	 * Method getPort is used to get the port number of the interface on which the request was received.
	 * 
	 * @return the port number.
	 */
	int getPort();
	
}
