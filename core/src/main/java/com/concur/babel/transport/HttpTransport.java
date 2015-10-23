package com.concur.babel.transport;

import static com.concur.babel.ArgValidator.notNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import com.concur.babel.ServiceMethod;
import com.concur.babel.protocol.Protocol;
import com.concur.babel.transport.handler.ServiceConnection;

/**
 * HttpTransport is an Http transport implementation for babel.
 * 
 * By default all calls will be made via POSTs with a read/connect timeout of 10000 milliseconds.
 */
public class HttpTransport extends BaseTransport {

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String HTTP_METHOD_POST = "POST";	
    
    private String contentType = "json";
    private String httpMethod = HTTP_METHOD_POST;  
	
    private final String url;
	
	/**
	 * Creates a new HttpTransport
	 * 
	 * @param url A String representation of the url
	 * @param protocol The protocol to use
	 */
	public HttpTransport(String url, Protocol protocol) {
		super(protocol);
		notNull("url", url);
		this.url = url;
	}    
	
	/**
	 * Creates a new HttpTransport
	 * 
	 * @param url A String representation of the url
	 * @param protocol The protocol to use
	 * @param timeout The connection and read timeout values, in milliseconds
	 */
	public HttpTransport(String url, Protocol protocol, int timeout) {		
		this(url, protocol, timeout, timeout);
	}
	
	/**
	 * Creates a new HttpTransport
	 * 
	 * @param url A String representation of the url
	 * @param protocol The protocol to use
	 * @param connectionTimeout The connection timeout value, in milliseconds
	 * @param readTimeout The read timeout value, in milliseconds
	 */
	public HttpTransport(String url, Protocol protocol, int connectionTimeout, int readTimeout) {		
		this(url, protocol);
		this.setConnectionTimeout(connectionTimeout);
		this.setReadTimeout(readTimeout);
	}
	
	/**
	 * Method setContentType sets the content type for this transport.
	 * The default type is json.
	 * @param contentType The content type.
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Method setHttpMethod sets the http method for this transport.
	 * The default method is POST.
	 * @param httpMethod The HTTP method.
	 */
	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}
	
	/**
	 * Method callEndPoint is HTTPTransport's implementation of calling a babel service endpoint.
	 */
	protected void callEndPoint(ServiceCallManager serviceCallManager) throws IOException{
		
		HttpURLConnection conn = null;
		try {
			
			String request = serviceCallManager.getRequest();
			conn = this.getConnection(serviceCallManager);
			
			PrintWriter pw = new PrintWriter(conn.getOutputStream());
        	pw.println(request);
            pw.close();	                  						

            Integer responseCode = conn.getResponseCode();
            String response = null;
			String responseMessage = null;
            
            if (responseCode != HttpURLConnection.HTTP_OK) {
            	response = this.getString(conn.getErrorStream());
            	responseMessage = conn.getResponseMessage();
            } else {
            	response = this.getString(conn.getInputStream());
            }            
            
            serviceCallManager.setResponse(response);
            serviceCallManager.setResponseCode(responseCode);
            serviceCallManager.setResponseMessage(responseMessage);
			
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
			
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected ServiceConnection getServiceConnection(ServiceMethod serviceMethod) {
		
		URL url = null;
		try {
			url = this.getUrl(serviceMethod);
		} catch (MalformedURLException e) {
			//Nothing - port will just be blank	
		}
		
		ServiceConnection serviceConnection = new ServiceConnection(
        	serviceMethod.getServiceName(), 
        	serviceMethod.getMethodName(), 
        	this.url,
        	url == null ? null : url.getPort());
		
		return serviceConnection;
	}
	
	private URL getUrl(ServiceMethod serviceMethod) throws MalformedURLException {
		
		return new URL(this.url + "/" + serviceMethod.getServiceName() + "/" + 
            	serviceMethod.getMethodName());
		
	}
	
	private HttpURLConnection getConnection(ServiceCallManager serviceCallManager) throws IOException {
		
		ServiceMethod serviceMethod = serviceCallManager.getServiceMethod();
		
		URL url = this.getUrl(serviceMethod);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestProperty(CONTENT_TYPE, this.contentType);
        conn.setRequestMethod(this.httpMethod);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(this.connectionTimeout);
        conn.setReadTimeout(this.readTimeout);
        
        Map<String, String> headers = serviceCallManager.getHeaders();
        if (headers != null) {
        	for (Entry<String, String> e : headers.entrySet()) {
        		conn.addRequestProperty(e.getKey(), URLEncoder.encode(e.getValue(), "UTF-8").replace("+", "%20"));		
        	}
        }
        
        return conn;
	}
	
}
