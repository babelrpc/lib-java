package com.concur.babel.processor;

import static com.concur.babel.ArgValidator.notNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.concur.babel.exception.BabelApplicationException;
import com.concur.babel.exception.BabelException;
import com.concur.babel.exception.builder.ExceptionBuilder;
import com.concur.babel.exception.builder.GenericExceptionBuilder;
import com.concur.babel.transport.server.BabelServerTransport;
import com.concur.babel.transport.server.ServerTransport;
import com.concur.babel.transport.server.handler.CallHandler;

/**
 * ServiceRequestDispatcher is the core dispatcher component for hosted babel servers.  The 
 * dispatcher allows you to register service invokers that will take a request from any transport
 * and protocol and call the appropriate service method. The dispatcher also allows for the 
 * registration of ExceptionBuilders that can be used to build the structure of information send
 * back to the caller of a service in the event of an exception being thrown from a service or any
 * where else if the context of a request/response of a service.
 */
public class ServiceRequestDispatcher {

	private Map<String, ServiceInvoker<?>> serviceMap = new HashMap<String, ServiceInvoker<?>>();
	private Map<String, ServiceInvoker<?>> serviceClassMap = 
		new HashMap<String, ServiceInvoker<?>>();
	private boolean filterStackTraces = false;
	private Map<Class<? extends Throwable>, ExceptionBuilder> exceptionBuilderMap = 
		new HashMap<Class<? extends Throwable>, ExceptionBuilder>();
	private List<CallHandler> callHandlers = new ArrayList<CallHandler>();
	private ExceptionBuilder defaultExceptionBuilder;	

	public ServiceRequestDispatcher() {
	
		this.defaultExceptionBuilder = new GenericExceptionBuilder(
			"9999", 
			"Unexpected Exception Occurred", 
			false);
		
	}
		
	/**
	 * Method setFilterStackTraces allows users to determine if stack traces should be filtered from
	 * responses.  This might be useful if you don't want stack trace details to do across the wire.
	 * Note that by default babel will not filter stack traces.  Also the full Exception, with
	 * the stack is available in the CallResult so it can be used for logging or anything else.
	 * 
	 * @param filter - true to turn on filtering.
	 */
	public void setFilterStackTraces(boolean filter) {
		this.filterStackTraces = filter;
	}
	
	/**
	 * Method register is used to register a service and make it available for processing requests.
	 * 
	 * @param processor - An implementation of a babel service.
	 */
	public void register(ServiceInvoker<?> processor) {
		
		notNull("processor", processor);
		
		this.serviceMap.put(processor.getServiceName().toLowerCase(), processor);
		this.serviceClassMap.put(processor.getInterface().getName(), processor);
		
	}	
	
	/**
	 * Method serviceInterfaceClass is used if you want to get the service implementation that was
	 * register with this manager.
	 * 
	 * @param serviceInterfaceClass - The interface class of the service.
	 * 
	 * @return an implementation of the given service interface or null if one is not found for the
	 * given interface class.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getService(Class<T> serviceInterfaceClass) {
		
		ServiceInvoker<?> processor = this.serviceClassMap.get(serviceInterfaceClass.getName());
		
		return processor == null ? null : (T)processor.getService();
		
	}
	
	/**
	 * Method dispatch is used to dispatch a service request to a hosted babel service.
	 * 
	 * @param transport - An instance of a transport that will handle the reading and writing of the
	 * request. 
	 * 
	 * @return true if the call was successful or false if an exception was thrown during the call.
	 */
	public boolean dispatch(ServerTransport transport) {
			
		return this.dispatch(transport, new HashMap<String, String>());
		
	}	
	
	/**
	 * Method dispatch is used to dispatch a service request to a hosted babel service.
	 * 
	 * @param transport - An instance of a transport that will handle the reading and writing of the
	 * request.
	 * @param requestContext - A map of request context information that you may want to use in conjunction with
	 * exception builders.
	 * 
	 * @return true if the call was successful or false if an exception was thrown during the call.
	 */
	public boolean dispatch(ServerTransport transport, Map<String, String> requestContext) {
			
		notNull("transport", transport);
				
		boolean success = true;
		
		long start = System.currentTimeMillis();
		long end;
		
		try {			
			String serviceName = transport.getServiceName().toLowerCase();	
			ServiceInvoker<?> invoker = this.serviceMap.get(serviceName);					
			
			if (invoker == null) {
				throw new RuntimeException("Unable to find service with name: " + serviceName);
			}				
			
			invoker.invoke(transport);
			end = System.currentTimeMillis();
			for (CallHandler handler : this.callHandlers) {
				
				handler.onSuccess(transport, end - start);
				
			}
			
		} catch (Exception e) {
			
			end = System.currentTimeMillis();
			this.sendErrorResponse(e, transport, requestContext, end - start);

			success = false;
		} catch (Throwable t) {
			
			// babel is not going to try and handle Errors.
			throw new RuntimeException(t);
			
		}
		
		return success;
		
	}
	
