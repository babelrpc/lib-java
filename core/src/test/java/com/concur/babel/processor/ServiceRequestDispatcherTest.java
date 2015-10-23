package com.concur.babel.processor;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.concur.babel.transport.server.handler.CallHandler;
import org.easymock.Capture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.concur.babel.ServiceError;
import com.concur.babel.exception.BabelException;
import com.concur.babel.exception.builder.ExceptionBuilder;
import com.concur.babel.exception.builder.GenericExceptionBuilder;
import com.concur.babel.test.service.TweetService;
import com.concur.babel.test.service.TweetServiceImpl;
import com.concur.babel.transport.server.BabelServerTransport;
import com.concur.babel.transport.server.ServerTransport;
import org.omg.SendingContext.RunTime;

@RunWith(JUnit4.class)
public class ServiceRequestDispatcherTest {
		
	@Test
	public void testProcessCanHandleUnexpectedExceptionFromService() throws Throwable {
	
		ServiceInvoker<TweetService.Iface> mockInvoker = createMock(ServiceInvoker.class);		
		ServerTransport mockTransport = createMock(ServerTransport.class);
		
		Capture<ServiceError> serviceError = new Capture<ServiceError>();
		expect(mockInvoker.getServiceName()).andReturn("ServiceName");
		expect(mockTransport.getServiceName()).andReturn("ServiceName");
		expect(mockInvoker.getInterface()).andReturn(TweetService.Iface.class);
		mockInvoker.invoke(mockTransport);
		expectLastCall().andThrow(new RuntimeException("bad something!"));	
		mockTransport.write(
			eq(BabelServerTransport.Code.UNEXPECTED_ERROR), 
			capture(serviceError));				
		
		replay(mockTransport, mockInvoker);
		
		ServiceRequestDispatcher dispatcher = new ServiceRequestDispatcher();
		dispatcher.register(mockInvoker);
		
		dispatcher.dispatch(mockTransport);
		
		verify(mockTransport, mockInvoker);	
		
	}
	
	@Test
	public void testProcessCanHandleUnexpectedExceptionFromServiceUsingOverridenDefaultExceptionBuilder() throws Throwable {
	
		ServiceInvoker<TweetService.Iface> mockInvoker = createMock(ServiceInvoker.class);		
		ServerTransport mockTransport = createMock(ServerTransport.class);
		
		Capture<ServiceError> serviceError = new Capture<ServiceError>();
		expect(mockInvoker.getServiceName()).andReturn("ServiceName");
		expect(mockTransport.getServiceName()).andReturn("ServiceName");
		expect(mockInvoker.getInterface()).andReturn(TweetService.Iface.class);
		mockInvoker.invoke(mockTransport);
		expectLastCall().andThrow(new RuntimeException("bad something!"));	
		mockTransport.write(
			eq(BabelServerTransport.Code.UNEXPECTED_ERROR), 
			capture(serviceError));				
		
		replay(mockTransport, mockInvoker);
		
		ServiceRequestDispatcher dispatcher = new ServiceRequestDispatcher();
		dispatcher.register(mockInvoker);
		dispatcher.setDefaultExceptionBuilder(new GenericExceptionBuilder("6666", false));
		
		dispatcher.dispatch(mockTransport);
		
		verify(mockTransport, mockInvoker);	
		assertEquals("6666", serviceError.getValue().getErrors().get(0).getCode());
		assertEquals(serviceError.getValue().getErrors().get(0).getMessage(), "bad something!");
		
	}	
	
