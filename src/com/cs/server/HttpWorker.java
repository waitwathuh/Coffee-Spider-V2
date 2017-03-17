package com.cs.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.concurrent.Callable;

import com.cs.annotations.AnnotationFinder;
import com.cs.enums.HttpMethod;
import com.cs.enums.HttpStatusCode;
import com.cs.enums.LogLevels;
import com.cs.http.BasicHttpRequest;
import com.cs.http.BasicHttpResponse;
import com.cs.http.Http;
import com.cs.http.HttpVersion;
import com.cs.interfaces.Config;
import com.cs.interfaces.Logger;

public class HttpWorker implements Callable< Void >
{
	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	private BasicHttpRequest request;
	private BasicHttpResponse response;
	private Logger log;
	private Config config;

	public HttpWorker( Socket httpSocket, Logger logger, Config configuration ) throws IOException
	{
		log = logger;
		config = configuration;
		socket = httpSocket;
		inputStream = socket.getInputStream();
		outputStream = socket.getOutputStream();
		request = new BasicHttpRequest( log );
		request.setHeaders( new HashMap< String, String >() );
		response = new BasicHttpResponse( log, config );
	}

	@Override
	public Void call()
	{
		Thread.currentThread().setName( "ProcessingHttpRequest" );
		parseRequest();
		processRequestMethod();
		setResponseHttpVersion();
		sendResponseAndCloseConnection();
		return null;
	}

	private void parseRequest()
	{
		try
		{
			getRequestDetails( readLineFromInputStream() );
			getRequestHeaders( inputStream );
			getRequestBody( inputStream );
		}
		catch ( SocketTimeoutException e ) {}
		catch ( IOException e )
		{
			log.writeLog( LogLevels.ERROR, "Error while reading header from inputStream: " + e.getMessage() );
		}
	}

	private String readLineFromInputStream() throws IOException, SocketTimeoutException
	{
		StringBuffer result = new StringBuffer();
		boolean crRead = false;
		int n;

		while ( ( n = inputStream.read() ) != -1 )
		{
			if ( n == '\r' )
			{
				crRead = true;
				continue;
			}
			else if ( n == '\n' && crRead )
			{
				return result.toString();
			}
			else
			{
				result.append( ( char ) n );
			}
		}

		return result.toString();
	}

	private void getRequestDetails( String headerFirstLine )
	{
		request.setRequestUri( headerFirstLine.split( " ", 3 )[ 1 ] );
		request.setHttpMethod( HttpMethod.extractMethod( headerFirstLine ) );
		request.setHttpVersion( HttpVersion.extractVersion( headerFirstLine ) );
	}

	private void getRequestHeaders( InputStream inputStream )
	{
		try
		{
			String nextLine = "";
			while ( ( nextLine = readLineFromInputStream() ).equals( "" ) == false )
			{
				String values[] = nextLine.split( ":", 2 );
				request.addHeader( values[ 0 ], values[ 1 ].trim() );
			}
		}
		catch ( IOException e )
		{
			log.writeLog( LogLevels.ERROR, "Error while reading request headers: " + e.getMessage() );
		}
	}

	private void getRequestBody( InputStream inputStream )
	{
		try
		{
			if ( request.getHeaders().get( Http.CONTENT_LENGTH ) != null )
			{
				int contentSize = Integer.parseInt( request.getHeaders().get( Http.CONTENT_LENGTH ) );
				request.setBody( processBodyStream( contentSize, inputStream ) );
			}
		}
		catch ( IOException e )
		{
			log.writeLog( LogLevels.ERROR, "Error while reading request body: " + e.getMessage() );
		}
	}

	private byte[] processBodyStream( int contentSize, InputStream inputStream ) throws IOException
	{
		byte[] data = new byte[ contentSize ];
		int n;

		for ( int i = 0; i < contentSize && ( n = inputStream.read() ) != -1; i++ )
		{
			data[ i ] = ( byte ) n;
		}

		return data;
	}

	private void processRequestMethod()
	{
		try
		{
			AnnotationFinder af = new AnnotationFinder();
			Method method = af.getMethod( request.getRequestUri(), request.getHttpMethod().name() );

			Class< ? > base = Class.forName( method.getDeclaringClass().getName() );
			Object objectInstance = base.newInstance();

			method.invoke( objectInstance, request, response, config, log );
		}
		catch ( NoSuchMethodException e )
		{
			handleResourse();
		}
		catch ( ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException e )
		{
			e.printStackTrace(); // TODO - Remove this
			log.writeLog( LogLevels.WARNING, "Error while closing connection: " + e.getMessage() );
		}
	}

	private void setResponseHttpVersion()
	{
		response.setHttpVersion( request.getHttpVersion() );
	}

	private void sendResponseAndCloseConnection()
	{
		try
		{
			addCloseHeader();
			addContentLengthHeader();
			sendResponse();
			socket.close();
		}
		catch ( IOException e )
		{
			log.writeLog( LogLevels.WARNING, "Error while sending response: " + e.getMessage() );
		}
	}

	private void addCloseHeader()
	{
		response.getHeaders().put( "Connection", "close" );
	}

	private void addContentLengthHeader()
	{
		if ( resposeHasBody() )
		{
			response.getHeaders().put( Http.CONTENT_LENGTH, "" + response.getBody().length );
		}
		else
		{
			response.getHeaders().put( Http.CONTENT_LENGTH, Integer.toString( 0 ) );
		}
	}

	private boolean resposeHasBody()
	{
		return response.getBody() != null && response.getBody().length > 0;
	}

	private void sendResponse() throws IOException
	{
		BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( outputStream ) );

		writer.write( response.getHttpVersion().toString() );
		writer.write( " " );
		writer.write( Integer.toString( response.getStatusCode().getCode() ) );
		writer.write( " " );
		writer.write( response.getStatusCode().getReasonPhrase() );
		writer.write( Http.CRLF );

		for ( String key : response.getHeaders().keySet() )
		{
			writer.write( key + ": " + response.getHeaders().get( key ) + Http.CRLF );
		}

		writer.write( Http.CRLF );
		writer.flush();

		if ( response.getBody() != null && response.getBody().length > 0 )
		{
			outputStream.write( response.getBody() );
		}

		outputStream.flush();
	}

	private void handleResourse()
	{
		if ( resourseFileExist() )
		{
			respondWithResource( this.config.getResourcePath() + request.getRequestUri() );
		}
		else
		{
			respondWithError( request.getRequestUri() );
		}
	}

	private boolean resourseFileExist()
	{
		return new File( this.config.getResourcePath() + request.getRequestUri() ).exists();
	}

	private void respondWithResource( String resourceLocation )
	{
		this.response.setStatusCode( HttpStatusCode.OK );
		this.response.setBody( new File( resourceLocation ) );
	}

	private void respondWithError( String requestURL )
	{
		this.log.writeLog( LogLevels.WARNING, "There is no GET request for: " + requestURL );
		this.response.setStatusCode( HttpStatusCode.NOT_FOUND );
		this.response.setBody( new File( this.config.getResourcePath() + "/404.html" ) );
	}
}