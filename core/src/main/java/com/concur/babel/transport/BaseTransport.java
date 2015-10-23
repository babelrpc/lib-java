package com.concur.babel.transport;

import static com.concur.babel.ArgValidator.notNull;
import static com.concur.babel.ArgValidator.preCondition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.concur.babel.ResponseServiceMethod;
import com.concur.babel.ServiceError;
import com.concur.babel.ServiceMethod;
import com.concur.babel.VoidServiceMethod;
import com.concur.babel.exception.BabelApplicationException;
import com.concur.babel.exception.BabelException;
import com.concur.babel.protocol.Protocol;
import com.concur.babel.transport.handler.ResponseHandler;
import com.concur.babel.transport.handler.ServiceConnection;

/**
 * BaseTransport is a base transport class for babel.
 */
public abstract class BaseTransport implements Transport {
	
	public static final String DEBUG_HEADER = "BABEL_DEBUG";
	
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 10000;
    
    protected static final int STATUS_CODE_OK = 200;
    protected static final int STATUS_CODE_INTERNAL_ERROR = 500;
    protected static final int STATUS_CODE_CONFLICT = 409;
	
	protected Map<String, String> headers = new HashMap<String, String>();	
	protected final Protocol protocol;
	protected int connectionTimeout = CONNECT_TIMEOUT;
	protected int readTimeout = READ_TIMEOUT;  	
	
	protected List<ResponseHandler> responseHandlers = new ArrayList<ResponseHandler>();
	protected Retry retry = new Retry(0, 0);
	
	/**
	 * Creates a BaseTransport
	 * 
	 * @param protocol The protocol to use
	 * @param handlers List of ResponseHandler objects
	 */
	public BaseTransport(Protocol protocol, List<ResponseHandler> handlers) {
		this(protocol, CONNECT_TIMEOUT, READ_TIMEOUT, handlers);
	}
	
	/**
	 * Creates a BaseTransport
	 * 
	 * @param protocol The protocol to use
	 */
	public BaseTransport(Protocol protocol) {
		this(protocol, CONNECT_TIMEOUT, READ_TIMEOUT);
	}
	
	/**
	 * Creates a BaseTransport
	 * 
	 * @param protocol The protocol to use
	 * @param timeout The connection and read timeout values, in milliseconds
	 */
	public BaseTransport(Protocol protocol, int timeout) {
		this(protocol, timeout, timeout);
	}
	
	/**
	 * Creates a BaseTransport
	 * 
	 * @param protocol The protocol to use
	 * @param timeout The connection and read timeout values, in milliseconds
	 * @param handlers List of ResponseHandler objects
	 */
	public BaseTransport(Protocol protocol, int timeout, List<ResponseHandler> handlers) {
		this(protocol, timeout, timeout, handlers);
	}
	
	/**
	 * Creates a BaseTransport
	 * 
	 * @param protocol The protocol to use
	 * @param connectionTimeout The connection timeout value, in milliseconds
	 * @param readTimeout The read timeout value, in milliseconds
	 */
	public BaseTransport(Protocol protocol, int connectionTimeout, int readTimeout) {		
		notNull("protocol", protocol);
		this.protocol = protocol;
		this.setConnectionTimeout(connectionTimeout);
		this.setReadTimeout(readTimeout);
	}
	
