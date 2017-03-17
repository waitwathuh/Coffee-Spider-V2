package com.cs.interfaces;

import com.cs.http.BasicHttpResponse;

public interface RequestHttpConnect
{
	BasicHttpResponse processRequest( HttpRequest request );
}