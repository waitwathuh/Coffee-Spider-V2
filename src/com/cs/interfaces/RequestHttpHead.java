package com.cs.interfaces;

import com.cs.http.BasicHttpResponse;

public interface RequestHttpHead
{
	BasicHttpResponse processRequest( HttpRequest request );
}