package com.concur.babel.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.concur.babel.Error;
import com.concur.babel.ServiceError;

/**
 * BabelException is a base exception that is used by the bable framework to determine something
 * went wrong during the communication between the client and service layers.
 */
@SuppressWarnings("serial")
public class BabelException extends RuntimeException {
	
	private ServiceError serviceError = new ServiceError();
	
	public BabelException(String message) {
		super();
		this.init(message, null, null);
	}
	
	public BabelException(String message, List<String> messageParams) {
		super();
		this.init(message, null, messageParams);
	}	
	
	public BabelException(String message, Throwable cause) {
		super(cause);
		this.init(message, null, null);
	}
	
	public BabelException(String errorCode, String message) {
		super();
		this.init(message, errorCode, null);
	}
	
	public BabelException(String errorCode, String message, Throwable cause) {
		super(cause);
		this.init(message, errorCode, null);
	}
	
	public BabelException(String errorCode, String message, List<String> messageParams) {
		super();
		this.init(message, errorCode, messageParams);
	}
	
	public BabelException(String errorCode, String message, List<String> messageParams, Throwable cause) {
		super(cause);
		this.init(message, errorCode, messageParams);
	}	
	
	public BabelException(Error ...errors) {
		super();
		this.serviceError.setTime(new Date());
		this.serviceError.setErrors(Arrays.asList(errors));
		this.serviceError.setDetails(this.toStringStack());		
	}
	
	public BabelException(Throwable cause, Error ...errors) {
		super(cause);
		this.serviceError.setTime(new Date());
		this.serviceError.setErrors(Arrays.asList(errors));
		this.serviceError.setDetails(this.toStringStack());		
	}

    /**
     * @return a concatenation of all Error messages associated to this exception. The expected format would include a
     * line break for each message and would include an error code if one exists and if one does not then the line for
     * that Error would only contain the message.
     */
    @Override
    public String getMessage() {

        StringBuilder sb = new StringBuilder();
        int index = 0;

        for (Error error : this.serviceError.getErrors()) {
            if (error.getCode() != null) {
                sb.append("ErrorCode: ").append(error.getCode()).append(" - ");
            }
            sb.append(error.getMessage());

            if (index < this.serviceError.getErrors().size() - 1) sb.append("\n");
            index++;

        }

        return sb.toString();

    }

    /**
	 * Method addContext adds a key/value pair to the context of this exception, in for following
	 * format:
	 * 
	 * {"name":{"value":"value"}
	 * 
	 * @param name - The name of the context value to add.
	 * @param value - The value of the context being added.
	 * 
	 * @return an instance of the exception.
	 */
	public BabelException addContext(String name, String value) {
		this.addContext(name, "value", value);
		return this;
	}
	
	/**
	 * Method addContext adds a key/value pair to the context of this exception, in for following
	 * format:
	 * 
	 * {"name":{"subname":"value"}
	 * 
	 * @param name - The name of the context value to add.
	 * @param subName = The sub name key of the value being added.
	 * @param value - The value of the context being added.
	 * 
	 * @return an instance of the exception.
	 */	
	public BabelException addContext(String name, String subName, String value) {
		Map<String,String> valueMap = new HashMap<String, String>();
		valueMap.put(subName, value);
        this.addContext(name, valueMap);
		return this;
	}
	
	/**
	 * Method addContext adds a key/value pair to the context of this exception, in for following
	 * format:
	 * 
	 * {"name":{"name":"value"}
	 * 
	 * @param name - The name of the context value to add.
	 * @param values - A map of values to be added to the context.
	 * 
	 * @return an instance of the exception.
	 */	
	public BabelException addContext(String name, Map<String,String> values) {
		if (this.serviceError.getContext() == null) {
			this.serviceError.setContext(new HashMap<String, Map<String,String>>());
		}		
		this.serviceError.getContext().put(name, values);
		return this;
	}		
	
	/**
	 * Should not be used outside of the babel framework unless you really know what you are doing.
	 * @param serviceError = An object representing an error that will get serialized across the
	 * wire.
	 */
	public BabelException(ServiceError serviceError) {
		this.serviceError = serviceError;
	}
	
	public void setInnerServiceError(ServiceError innerServiceError) {
		this.serviceError.setInner(innerServiceError);
	}
	
	public ServiceError getServiceError() { return this.serviceError; }
	
	protected BabelException addError(String message) {
		return this.addError(message, null, null);
	}
	
	protected BabelException addError(String message, String code) {
		return this.addError(message, code, null);
	}
	
	protected BabelException addError(String message, String code, List<String> params) {
		if (this.serviceError.getErrors() == null) {
			this.serviceError.setErrors(new ArrayList<Error>());			
		}
		Error error = new Error();
		error.setMessage(message);
		error.setCode(code);
		error.setParams(params);		
		this.serviceError.getErrors().add(error);
		return this;
	}
	
	private String toStringStack() {
		StringWriter sw = new StringWriter();
		this.printStackTrace(new PrintWriter(sw));		
		return sw.toString();
	}
	
	private void init(String message, String code, List<String> params) {
		this.serviceError.setTime(new Date());
		this.serviceError.setTags(new ArrayList<String>());
		this.addError(message, code, params);
		this.serviceError.setDetails(this.toStringStack());
	}
	
}
