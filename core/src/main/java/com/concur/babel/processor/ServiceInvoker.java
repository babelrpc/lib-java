package com.concur.babel.processor;

import com.concur.babel.transport.server.ServerTransport;

/**
 * ServiceInvoker is an interface that defines the interface for invoking babel hosted services.
 */
public interface ServiceInvoker<I> {

	/**
	 * Method invoke is used to invoke a service method of a service hosted by babel.
	 * 
	 * @param transport - an instance of a server transport.
	 */	
	void invoke(ServerTransport transport) throws Throwable;
	
	/**
	 * Method getServiceName is used to get a name this service uses to identify itself.
	 * 
	 * @return the name of the babel service.
	 */
	String getServiceName();
	
	/**
	 * Method getService is used to get the implemented service object represented by this service
	 * processor.
	 * 
	 * @return a service implementation.
	 */
	I getService();
	
	/**
	 * Method getInterface is used to get the service interface class that represents the interface
	 * definition that this service processor can make requests to.
	 * 
	 * @return the service interface class.
	 */
	Class<I> getInterface();
	
}