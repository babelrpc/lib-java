package com.concur.babel.transport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

import com.concur.babel.ServiceMethod;
import com.concur.babel.protocol.Protocol;
import com.concur.babel.transport.handler.ServiceConnection;

/**
 * SocketTransport is a transport for making calls via sockets.  This is just experimental at the
 * moment.
 */
public class SocketTransport extends BaseTransport implements Transport {

	private String host;
	private int port;
	
	/**
	 * Creates a new SocketTransport
	 * 
	 * @param host The host of the babel service
	 * @param port The port number of the host of the babel service 
	 * @param protocol The protocol to use
	 */
	public SocketTransport(String host, int port, Protocol protocol) {
		super(protocol);		
		this.host = host;
		this.port = port;
	}
	
	/**
	 * Creates a new SocketTransport
	 * 
	 * @param host The host of the babel service
	 * @param port The port number of the host of the babel service 
	 * @param protocol The protocol to use
	 * @param timeout The connection and read timeout values, in milliseconds
	 */
	public SocketTransport(String host, int port, Protocol protocol, int timeout) {
		this(host, port, protocol, timeout, timeout);
	}
	
	/**
	 * Creates a new SocketTransport
	 * 
	 * @param host The host of the babel service
	 * @param port The port number of the host of the babel service 
	 * @param protocol The protocol to use
	 * @param connectionTimeout The connection timeout value, in milliseconds
	 * @param readTimeout The read timeout value, in milliseconds
	 */
	public SocketTransport(
		String host, 
		int port, 
		Protocol protocol, 
		int connectionTimeout, 
		int readTimeout) 
	{
		this(host, port, protocol);
		this.setConnectionTimeout(connectionTimeout);
		this.setReadTimeout(readTimeout);
	}
	
	/**
	 * Method callEndPoint is SocketTransport's implementation of calling a babel service endpoint.
	 * 
	 */
	protected void callEndPoint(ServiceCallManager serviceCallManager) throws IOException {
		
		Socket socket = null;
		
		try {
			
			String headerMsg = this.buildHeaders(serviceCallManager.getHeaders());
			String request = serviceCallManager.getRequest();
			ServiceMethod serviceMethod = serviceCallManager.getServiceMethod();
			
			socket = this.initSocket();
			socket.getOutputStream().write(headerMsg.getBytes());
			socket.getOutputStream().write(new String(serviceMethod.getServiceName() + "||" + 
				serviceMethod.getMethodName() + "\r\n").getBytes());
			socket.getOutputStream().write(request.getBytes());
			socket.getOutputStream().flush();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			Integer responseCode = Integer.parseInt(reader.readLine());
			String response = reader.readLine();
			
			serviceCallManager.setResponse(response);
			serviceCallManager.setResponseCode(responseCode);
			
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
		}
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	protected String getRequestMessage(ServiceMethod serviceMethod) {
		
		return super.getRequestMessage(serviceMethod) + "\r\n";
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected ServiceConnection getServiceConnection(ServiceMethod serviceMethod) {
		
		return new ServiceConnection(
        	serviceMethod.getServiceName(), 
        	serviceMethod.getMethodName(), 
        	this.host,
        	this.port);
		
	}
	
	private Socket initSocket() throws IOException {
		
		Socket socket = new Socket();
		socket.setSoTimeout(this.readTimeout);
		socket.connect(new InetSocketAddress(this.host, this.port), this.connectionTimeout);
		return socket;
		
	}
	
	private String buildHeaders(Map<String, String> headers) {
		
		StringBuilder sb = new StringBuilder();
		
		int index = 1;
		for (String key : this.headers.keySet()) {
			
			sb.append(key).append("=").append(headers.get(key));
			if (index != this.headers.size()) {
				sb.append("||");
			}
			index++;
			
		}
		sb.append("\r\n");
		return sb.toString();
		
	}
	
}
