package com.concur.babel.transport.server;

import static com.concur.babel.ArgValidator.notNull;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.concur.babel.ServiceMethod;
import com.concur.babel.exception.BabelException;
import com.concur.babel.protocol.JSONProtocol;
import com.concur.babel.protocol.Protocol;
import com.concur.babel.transport.BaseTransport;

public class HttpServerTransport extends BabelServerTransport {

	private HttpServletRequest request;
	private HttpServletResponse response;
	private String serviceName;
	private String methodName;
	private String message;
	private String responseMessage;
	private Code responseCode;
	private boolean isDebugging = false;	
	
	public HttpServerTransport(
		HttpServletRequest request, 
		HttpServletResponse response, 
		Protocol protocol) 
	{
		
		super(protocol);
		
		notNull("request", request);
		notNull("response", response);
		
		this.request = request;
		this.response = response;
		this.parseRequest(request);
		
	}
	
	public String getHeader(String name) {

		try {
			String value = this.request.getHeader(name);
			return (value == null ? value : URLDecoder.decode(value, "UTF-8"));	
		} catch (UnsupportedEncodingException e ) {
			throw new BabelException(e);
		}

	}
	
	public Set<String> getHeaderNames() {		
		Set<String> names = new HashSet<String>();
		for (Enumeration<String> e = this.request.getHeaderNames(); e.hasMoreElements();) {
			names.add(e.nextElement());
		}
		return names;
	}
	
	public void write(Code code, Object src) {
	
		this.responseCode = code;
		this.response.setStatus(code.getValue());
        this.response.setCharacterEncoding("UTF-8");
		if (this.protocol instanceof JSONProtocol) {
			this.response.setContentType("application/json");
		}		
		
		if (src == null) {
			return;
		}
	
		try {
		
			String responseMessage = this.protocol.write(src);
			this.responseMessage = responseMessage;
		
			if (this.isDebugging()) {
				this.log("Babel Write Message: " + responseMessage);
			}

            this.response.getWriter().write(responseMessage);
		
		} catch (Exception e) {
			throw new RuntimeException("Unable to write response on output stream!");
		}
	
	}

	public ServiceMethod read(Class<? extends ServiceMethod> clazz) {

		ServiceMethod serviceMethod;
		if (this.isDebugging()) {
		
			this.log("Babel Read Message: " + this.message);
					
		}
		
		serviceMethod = this.protocol.read(this.message, clazz);
	
		if (serviceMethod == null) {
			throw new RuntimeException("Unable to parse babel service call json for " +
				this.serviceName + "-" + this.methodName);
		}			
	
		return serviceMethod;

	}	

	public HttpServletRequest getRequest() { return this.request; }
	
	public HttpServletResponse getResponse() { return this.response; }
	
	public String getServiceName() { return this.serviceName; }
	
	public String getMethodName() { return this.methodName; }
	
	public String getMessage() { return this.message; }
	
	public String getResponseMessage() { return this.responseMessage; }
	
	public Code getResponseCode() { return this.responseCode; }

	public String getLocalHostName() { return this.request.getServerName(); }
	
	public String getLocalIpAddress() { return this.request.getLocalAddr(); }

	public String getRemoteIpAddress() { return this.request.getRemoteAddr(); }
	
	public Map<String, String> getHeaders() {
		
		Map<String, String> headers = new HashMap<String, String>();
		
		for (String headerName : this.getHeaderNames()) {
			
			headers.put(headerName, this.getHeader(headerName));
			
		}
		
		return headers;
		
	}
	
	public int getPort() { return this.request.getLocalPort(); }

	protected boolean isDebugging() { return this.isDebugging; }
	
	private void parseRequest(HttpServletRequest request) {
		
		if (request.getPathInfo() == null) {
			throw new RuntimeException("Calling service was not found in the URI: " + 
				request.getRequestURI());
		}
		
		String[] callingInfo = request.getPathInfo().split("/");		
		if (callingInfo.length < 2) {
			throw new RuntimeException("URL: " + request.getPathInfo() + " does not appear to " +
				"contain service/method, be sure to include a URL in this format: " +
				"serviceName/methodName");
		}		

		this.serviceName = callingInfo[callingInfo.length - 2];
		this.methodName = callingInfo[callingInfo.length - 1];
		
		if (request.getHeader(BaseTransport.DEBUG_HEADER) != null) {
			this.isDebugging = Boolean.parseBoolean(request.getHeader(BaseTransport.DEBUG_HEADER));
		}
		
		try {
			
			this.message = this.readerToString(
				new InputStreamReader(request.getInputStream(), "UTF-8"));
			
		} catch (Exception e) {
			throw new RuntimeException("Unable to read babel message from input stream for " +
				this.serviceName + "-" + this.methodName, e);
		}		
		
	}
	
}
