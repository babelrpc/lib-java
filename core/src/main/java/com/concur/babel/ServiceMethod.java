package com.concur.babel;

/**
 * ServiceMethod is an interface that defines the methods that are used to define a service method
 * within Babel.
 */
public interface ServiceMethod {

	/**
	 * Method getMethodParameters is used to define the method signature of a service method. This
	 * method should return the correct parameter order so that Babel can use it to call an 
	 * implemented service method correctly.
	 * 
	 * @return Object[] - an ordered array that defines the parameter signature of a service method.
	 */
	Object[] getMethodParameters();
	
	/**
	 * Method getServiceName is used to return the name of the service this class represents
	 * in Babel.
	 * 
	 * @return String - the name of the service this class represents.
	 */
	String getServiceName();
	
	/**
	 * Method getMethodName is used to return the name of the service method this class represents
	 * in Babel.
	 * 
	 * @return String - the name of the service method this class represents.
	 */	
	String getMethodName();	
	
}
