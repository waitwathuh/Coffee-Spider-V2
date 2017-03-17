package com.cs.interfaces;

import com.cs.http.BasicHttpResponse;

public interface RequestHttpDelete
{
	BasicHttpResponse processRequest( HttpRequest request );
}