	/**
	 * Creates a BaseTransport
	 * 
	 * @param protocol The protocol to use
	 * @param connectionTimeout The connection timeout value, in milliseconds
	 * @param readTimeout The read timeout value, in milliseconds
	 * @param handlers List of ResponseHandler objects
	 */
	public BaseTransport(Protocol protocol, int connectionTimeout, int readTimeout, List<ResponseHandler> handlers) {		
		notNull("protocol", protocol);
		notNull("handlers", handlers);
		this.protocol = protocol;
		this.setConnectionTimeout(connectionTimeout);
		this.setReadTimeout(readTimeout);
		this.responseHandlers = handlers;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public <T> T invoke(ResponseServiceMethod<T> serviceMethod) {
		notNull("serviceMethod", serviceMethod);
		String message = this.callService(serviceMethod);
		return this.protocol.read(message, serviceMethod.getReturnType());
	}

	/**
	 * {@inheritDoc}
	 */
	public void invoke(VoidServiceMethod serviceMethod) {
		notNull("serviceMethod", serviceMethod);		
		this.callService(serviceMethod);
	}	
	
	/**
	 * {@inheritDoc}
	 */
	public void setConnectionTimeout(int timeoutInMillis) {
		preCondition(timeoutInMillis >= 0, "Connection timeout must be greater than or equal to ZERO");
		this.connectionTimeout = timeoutInMillis;	
	}

	/**
	 * {@inheritDoc}
	 */
	public void setReadTimeout(int timeoutInMillis) {
		preCondition(timeoutInMillis >=0, "Read timeout must be greater than or equal to ZERO");
		this.readTimeout = timeoutInMillis;	
	}

	/**
	 * {@inheritDoc}
	 */
	public void setHeaders(Map<String, String> headers) {
		notNull("headers", headers);
		this.headers = headers;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setHeader(String key, String value) {
		notNull("key", key);
		notNull("value", value);
		this.headers.put(key, value);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addResponseHandlers(List<ResponseHandler> handlers) {
		notNull("handlers", handlers);
		this.responseHandlers.addAll(handlers);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addResponseHandler(ResponseHandler handler) {
		notNull("handler", handler);
		this.responseHandlers.add(handler);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Default is 0.
	 */
	public void setMaxRetries(int maxRetries) {
		this.setRetry(
			new Retry(maxRetries, this.retry.getRetryDelay())
		);		
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Default is 0.
	 */
	public void setRetryDelay(long retryDelay) {
		this.setRetry(
			new Retry(this.retry.getMaxRetries(), retryDelay)
		);
	}
	
	/**
	 * Method getString gets a String from an InputStream.
	 * @param in The InputStream
	 * @return A String representation of the InputStream.
	 * @throws java.io.IOException
	 */
	protected String getString(InputStream in) throws IOException {
		
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder(); 
		String line;
		try { 
			br = new BufferedReader(new InputStreamReader(in));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}	 		
		return sb.toString();						
	}	
	
	/**
	 * Method getRequestMessage gets the request message to send to the service call.
	 * @param serviceMethod The ServiceMethod that is to be called.
	 * @return A String that represents the request message to send to the service call.
	 */
	protected String getRequestMessage(ServiceMethod serviceMethod) {
		return this.protocol.write(serviceMethod);
	}
	
	/**
	 * Method callService makes the call to the babel service.
	 * @param serviceMethod The ServiceMethod that represents the babel service to call.
	 * @return A String that represents the response from the call to the babel service. 
	 */
	protected String callService(ServiceMethod serviceMethod) {
		
		ServiceCallManager serviceCallManager = new ServiceCallManager(serviceMethod, this);
		try {
			
			while (serviceCallManager.shouldAttempt()) {
				
				try {
					serviceCallManager.makeCall();
				} catch (IOException e) {
					serviceCallManager.handleIOException(e);
				}
				
			}
			
			String response = serviceCallManager.getResponse();
			int responseCode = serviceCallManager.getResponseCode();
			String responseMessage = serviceCallManager.getResponseMessage();
			
			if (responseCode != STATUS_CODE_OK) {
				if (responseCode == STATUS_CODE_INTERNAL_ERROR) {
					throw new BabelException(this.protocol.read(
						response, 
						ServiceError.class));
				} else if (responseCode == STATUS_CODE_CONFLICT) {
					throw new BabelApplicationException(this.protocol.read(
						response, 
						ServiceError.class));
				} else {
					throw new RuntimeException(
						"Error making service call " + 
						responseCode + (responseMessage != null ? ":" + responseMessage : ""));            		            		
				}
			}
			
			serviceCallManager.handleSuccessResponse();
			return response;
			
		} catch (BabelException e) {
			serviceCallManager.handleFailureResponse(e);
			throw e;
		} catch (Exception e) {
			serviceCallManager.handleFailureResponse(e);
			throw new RuntimeException(e);
		}
		
	}
	
	/**
	 * Method callEndPoint allows each transport to implement calling a babel service.
	 * @param serviceCallManager - The ServiceCallManager object that manages the call to the service end point.
	 * Implementers can access the request message, and should set the response, responseCode, and responseMessage.
	 * @throws java.io.IOException
	 */
	protected abstract void callEndPoint(ServiceCallManager serviceCallManager) throws IOException;
	
	/**
	 * Method getServiceConnection gets the ServiceConnection object for the call to the service method.
	 * @param serviceMethod The ServiceMethod that is to be called.
	 * @return A ServiceConnection for the call to the service method.
	 */
	protected abstract ServiceConnection getServiceConnection(ServiceMethod serviceMethod);	
	
	private void setRetry(Retry retry) {
		notNull("retry", retry);
		this.retry = retry;
	}
	
	/**
	 * ServiceCallManager is a class that wraps a call to a babel service.
	 * It is used to start the call, and handle the outcome of a call, 
	 * including any retry logic and response handling.  The object will be
	 * passed to the transport implementation to call the service endpoint.
	 *
	 */
	protected class ServiceCallManager {
		
		private final BaseTransport transport; 
		private final ServiceMethod serviceMethod;
		private final String request;
		private final Map<String, String> headers;
		private final ServiceConnection serviceConnection;
		private final Retry retry;
		private final List<ResponseHandler> responseHandlers;
		
		private String response;
		private Integer responseCode;
		private String responseMessage;
		
		private long start = 0;
		private long end = 0;
		private int attempts = 0;
		private boolean hasFinished;
		
		protected ServiceCallManager(ServiceMethod serviceMethod, BaseTransport transport) {
			
			notNull("serviceMethod", serviceMethod);
			notNull("headers", transport.headers);
			notNull("serviceMethod", serviceMethod);
			notNull("retry", transport.retry);
			notNull("responseHandlers", transport.responseHandlers);
			
			this.transport = transport;
			
			this.serviceMethod = serviceMethod;
			this.request = transport.getRequestMessage(serviceMethod);
			this.headers = transport.headers;
			this.serviceConnection = transport.getServiceConnection(serviceMethod);
			this.retry = transport.retry;
			this.responseHandlers = transport.responseHandlers;
			
		}
		
		public String getRequest() { return request; }
		public ServiceMethod getServiceMethod() { return this.serviceMethod; }
		public Map<String, String> getHeaders() { return this.headers; }

		public String getResponse() {return response; }
		public void setResponse(String response) {
			this.response = response;
		}
		
		public Integer getResponseCode() { return responseCode; }
		public void setResponseCode(Integer responseCode) {
			this.responseCode = responseCode;
		}

		public String getResponseMessage() { return responseMessage; }
		public void setResponseMessage(String responseMessage) {
			this.responseMessage = responseMessage;
		}
		
		public void makeCall() throws IOException {
			this.start();
			this.transport.callEndPoint(this);
			this.finish();
			
		}
		
		public boolean shouldAttempt() {
			return !this.hasFinished && (this.attempts < this.retry.getMaxRetries() + 1);	
		}
		
		public void handleSuccessResponse() {
			
			for (ResponseHandler handler : this.responseHandlers) {
				handler.onSuccess(
	        		this.serviceConnection, 
	        		this.headers, 
	        		this.request,
	        		this.response, 
	        		this.getDuration()); 
            }
			
		}
		
		public void handleFailureResponse(Exception e) {
			
			for (ResponseHandler handler : this.responseHandlers) {	
            	handler.onFailure(
            		this.serviceConnection, 
            		this.headers, 
            		this.request,
            		this.response, 
            		this.getDuration(),
            		this.responseCode,
            		e); 
            }
			
		}
		
		public void handleIOException(IOException e) throws Exception {
			
			if (e instanceof SocketTimeoutException || //read/connect timeout is exceeded
            		e instanceof ConnectException) //server is unreachable
        	{
                if (this.attempts == this.retry.getMaxRetries() + 1) {
                    throw e;             
                }                      
                this.handleFailureResponse(e);
            	if (this.retry.getRetryDelay() > 0) {
            		System.err.println("Sleeping for " + this.retry.getRetryDelay() + "ms");
            		Thread.sleep(this.retry.getRetryDelay());
            	}
        	} else {
        		throw e;
        	}
			
		}
		
		private void start() {
			this.start = System.currentTimeMillis();
			this.end = 0;
			this.attempts++;
		}
		
		private void finish() {
			this.end = System.currentTimeMillis();
			this.hasFinished = true;
		}
		
		private long getDuration() {
			if (this.end == 0) this.end = System.currentTimeMillis(); 
			return this.end - this.start;		
		}
		
	}
	
	/**
	 * Retry encapsulates retry logic for babel clients to use when 
	 * errors occur connecting to a babel hosted service.
	 */
	protected class Retry {
		
		private final int maxRetries;
		private final long retryDelay;
		
		public Retry(int maxRetries, long retryDelay) {
			
			preCondition(maxRetries >= 0, "maxRetries must be greater than or equal to ZERO");
			preCondition(retryDelay >= 0, "retryDelay must be greater than or equal to ZERO");
			
			this.maxRetries = maxRetries;
			this.retryDelay = retryDelay;
			
		}

		public int getMaxRetries() { return maxRetries; }
		public long getRetryDelay() { return retryDelay; }

	}
	
}
