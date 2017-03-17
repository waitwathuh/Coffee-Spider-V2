package com.cs.interfaces;

import com.cs.http.BasicHttpResponse;

public interface RequestHttpPut
{
	BasicHttpResponse processRequest( HttpRequest request );
}