	@Test
	public void testProcessCanHandleThrownNonAppExceptionThatIsMappedToBuilder() throws Throwable {
		
		ServiceInvoker<TweetService.Iface> mockInvoker = createMock(ServiceInvoker.class);		
		ServerTransport mockTransport = createMock(ServerTransport.class);
		
		Capture<ServiceError> serviceError = new Capture<ServiceError>();
		expect(mockInvoker.getServiceName()).andReturn("ServiceName");
		expect(mockTransport.getServiceName()).andReturn("ServiceName");
		expect(mockInvoker.getInterface()).andReturn(TweetService.Iface.class);
		mockInvoker.invoke(mockTransport);
		expectLastCall().andThrow(new RuntimeException("bad something!"));	
		mockTransport.write(
			eq(BabelServerTransport.Code.UNEXPECTED_ERROR), 
			capture(serviceError));				
		
		replay(mockTransport, mockInvoker);
		
		ServiceRequestDispatcher dispatcher = new ServiceRequestDispatcher();
		dispatcher.register(mockInvoker);
		dispatcher.registerExceptionBuilder(RuntimeException.class, new GenericExceptionBuilder("8888", false));
		
		dispatcher.dispatch(mockTransport);
		
		verify(mockTransport, mockInvoker);	
		assertEquals("8888", serviceError.getValue().getErrors().get(0).getCode());
		assertEquals(serviceError.getValue().getErrors().get(0).getMessage(), "bad something!");		
		
	}
		
	@Test
	public void testProcessCanHandleThrownAppExceptionThatIsMappedToBuilder() throws Throwable {
		
		ServiceInvoker<TweetService.Iface> mockInvoker = createMock(ServiceInvoker.class);		
		ServerTransport mockTransport = createMock(ServerTransport.class);
		
		Capture<ServiceError> serviceError = new Capture<ServiceError>();
		expect(mockInvoker.getServiceName()).andReturn("ServiceName");
		expect(mockTransport.getServiceName()).andReturn("ServiceName");
		expect(mockInvoker.getInterface()).andReturn(TweetService.Iface.class);
		mockInvoker.invoke(mockTransport);
		expectLastCall().andThrow(new RuntimeException("bad something!"));	
		mockTransport.write(
			eq(BabelServerTransport.Code.APP_ERROR), 
			capture(serviceError));				
		
		replay(mockTransport, mockInvoker);
		
		ServiceRequestDispatcher dispatcher = new ServiceRequestDispatcher();
		dispatcher.register(mockInvoker);
		dispatcher.registerExceptionBuilder(RuntimeException.class, new GenericExceptionBuilder("8888", true));
		
		dispatcher.dispatch(mockTransport);
		
		verify(mockTransport, mockInvoker);	
		assertEquals("8888", serviceError.getValue().getErrors().get(0).getCode());
		assertEquals(serviceError.getValue().getErrors().get(0).getMessage(), "bad something!");		
		
	}	
	
	@Test
	public void testProcessCanHandleThrownNonAppExceptionThatIsMappedToBuilderButIsAParentToTheThrownException() throws Throwable {
		
		ServiceInvoker<TweetService.Iface> mockInvoker = createMock(ServiceInvoker.class);		
		ServerTransport mockTransport = createMock(ServerTransport.class);
		
		Capture<ServiceError> serviceError = new Capture<ServiceError>();
		expect(mockInvoker.getServiceName()).andReturn("ServiceName");
		expect(mockTransport.getServiceName()).andReturn("ServiceName");
		expect(mockInvoker.getInterface()).andReturn(TweetService.Iface.class);
		mockInvoker.invoke(mockTransport);
		expectLastCall().andThrow(new NullPointerException("bad something!"));	
		mockTransport.write(
			eq(BabelServerTransport.Code.UNEXPECTED_ERROR), 
			capture(serviceError));				
		
		replay(mockTransport, mockInvoker);
		
		ServiceRequestDispatcher dispatcher = new ServiceRequestDispatcher();
		dispatcher.register(mockInvoker);
		dispatcher.registerExceptionBuilder(RuntimeException.class, new GenericExceptionBuilder("8888", false));
		
		dispatcher.dispatch(mockTransport);
		
		verify(mockTransport, mockInvoker);	
		assertEquals("8888", serviceError.getValue().getErrors().get(0).getCode());
		assertEquals(serviceError.getValue().getErrors().get(0).getMessage(), "bad something!");		
		
	}		
	
