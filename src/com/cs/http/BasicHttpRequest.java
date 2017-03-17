package com.cs.http;

import com.cs.enums.HttpMethod;
import com.cs.interfaces.HttpRequest;
import com.cs.interfaces.Logger;

public class BasicHttpRequest extends BasicHttpMessage implements HttpRequest
{
	private HttpMethod method;
	private String requestUri;

	public BasicHttpRequest( Logger logger )
	{
		super( logger );
	}

	@Override
	public HttpMethod getHttpMethod()
	{
		return method;
	}

	@Override
	public String getRequestUri()
	{
		return requestUri;
	}

	public void setHttpMethod( HttpMethod method )
	{
		this.method = method;
	}
	
	public void setRequestUri( String requestUri )
	{
		this.requestUri = requestUri;
	}
}