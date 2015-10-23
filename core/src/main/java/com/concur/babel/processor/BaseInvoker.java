package com.concur.babel.processor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.concur.babel.ServiceMethod;
import com.concur.babel.transport.server.BabelServerTransport;
import com.concur.babel.transport.server.ServerTransport;

import static com.concur.babel.ArgValidator.*;

/**
 * BaseInvoker is the base service invoker class within Babel.  All generated babel code
 * includes an "Invoker" class that extends this base class.  This class is used on the babel service
 * layer when a request comes to direct and process the call to the correct implemented service
 * definition.
 * 
 * @param <I> - Should be the "Iface" interface for a generated service.
 */
public abstract class BaseInvoker<I> implements ServiceInvoker<I> {

	private final I serviceImpl;
	private final Map<String, Class<? extends ServiceMethod>> serviceMethodMap;
	
	/**
	 * BaseInvoker creates a new instance of a Invoker for a given service implementation to a
	 * babel generated service interface.
	 * 
	 * @param serviceImpl - an implementation of the generated babel service interface.
	 */
	public BaseInvoker(I serviceImpl) {		
		notNull("serviceImpl", serviceImpl);
		this.serviceImpl = serviceImpl;
		this.serviceMethodMap = this.initServiceMethods();	
	}
	
	public I getService() { return this.serviceImpl; }
	
	public void invoke(ServerTransport transport) throws Throwable {

		notNull("transport", transport);
	
		ServiceMethod serviceMethod = this.getServiceMethod(transport.getMethodName(), transport);
		Method method = this.findServiceImplmentationMethod(
			transport.getMethodName(), 
			serviceMethod);
		
		try {
				
			Object object = method.invoke(this.serviceImpl, serviceMethod.getMethodParameters());
			transport.write(BabelServerTransport.Code.SUCCESS, object);				
		
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
		
	}
	
	/**
	 * Method initServiceMethods is used to init service methods related to this service invoker.
	 * 
	 * @return a map of ServiceMethod types for a given service method name.
	 */
	protected abstract Map<String, Class<? extends ServiceMethod>> initServiceMethods();
	
	private ServiceMethod getServiceMethod(String methodName, ServerTransport transport) {

		Class<? extends ServiceMethod> serviceMethodClass = null;
		
		for (String key : this.serviceMethodMap.keySet()) {
			if (key.equalsIgnoreCase(methodName)) {
				serviceMethodClass = this.serviceMethodMap.get(key);
				break;
			}
		}
		
		if (serviceMethodClass == null) {
			throw new RuntimeException("Unable to find babel processor for service method: " + 
				methodName);
		}
		
		return transport.read(serviceMethodClass);
		
	}
	
	private Method findServiceImplmentationMethod(
		String methodName, 
		ServiceMethod serviceMethod) 
	{
		Method method = null;
		
		for (Method m : this.serviceImpl.getClass().getMethods()) {
			
			if (m.getName().equalsIgnoreCase(methodName) && 
				m.getParameterTypes().length == serviceMethod.getMethodParameters().length) 
			{
				method = m;
				break;
			}
			
		}
		
		if (method == null) {		
			throw new RuntimeException("Unable to find service method: " + methodName + " with " +
				serviceMethod.getMethodParameters().length + " no. arguments on class: " + 
				this.serviceImpl.getClass().getName());
		}
		
		return method;
		
	}
	
}
