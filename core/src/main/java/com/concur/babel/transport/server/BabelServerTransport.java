package com.concur.babel.transport.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import com.concur.babel.protocol.Protocol;

import static com.concur.babel.ArgValidator.*;

/**
 * BabelBaseServerTransport is an abstract class used to handle requests from client transports on
 * the server side.
 */
public abstract class BabelServerTransport implements ServerTransport {
	
	protected static final Logger LOGGER = Logger.getLogger("BabelLogger");
	
	public enum Code {
		
		SUCCESS(200),
		UNEXPECTED_ERROR(500),
		APP_ERROR(409);
		
		private final int value;

		public int getValue() { return this.value; }
		
		private Code(int value) {
			this.value = value;
		}
		
	}	
	
	protected Protocol protocol;
	
	public BabelServerTransport(Protocol protocol) {
		
		notNull("protocol", protocol);
		this.protocol = protocol;
		
	}
	
	protected abstract boolean isDebugging();
	
	protected void log(String message) {
		
		LOGGER.info(this.getServiceName() + "/" + this.getMethodName() + " -- " + message);
		
	}
	
	protected String readerToString(InputStreamReader reader) {
		
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
 
		String line;
		try {
 
			br = new BufferedReader(reader);
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			
			return sb.toString();
 
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}	
	
}