	@Test
	public void testProcessCanHandleThrownExceptionInstance() throws Throwable {
		
		ServiceInvoker<TweetService.Iface> mockInvoker = createMock(ServiceInvoker.class);		
		ServerTransport mockTransport = createMock(ServerTransport.class);
		
		Capture<ServiceError> serviceError = new Capture<ServiceError>();
		expect(mockInvoker.getServiceName()).andReturn("ServiceName");
		expect(mockTransport.getServiceName()).andReturn("ServiceName");
		expect(mockInvoker.getInterface()).andReturn(TweetService.Iface.class);
		mockInvoker.invoke(mockTransport);
		expectLastCall().andThrow(new Exception("bad something!"));	
		mockTransport.write(
			eq(BabelServerTransport.Code.UNEXPECTED_ERROR), 
			capture(serviceError));				
		
		replay(mockTransport, mockInvoker);
		
		ServiceRequestDispatcher dispatcher = new ServiceRequestDispatcher();
		dispatcher.register(mockInvoker);
		
		dispatcher.dispatch(mockTransport);
		
		verify(mockTransport, mockInvoker);	
		assertEquals("9999", serviceError.getValue().getErrors().get(0).getCode());
		assertEquals(serviceError.getValue().getErrors().get(0).getMessage(), "Unexpected Exception Occurred");		
		
	}
	
	@Test
	public void testYouCanSendErrorResponseWithSendErrorResponseMethod() {
		
		ServiceInvoker<TweetService.Iface> mockInvoker = createMock(ServiceInvoker.class);		
		ServerTransport mockTransport = createMock(ServerTransport.class);	
		
		Capture<ServiceError> serviceError = new Capture<ServiceError>();	
		expect(mockInvoker.getServiceName()).andReturn("ServiceName");	
		expect(mockInvoker.getInterface()).andReturn(TweetService.Iface.class);
		mockTransport.write(
			eq(BabelServerTransport.Code.UNEXPECTED_ERROR), 
			capture(serviceError));			
		
		replay(mockTransport, mockInvoker);
		
		ServiceRequestDispatcher dispatcher = new ServiceRequestDispatcher();
		dispatcher.register(mockInvoker);
		
		dispatcher.sendErrorResponse(
			new RuntimeException("you did something bad!"), 
			mockTransport, 
			new HashMap<String, String>());		
		
		verify(mockTransport, mockInvoker);
		assertEquals("9999", serviceError.getValue().getErrors().get(0).getCode());
		assertEquals(serviceError.getValue().getErrors().get(0).getMessage(), "Unexpected Exception Occurred");			
		
	}

    @Test
    public void sendErrorResponseShouldAddOnFailureOfRegisteredCallHandlers() {

        CallHandler mockCallHandler = createMock(CallHandler.class);
        mockCallHandler.onFailure(
            anyObject(ServerTransport.class),
            anyLong(),
            anyInt(),
            anyObject(RuntimeException.class));
        ServerTransport mockTransport = createNiceMock(ServerTransport.class);
        mockTransport.write(BabelServerTransport.Code.UNEXPECTED_ERROR, null);
        expect(mockTransport.getResponseCode()).andStubReturn(BabelServerTransport.Code.UNEXPECTED_ERROR);

        ServiceRequestDispatcher dispatcher = new ServiceRequestDispatcher();
        dispatcher.addCallHandler(mockCallHandler);

        replay(mockCallHandler, mockTransport);

        dispatcher.sendErrorResponse(new RuntimeException("error"), mockTransport, null);

        verify(mockCallHandler);

    }
	
	@Test
	public void testDispatcherThrowsExceptionIfNoServiceIsMapped() {
	
		ServerTransport mockTransport = createMock(ServerTransport.class);	
		Capture<ServiceError> serviceError = new Capture<ServiceError>();
		expect(mockTransport.getServiceName()).andReturn("badServiceName");
		mockTransport.write(
			eq(BabelServerTransport.Code.UNEXPECTED_ERROR), 
			capture(serviceError));
		replay(mockTransport);
		
		ServiceRequestDispatcher dispatcher = new ServiceRequestDispatcher();
		dispatcher.register(new TweetService.Invoker(new TweetServiceImpl()));				
		
		dispatcher.dispatch(mockTransport);
		
		verify(mockTransport);
		assertEquals(serviceError.getValue().getErrors().get(0).getMessage(), "Unexpected Exception Occurred");
		
	}	
	
