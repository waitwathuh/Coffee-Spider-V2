package com.cs.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import com.cs.enums.HttpStatusCode;
import com.cs.enums.LogLevels;
import com.cs.interfaces.Config;
import com.cs.interfaces.HttpResponse;
import com.cs.interfaces.Logger;

public class BasicHttpResponse extends BasicHttpMessage implements HttpResponse
{
	private HttpStatusCode statusCode;
	private Config config;

	public BasicHttpResponse( Logger logger, Config config )
	{
		super( logger );
		this.config = config;
		setHeaders( new HashMap< String, String >() );
		setStatusCode( HttpStatusCode.OK );
	}

	@Override
	public HttpStatusCode getStatusCode()
	{
		return statusCode;
	}

	public void setStatusCode( HttpStatusCode statusCode )
	{
		this.statusCode = statusCode;
	}

	public void setBody( File resource )
	{
		try
		{
			setBody( getResourceAsBytes( new FileInputStream( resource ), resource.length() ) );
			setContentTypeHeader( resource.getPath() );
		}
		catch ( IOException e )
		{
			respondWithError();
		}
	}

	private void respondWithError()
	{
		try
		{
			File resource = new File( config.getResourcePath() + "/404.html" );
			setStatusCode( HttpStatusCode.NOT_FOUND );
			setBody( getResourceAsBytes( new FileInputStream( resource ), resource.length() ) );
			setContentTypeHeader( resource.getPath() );
		}
		catch ( IOException e )
		{
			this.log.writeLog( LogLevels.ERROR, "Error while setting 404 page as body: " + e.getMessage() );
		}
	}
}