package com.concur.babel.transport.handler;

/**
 * ServiceConnection encapsulates connection information about a remote service call.
 *
 */
public class ServiceConnection {

	private final String service;
	private final String method;
	private final String host;
	private final Integer port;
	
	public ServiceConnection(
		String serviceName, 
		String methodName,
		String host,
		Integer port) 
	{
		
		this.service = serviceName;
		this.method = methodName;
		this.host = host;
		this.port = port;
		
	}
	
	public String getServiceName() { return service; }
	public String getMethodName() { return method; }
	public String getHost() { return host; }
	public Integer getPortNumber() { return port; }
	
}
