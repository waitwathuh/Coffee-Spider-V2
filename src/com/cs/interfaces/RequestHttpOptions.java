package com.cs.interfaces;

import com.cs.http.BasicHttpResponse;

public interface RequestHttpOptions
{
	BasicHttpResponse processRequest( HttpRequest request );
}