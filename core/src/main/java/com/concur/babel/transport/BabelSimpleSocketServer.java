package com.concur.babel.transport;


import static com.concur.babel.ArgValidator.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.concur.babel.processor.ServiceInvoker;
import com.concur.babel.processor.ServiceRequestDispatcher;
import com.concur.babel.protocol.Protocol;

public class BabelSimpleSocketServer {

	private boolean stopped = false;
	private final int port;
	private ServerSocket listener;
	private Protocol protocol;
	
	protected ServiceRequestDispatcher manager = new ServiceRequestDispatcher();
	
	public BabelSimpleSocketServer(int port, Protocol protocol) {
		this.port = port;
		this.protocol = protocol;
	}
	
	public void start() {
		
		try {
			this.listener = new ServerSocket(port);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		this.stopped = false;
		while (!stopped) {
	
			try {
				
				Socket socket = this.listener.accept();
				new BabelSocketThread(this.manager, socket, this.protocol).run();			
				
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
		}
		
	}
	
	public void stop() {
		this.stopped = true;
		if (this.listener != null) {
			try {
				this.listener.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void register(ServiceInvoker<?> processor) {
		
		notNull("processor", processor);
		this.manager.register(processor);
		
	}
	
	public ServiceRequestDispatcher getManager() { return this.manager; }
		
}
