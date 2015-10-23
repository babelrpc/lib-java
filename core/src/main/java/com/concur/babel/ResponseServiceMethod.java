package com.concur.babel;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * ResponseServiceMethod is an implementation of the ServiceMethod interface used to handle
 * Babel service methods that return a response.
 * 
 * @param <T> - defines the return type of the service method.
 */
public abstract class ResponseServiceMethod<T> implements ServiceMethod {	
	
	/**
	 * Method getReturnType is used to return the generic return type "at runtime" of this service
	 * method.
	 * 
	 * @return Type - the return type of this service method.
	 */
	public Type getReturnType() {
		ParameterizedType paramType = (ParameterizedType)getClass().getGenericSuperclass();
		return paramType.getActualTypeArguments()[0];
	}	
	
}