	/**
	 * Method setDefaultExceptionBuilder can be used to set the default exception builder 
	 * implementation to be used by any thrown exception that is not mapped by the 
	 * "registerExceptionBuilder" method.  By default babel provides a default exception
	 * builder that will provide an error code of 9999 and an "unexpected error" message. This
	 * default exception can be replaced using this method or my registering an exception builder
	 * for Throwable.class.
	 * 
	 * @param builder - the default exception builder implementation to be used.
	 */
	public void setDefaultExceptionBuilder(ExceptionBuilder builder) {
		
		notNull("builder", builder);
		this.defaultExceptionBuilder = builder;
		
	}
	
	/**
	 * Method registerExceptionBuilder is used to register an implmentation of an exception builder
	 * for a given exception class.  If an exception is thrown of this exception class tyep or a 
	 * child of this type this exception builder will be used to provice exception information in an
	 * error message back to the client of a babel service.
	 * 
	 * @param exceptionClass - The class of the exception to map to the exception builder.
	 * @param builder - The exception builder implementation to be used for the given exception
	 * class.
	 */
	public void registerExceptionBuilder(
		Class<? extends Throwable> exceptionClass, 
		ExceptionBuilder builder) 
	{
		notNull("exceptionClass", exceptionClass);
		notNull("builder", builder);
		this.exceptionBuilderMap.put(exceptionClass, builder);
		
	}
	
	/**
	 * Method sendErrorResponse can be used to respond to a client call with an error response out
	 * side of the context of the babel service process manager. Type typical use case for doing
	 * something like this might if users of babel want to throw an exception message to a babel
	 * client when initializing request resources.  Any exception passed to this method will use
	 * any ExceptionBuilder mapping provided/setup for the service process manager.
	 * 
	 * @param exception - The exception to respond with.
	 * @param serverTransport - The service transport used for the requesting call.
	 * @param requestContext - Any request context information that you may want to provide in the
	 * returning error...used by a mapped ExceptionBuilder.
	 */
	public void sendErrorResponse(
		Exception exception,
		ServerTransport serverTransport,
		Map<String,String> requestContext)
	{
	    this.sendErrorResponse(exception, serverTransport, requestContext, 0);
	}

	/**
	 * Method addCallHandler is used to add an implementation of a call handler.
	 * The call handler will be invoked after a babel hosted service method is invoked and
	 * can be used to perform custom logging logic.
	 * 
	 * @param handler
	 */
	public void addCallHandler(CallHandler handler) {
		
		this.callHandlers.add(handler);
		
	}
	
	protected ExceptionBuilder findExceptionBuilder(Class<? extends Throwable> exceptionClass) {
		
		if (this.exceptionBuilderMap.containsKey(exceptionClass)) {
			return this.exceptionBuilderMap.get(exceptionClass);
		}
		
		ExceptionBuilder builder = null;
		Class<?> parentClass = exceptionClass.getSuperclass();
		while (
			parentClass != null && 
			(parentClass != Object.class || parentClass != Throwable.class)) 
		{			
			if (this.exceptionBuilderMap.containsKey(parentClass)) {
				builder = this.exceptionBuilderMap.get(parentClass);
				break;
			}
			parentClass = parentClass.getSuperclass();
		}
		
		return builder == null ? this.defaultExceptionBuilder : builder;
		
	}

    private void sendErrorResponse(
        Exception exception,
        ServerTransport serverTransport,
        Map<String,String> requestContext,
        long duration)
    {

        if (requestContext == null) {
            requestContext = new HashMap<String, String>();
        }
        ExceptionBuilder builder = this.findExceptionBuilder(exception.getClass());
        BabelException babelException = builder.buildException(
                exception,
                requestContext,
                serverTransport);

        if (this.filterStackTraces) {
            babelException.getServiceError().setDetails(null);
            if (babelException.getServiceError().getInner() != null) {
                babelException.getServiceError().getInner().setDetails(null);
            }
        }

        BabelServerTransport.Code code = babelException instanceof BabelApplicationException ?
                BabelServerTransport.Code.APP_ERROR :
                BabelServerTransport.Code.UNEXPECTED_ERROR;

        serverTransport.write(code, babelException.getServiceError());

        for (CallHandler handler : this.callHandlers) {

            handler.onFailure(
                serverTransport,
                duration,
                serverTransport.getResponseCode().getValue(),
                exception);

        }

    }
	
}