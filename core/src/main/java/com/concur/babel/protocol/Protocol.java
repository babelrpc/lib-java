package com.concur.babel.protocol;

import java.io.Reader;
import java.lang.reflect.Type;

/**
 * Protocol is an interface that defines available protocols that the babel framework can use.
 */
public interface Protocol {

	<T> T read(String json, Class<T> classOfT); 
	
	<T> T read(String json, Type typeOfT);
	
	<T> T read(Reader reader, Class<T> classOfT); 
	
	<T> T read(Reader reader, Type typeOfT);	
		
	String write(Object src);	
	
}
