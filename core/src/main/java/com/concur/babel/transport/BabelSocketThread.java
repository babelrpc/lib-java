package com.concur.babel.transport;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static com.concur.babel.ArgValidator.*;

import com.concur.babel.processor.ServiceRequestDispatcher;
import com.concur.babel.protocol.Protocol;
import com.concur.babel.transport.server.SocketServerTransport;

/**
 * BabelSocketThread is the main thread that is used to read from a socket, call a babel service
 * and write the response.  This can be overridden to allow for hooks into the process like logging,
 * metrics, transactions etc etc.
 */
public class BabelSocketThread extends Thread {

	private ServiceRequestDispatcher dispatcher;
	private final Socket socket;
	private final Protocol protocol;
	
	/**
	 * BabelSocketThread created a new instance of a worker thread for invoking hosted babel 
	 * services via sockets.
	 * 
	 * @param dispatcher - an instance of a ServiceRequestDispatcher.
	 * @param socket - a socket instance that is receiving a babel service request. 
	 * @param protocol - the protocol being used on the wire for the service request and response.
	 */
	public BabelSocketThread(
		ServiceRequestDispatcher dispatcher, 
		Socket socket, 
		Protocol protocol) 
	{
		
		notNull("dispatcher", dispatcher);
		notNull("socket", socket);
		notNull("protocol", protocol);
		this.dispatcher = dispatcher;
		this.socket = socket;
		this.protocol = protocol;
		
	}
	
	public void run() {
		
		this.callService(new HashMap<String, String>());
		
		try {
			this.socket.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	/**
	 * Method callService is the main method being used to invoke a hosted babel service. You can
	 * extend this class and override this method to provide before/after processing if need be.
	 * 
	 * @param requestContext - any request context information you might want to add that can be 
	 * used by exception builders in the event of an exception being thrown from a service or in
	 * the context of a service request/response.
	 * 
	 */
	protected void callService(Map<String, String> requestContext) {
		
		this.dispatcher.dispatch(new SocketServerTransport(
			this.socket, 
			this.protocol),
			requestContext);		
		
	}	
	
}
