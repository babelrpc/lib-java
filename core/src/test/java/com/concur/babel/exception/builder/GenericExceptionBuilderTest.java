package com.concur.babel.exception.builder;

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

import com.concur.babel.exception.BabelApplicationException;
import com.concur.babel.exception.BabelException;
import com.concur.babel.transport.server.ServerTransport;

@RunWith(JUnit4.class)
public class GenericExceptionBuilderTest {

	@Test
	public void testBuildWithErrorCodeMessageTagsWithNonAppException() {
		
		ServerTransport mockTransport = createMock(ServerTransport.class);
		
		GenericExceptionBuilder builder = new GenericExceptionBuilder(
			"8888", 
			"My Message", 
			Arrays.asList("babel"), 
			false);
		
		BabelException exception = builder.buildException(
			new RuntimeException("testing exception builder"), 
			new HashMap<String, String>(), 
			mockTransport);
		
		assertFalse(exception instanceof BabelApplicationException);
		assertEquals(exception.getMessage(), "ErrorCode: 8888 - My Message");
		assertNotNull(exception.getCause());
		assertEquals(exception.getServiceError().getTags(), Arrays.asList("babel"));
		
	}
	
	@Test
	public void testBuildWithErrorCodeMessageTagsWithAppException() {
		
		ServerTransport mockTransport = createMock(ServerTransport.class);
		
		GenericExceptionBuilder builder = new GenericExceptionBuilder(
			"8888", 
			"My Message", 
			Arrays.asList("babel"), 
			true);
		
		BabelException exception = builder.buildException(
			new RuntimeException("testing exception builder"), 
			new HashMap<String, String>(), 
			mockTransport);
		
		assertTrue(exception instanceof BabelApplicationException);
		assertEquals(exception.getMessage(), "ErrorCode: 8888 - My Message");
		assertNotNull(exception.getCause());
		assertEquals(exception.getServiceError().getTags(), Arrays.asList("babel"));
		
	}	
	
	@Test
	public void testBuildWithErrorCodeMessageWithNonAppException() {
		
		ServerTransport mockTransport = createMock(ServerTransport.class);
		
		GenericExceptionBuilder builder = new GenericExceptionBuilder(
			"8888", 
			"My Message",  
			false);
		
		BabelException exception = builder.buildException(
			new RuntimeException("testing exception builder"), 
			new HashMap<String, String>(), 
			mockTransport);
		
		assertFalse(exception instanceof BabelApplicationException);
		assertEquals(exception.getMessage(), "ErrorCode: 8888 - My Message");
		assertNotNull(exception.getCause());
		assertEquals(exception.getServiceError().getTags(), new ArrayList<String>());
		
	}	
	
	@Test
	public void testBuildWithErrorCodeMessageWithAppException() {
		
		ServerTransport mockTransport = createMock(ServerTransport.class);
		
		GenericExceptionBuilder builder = new GenericExceptionBuilder(
			"8888", 
			"My Message",  
			true);
		
		BabelException exception = builder.buildException(
			new RuntimeException("testing exception builder"), 
			new HashMap<String, String>(), 
			mockTransport);
		
		assertTrue(exception instanceof BabelApplicationException);
		assertEquals(exception.getMessage(), "ErrorCode: 8888 - My Message");
		assertNotNull(exception.getCause());
		assertEquals(exception.getServiceError().getTags(), new ArrayList<String>());
		
	}	
	
	@Test
	public void testBuildWithErrorCodeWithNonAppException() {
		
		ServerTransport mockTransport = createMock(ServerTransport.class);
		
		GenericExceptionBuilder builder = new GenericExceptionBuilder(
			"8888",
			false);
		
		BabelException exception = builder.buildException(
			new RuntimeException("testing exception builder"), 
			new HashMap<String, String>(), 
			mockTransport);
		
		assertFalse(exception instanceof BabelApplicationException);
		assertEquals(exception.getMessage(), "ErrorCode: 8888 - testing exception builder");
		assertNotNull(exception.getCause());
		assertEquals(exception.getServiceError().getTags(), new ArrayList<String>());
		
	}	
	
	@Test
	public void testBuildWithErrorCodeWithAppException() {
		
		ServerTransport mockTransport = createMock(ServerTransport.class);
		
		GenericExceptionBuilder builder = new GenericExceptionBuilder(
			"8888",
			true);
		
		BabelException exception = builder.buildException(
			new RuntimeException("testing exception builder"), 
			new HashMap<String, String>(), 
			mockTransport);
		
		assertTrue(exception instanceof BabelApplicationException);
		assertEquals(exception.getMessage(), "ErrorCode: 8888 - testing exception builder");
		assertNotNull(exception.getCause());
		assertEquals(exception.getServiceError().getTags(), new ArrayList<String>());
		
	}	
	
}
