package com.concur.babel.transport;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.concur.babel.processor.ServiceRequestDispatcher;
import com.concur.babel.protocol.JSONProtocol;
import com.concur.babel.protocol.Protocol;
import com.concur.babel.transport.server.HttpServerTransport;
import com.concur.babel.transport.server.handler.CallHandler;

/**
 * BabelServlet is the base HTTP transport for the babel framework.  You should extend this class
 * and register babel services with it.  Requests processed by this servlet will be in the form of
 * <<service_name>>/<<method_name>>.
 */
@SuppressWarnings("serial")
public abstract class BabelServlet extends HttpServlet {
	
	protected ServiceRequestDispatcher dispatcher = new ServiceRequestDispatcher();
	protected Protocol protocol = new JSONProtocol();
	
	@Override
	public void init() throws ServletException {
	
		super.init();
		this.registerServices(this.dispatcher);
		
	}
	
	public ServiceRequestDispatcher getDispatcher() { return this.dispatcher; }

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws 
		ServletException, IOException 
	{		
		
		this.callService(
			new HttpServerTransport(req, resp, this.protocol), 
			new HashMap<String, String>());
			
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	throws 
		ServletException, IOException 
	{
	    doPost(request, response);
	}	
		
	protected void callService(
		HttpServerTransport serverTransport, 
		Map<String,String> requestContext) 
	throws
		IOException
	{
		
		this.dispatcher.dispatch(serverTransport, requestContext);
		
	}
	
	protected void addCallHandler(CallHandler callHandler) {
		
	}
	
	/**
	 * Method registerServices must be implemented in the extending servlet to allow users of the
	 * babel framework to register service invokers with the babel request dispatcher.
	 * 
	 * @param dispatcher the servlets instance of a ServiceRequestDispatcher.
	 */
	protected abstract void registerServices(ServiceRequestDispatcher dispatcher);
	
}