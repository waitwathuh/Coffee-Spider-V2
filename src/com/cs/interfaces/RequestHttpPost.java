package com.cs.interfaces;

import com.cs.http.BasicHttpResponse;

public interface RequestHttpPost
{
	BasicHttpResponse processRequest( HttpRequest request );
}