	@Test
	public void testDispatcherFiltersStackTraces() throws Throwable {
				
		ServiceInvoker<TweetService.Iface> mockInvoker = createMock(ServiceInvoker.class);		
		ServerTransport mockTransport = createMock(ServerTransport.class);		
		
		Capture<ServiceError> serviceError = new Capture<ServiceError>();
		expect(mockInvoker.getServiceName()).andReturn("ServiceName");
		expect(mockTransport.getServiceName()).andReturn("ServiceName");
		expect(mockInvoker.getInterface()).andReturn(TweetService.Iface.class);
		mockInvoker.invoke(mockTransport);
		expectLastCall().andThrow(new RuntimeException("My Test Message"));
		mockTransport.write(
			eq(BabelServerTransport.Code.UNEXPECTED_ERROR), 
			capture(serviceError));		
		
		replay(mockTransport, mockInvoker);
		
		ServiceRequestDispatcher dispatcher = new ServiceRequestDispatcher();
		dispatcher.register(mockInvoker);
		dispatcher.setFilterStackTraces(true);
		dispatcher.setDefaultExceptionBuilder(new InnerServiceErrorBuilder());
		
		dispatcher.dispatch(mockTransport);		
		
		verify(mockTransport, mockInvoker);	
		assertEquals("7777", serviceError.getValue().getErrors().get(0).getCode());
		assertEquals(serviceError.getValue().getErrors().get(0).getMessage(), "error occurred");
		assertNull(serviceError.getValue().getDetails());	
		assertNotNull(serviceError.getValue().getInner());
		assertNull(serviceError.getValue().getInner().getDetails());
		
	}	
	
	@Test
	public void testProcessCanSuccessfullyMakeServiceCallWithRequestContext() throws Throwable {
		
		ServiceInvoker<TweetService.Iface> mockInvoker = createMock(ServiceInvoker.class);		
		ServerTransport mockTransport = createMock(ServerTransport.class);		
		
		expect(mockInvoker.getServiceName()).andReturn("ServiceName");
		expect(mockTransport.getServiceName()).andReturn("ServiceName");
		expect(mockInvoker.getInterface()).andReturn(TweetService.Iface.class);
		mockInvoker.invoke(mockTransport);
		
		replay(mockTransport, mockInvoker);
		
		ServiceRequestDispatcher dispatcher = new ServiceRequestDispatcher();
		dispatcher.register(mockInvoker);
		
		dispatcher.dispatch(mockTransport, new HashMap<String, String>());		
		
		verify(mockTransport, mockInvoker);	
		
	}

	@Test
	public void testProcessCanSuccessfullyMakeServiceCallWithOutRequestContext() throws Throwable {

		ServiceInvoker<TweetService.Iface> mockInvoker = createMock(ServiceInvoker.class);
		ServerTransport mockTransport = createMock(ServerTransport.class);

		expect(mockInvoker.getServiceName()).andReturn("ServiceName");
		expect(mockTransport.getServiceName()).andReturn("ServiceName");
		expect(mockInvoker.getInterface()).andReturn(TweetService.Iface.class);
		mockInvoker.invoke(mockTransport);

		replay(mockTransport, mockInvoker);

		ServiceRequestDispatcher dispatcher = new ServiceRequestDispatcher();
		dispatcher.register(mockInvoker);

		dispatcher.dispatch(mockTransport);

		verify(mockTransport, mockInvoker);

	}

	private class InnerServiceErrorBuilder implements ExceptionBuilder {

		public BabelException buildException(
			Throwable exception,
			Map<String, String> requestContext,
			ServerTransport serverTransport) 
		{
		
			ServiceError innerError = new ServiceError();
			innerError.setTime(new Date());
			innerError.setDetails("some testing details");
			
			BabelException babelException = new BabelException("7777", "error occurred", exception);
			babelException.setInnerServiceError(innerError);
			
			return babelException;
			
		}
		
	}
	
}
