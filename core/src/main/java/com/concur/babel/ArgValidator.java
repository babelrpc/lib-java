package com.concur.babel;

/**
 * ArgValidator is a simple helper class used to validate method parameters.
 */
public class ArgValidator {

	/**
	 * Private no-arg constructor so that this class remains a singleton.
	 */
	private ArgValidator() {
		
	} // ArgValidator
	
	public static void notNull(String paramName, Object param)
	throws
		IllegalArgumentException
    {
		
		if (param == null) 
		{
			
			throw new IllegalArgumentException("Parameter: " + paramName + 
				" can not be NULL");
			
		}
		
	}
	
	public static void notNull(String[] paramNames, Object[] params)
	throws
		IllegalArgumentException
	{
	
		if (paramNames.length != params.length) 
		{
			
			throw new IllegalArgumentException("Param names array lenght did not " + 
				"match params array lenght");
			
		}
		
		for (int index = 0; index < params.length; index++) 
		{
			
			notNull(paramNames[index], params[index]);
			
		}
		
	}

	public static void preCondition(boolean expression, String message) 
	throws
		IllegalArgumentException
	{
		
		if (!expression) 
		{
			
			throw new IllegalArgumentException(message);
			
		}
		
	}		
	
}