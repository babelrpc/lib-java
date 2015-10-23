// AUTO-GENERATED FILE - DO NOT MODIFY
// Generated from lib/error.babel
// Babel's Error Format

package com.concur.babel;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;




/**
 * Error defines a single error message and code that might be localized
 * and displayed to a caller.
 */
public class Error implements Serializable {	



	/**
	 * The service-specific error code
	 */
	@SerializedName("Code")
	private String code;


	/**
	 * The text of the error in US-English
	 */
	@SerializedName("Message")
	private String message;


	/**
	 * The list of parameters to the error message. This could be used by
	 * localization systems to generate messages based on the error code.
	 */
	@SerializedName("Params")
	private java.util.List<String> params = new java.util.ArrayList<String>();

	public Error() {}

	public Error(
		String code,
		String message,
		java.util.List<String> params)
	{

		this.code = code;
		this.message = message;
		this.params = params;
	}

	public String getCode() { return this.code; };
	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() { return this.message; };
	public void setMessage(String message) {
		this.message = message;
	}

	public java.util.List<String> getParams() { return this.params; };
	public void setParams(java.util.List<String> params) {
		this.params = params;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("Error(");
		sb.append("code:");
		sb.append(this.code + ", ");
		sb.append("message:");
		sb.append(this.message + ", ");
		sb.append("params:");
		sb.append(this.params + ")");
		return sb.toString();
	}
}