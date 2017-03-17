package com.cs.enums;

/**
 * An enum of available HTTP methods.
 */
public enum HttpMethod
{
	CONNECT,
	DELETE,
	GET,
	HEAD,
	OPTIONS,
	POST,	
	PUT;

	@Override
	public String toString()
	{
		return this.name();
	}

	public static HttpMethod extractMethod( String headerLine ) throws IllegalArgumentException
	{
		String method = headerLine.split( " " )[ 0 ];

		if ( method != null )
		{
			return HttpMethod.valueOf( method );
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}
}
