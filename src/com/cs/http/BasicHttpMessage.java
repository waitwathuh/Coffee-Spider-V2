package com.cs.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import com.cs.enums.LogLevels;
import com.cs.interfaces.HttpMessage;
import com.cs.interfaces.Logger;

public class BasicHttpMessage implements HttpMessage
{
	private HttpVersion version;
	private byte[] body = null;
	private Map< String, String > headers;
	protected Logger log;

	public BasicHttpMessage( Logger logger )
	{
		log = logger;
	}

	@Override
	public HttpVersion getHttpVersion()
	{
		return version;
	}

	@Override
	public Map< String, String > getHeaders()
	{
		return headers;
	}

	@Override
	public byte[] getBody()
	{
		return body;
	}

	public void setHttpVersion( HttpVersion httpVersion )
	{
		version = httpVersion;
	}

	public void setHeaders( Map< String, String > headers )
	{
		this.headers = headers;
	}

	public void setBody( byte[] body )
	{
		this.body = body;
	}

	public void setBody( String body )
	{
		this.body = body.getBytes();
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
			log.writeLog( LogLevels.ERROR, "Error while setting index page as body: " + e.getMessage() );
		}
	}

	public void addHeader( String key, String value )
	{
		headers.put( key, value );
	}

	protected byte[] getResourceAsBytes( FileInputStream inputStream, long resourceSize ) throws IOException
	{
		byte fileContent[] = new byte[ ( int ) resourceSize ];
		inputStream.read( fileContent );
		inputStream.close();
		return fileContent;
	}

	protected void setContentTypeHeader( String resourcePath )
	{
		String extention = getExtension( resourcePath );
		String httpContentType = Http.getContentType( extention );
		getHeaders().put( Http.CONTENT_TYPE, httpContentType );
	}

	private String getExtension( String fName )
	{
		int extStart = fName.lastIndexOf( "." ) + 1;
		String fileExtention = fName.substring( extStart );

		return fileExtention;
	}
}