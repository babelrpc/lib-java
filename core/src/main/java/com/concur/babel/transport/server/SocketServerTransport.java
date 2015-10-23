package com.concur.babel.transport.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.concur.babel.ServiceMethod;
import com.concur.babel.protocol.Protocol;
import com.concur.babel.transport.BaseTransport;

import static com.concur.babel.ArgValidator.*;

public class SocketServerTransport extends BabelServerTransport {

	private final Socket socket;
	private String serviceName;
	private String methodName;
	private String message;
	private String responseMessage;
	private Code responseCode;
	private boolean streamRead = false;
	private Map<String, String> headers = new HashMap<String, String>();
	private boolean isDebugging = false;
	private final String serverName;
	private final String localAddr;
	private final String remoteAddr;
	
	public SocketServerTransport(Socket socket, Protocol protocol) {
		
		super(protocol);
		notNull("socket", socket);
		this.socket = socket;
		this.serverName = this.socket.getLocalAddress().getCanonicalHostName();
		this.localAddr = new String(this.socket.getLocalAddress().getAddress());
		this.remoteAddr = this.socket.getRemoteSocketAddress() != null ?
			this.socket.getRemoteSocketAddress().toString() :
			null;
		
	}

	public String getHeader(String name) {
		if (!this.streamRead) {
			this.readStream();
		}
		return this.headers.get(name);
	}
	
	public Set<String> getHeaderNames() {
		
		if (!this.streamRead) {
			this.readStream();
		}
		
		return this.headers.keySet();
		
	}
	
	public String getLocalHostName() { return this.serverName; }
	
	public String getLocalIpAddress() { return this.localAddr; }

	public String getRemoteIpAddress() { return this.remoteAddr; }
	
	public String getMessage() { return this.message; }
	
	public String getResponseMessage() { return this.responseMessage; }
	
	public Code getResponseCode() { return this.responseCode; }
	
	public int getPort() { return this.socket.getLocalPort(); }
	
	@Override
	public void write(Code code, Object src) {

		try {
		
			this.responseCode = code;
			OutputStream out = this.socket.getOutputStream();
			String header = new String(code.getValue() + "\r\n");
			String message = new String(this.protocol.write(src) + "\r\n");			
			
			if (this.isDebugging()) {
				this.log("Babel Write Socket Header: " + header);
				this.log("Babel Write Socket Message: " + message);
			}
			
			out.write(header.getBytes());
			out.write(message.getBytes());
			out.flush();
			this.responseMessage = message;
			
		} catch (IOException e) {
			throw new RuntimeException("Unable to write to the socket stream", e);
		}
		
	}

	@Override
	public ServiceMethod read(Class<? extends ServiceMethod> clazz) {

		if (!this.streamRead) {
			this.readStream();
		}
		
		ServiceMethod serviceMethod = this.protocol.read(this.message, clazz);		
		if (serviceMethod == null) {
			throw new RuntimeException("Unable to parse service call json");
		}			
		
		return serviceMethod;		
		
	}

	public String getServiceName() {
		
		if (!this.streamRead) {
			this.readStream();
		}
		return this.serviceName;
		
	}
	
	public String getMethodName() {

		if (!this.streamRead) {
			this.readStream();
		}		
		return this.methodName;
		
	}	
	
	public Map<String, String> getHeaders() { 
		
		if (!this.streamRead) {
			this.readStream();
		}
		
		return this.headers; 
	}
	
	protected boolean isDebugging() { return this.isDebugging; }
	
	private void readStream() {
		
		try {
			
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(this.socket.getInputStream()));			
			String headerMsg = reader.readLine();
			String routingMsg = reader.readLine();
			this.message = reader.readLine();
			streamRead = true;
			
			this.setHeaders(headerMsg);
			this.setRouting(routingMsg);			
			
			if (this.isDebugging()) {
				this.log("Babel Read Socket Header: " + headerMsg);				
				this.log("Babel Read Socket Routing: " + routingMsg);
				this.log("Babel Read Socket Message: " + this.message);
			}
			
		} catch (IOException e) {
			throw new RuntimeException("Unable to read message from sockets", e);
		}
		
	}
	
	private void setRouting(String routingInfo) throws IOException {

		StringTokenizer tokenizer = new StringTokenizer(routingInfo, "||");
		if (tokenizer.hasMoreTokens()) {
			this.serviceName = tokenizer.nextToken();
		}
		if (tokenizer.hasMoreTokens()) {
			this.methodName = tokenizer.nextToken();
		}
		
	}
	
	private void setHeaders(String headerMsg) {

		StringTokenizer tokenizer = new StringTokenizer(headerMsg, "||");
		while (tokenizer.hasMoreElements()) {
			String[] pair = tokenizer.nextToken().split("=");
			this.headers.put(pair[0], pair[1]);
		}
		
		if (this.headers.containsKey(BaseTransport.DEBUG_HEADER)) {
			this.isDebugging = Boolean.parseBoolean(this.headers.get(BaseTransport.DEBUG_HEADER));
		}
		
	}
	
}
