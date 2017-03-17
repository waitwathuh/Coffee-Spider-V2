package com.cs.interfaces;

import com.cs.http.BasicHttpResponse;

public interface RequestHttpGet
{
	public void setConfig( Config configuration );
	public void setLogger( Logger logger );
	
	public BasicHttpResponse processRequest( HttpRequest request );
}