package com.cs.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum LogLevels
{
	NONE( 0, "None" ),
	ERROR( 1, "Error" ),
	WARNING( 2, "Warning" ),
	LOG( 3, "Log" );

	private final int code;
	private final String reasonPhrase;
	private static final Map< Integer, HttpStatusCode > codeLookupTable = new HashMap< Integer, HttpStatusCode >();

	static
	{
		for ( HttpStatusCode s : EnumSet.allOf( HttpStatusCode.class ) )
		{
			codeLookupTable.put( s.getCode(), s );
		}
	}

	private LogLevels( int code, String reasonPhrase )
	{
		this.code = code;
		this.reasonPhrase = reasonPhrase;
	}

	public int getCode()
	{
		return code;
	}

	public String getReasonPhrase()
	{
		return reasonPhrase.toUpperCase();
	}

	public static HttpStatusCode getStatusCode( int code )
	{
		return codeLookupTable.get( code );
	}
}