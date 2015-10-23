package com.concur.babel.transport.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.concur.babel.protocol.JSONProtocol;

@RunWith(JUnit4.class)
public class HttpServerTransportTest {

	@Test
	public void testThatAnExceptionHappensIfNoURLIsAvailable() {
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		try {
			
			new HttpServerTransport(request, response, new JSONProtocol());
			
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().startsWith("Calling service was not found"));
		}
		
	}
	
	@Test
	public void testThatAServiceAndMethodNameCanBeParsed() {
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setContent(new String("sample data").getBytes());
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setPathInfo("/TweetService/postTweet");
			
		ServerTransport transport = new HttpServerTransport(request, response, new JSONProtocol());
		assertEquals("TweetService", transport.getServiceName());
		assertEquals("postTweet", transport.getMethodName());
		
	}
	
	@Test
	public void testThatServiceNameAndMethodNameCanBeParsedWhenThereIsExtraContextPath() {
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setContent(new String("sample data").getBytes());
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setPathInfo("/someotherpath/TweetService/postTweet");
			
		ServerTransport transport = new HttpServerTransport(request, response, new JSONProtocol());
		assertEquals("TweetService", transport.getServiceName());
		assertEquals("postTweet", transport.getMethodName());		
		
	}
	
	@Test
	public void testThatServiceNameAndMethodNameCanBeParsedWhenWhereIsQueryParameters() {
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setContent(new String("sample data").getBytes());
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setPathInfo("/TweetService/postTweet");
		request.setQueryString("?foo=1&bar=true");
			
		ServerTransport transport = new HttpServerTransport(request, response, new JSONProtocol());
		assertEquals("TweetService", transport.getServiceName());
		assertEquals("postTweet", transport.getMethodName());		
		
	}
	
	@Test
	public void testHeaderDecoding() {
		
		String unicodeName = "Test Unicode";
		String unicodeValue = "日本の首都";
		String spacesName = "Test Spaces";
		String spacesValue = "This is a test";
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(unicodeName, unicodeValue);
		headers.put(spacesName, spacesValue);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setContent(new String("sample data").getBytes());
		request.setPathInfo("/TweetService/postTweet");
		
		try {
			for (Entry<String, String> e : headers.entrySet()) {
				request.addHeader(e.getKey(), URLEncoder.encode(e.getValue(), "UTF-8").replace("+", "%20"));		
        	}
		} catch (Exception e) {
			assertTrue(false);
		}
		
		MockHttpServletResponse response = new MockHttpServletResponse();	
		ServerTransport transport = new HttpServerTransport(request, response, new JSONProtocol());
		assertTrue(unicodeValue.equals(transport.getHeader(unicodeName)));
		assertTrue(spacesValue.equals(transport.getHeader(spacesName)));
		
	}
	
	@Test
	public void testMissingHeaderDecoding() {
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setContent(new String("sample data").getBytes());
		request.setPathInfo("/TweetService/postTweet");
		
		MockHttpServletResponse response = new MockHttpServletResponse();	
		ServerTransport transport = new HttpServerTransport(request, response, new JSONProtocol());
		
		assertTrue(transport.getHeader("EMPTY") == null);
		
	}
	
}
