package com.cs.interfaces;

import java.util.Map;

import com.cs.enums.HttpStatusCode;

/**
 * An interface for HTTP responses.
 */
public interface HttpResponse extends HttpMessage
{
	/**
	 * Set a list of key-value header fields.
	 */
	void setHeaders( Map< String, String > headers );

	/**
	 * Set the HTTP body as a byte array or null, if no entity is available.
	 */
	void setBody( byte[] body );

	/**
	 * Returns the HTTP Status Code of this response.
	 */
	HttpStatusCode getStatusCode();
}