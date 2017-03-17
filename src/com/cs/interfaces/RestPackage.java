package com.cs.interfaces;

public interface RestPackage
{
	public void initializeDefaults();
	
	// public set methods
	public void setConfig( Config configuration );
	public void setLogger( Logger logger );
	public void setConnect( RequestHttpConnect connectHandler );
	public void setDelete( RequestHttpDelete deleteHandler );
	public void setGet( RequestHttpGet getHandler );
	public void setHead( RequestHttpHead headHandler );
	public void setOptions( RequestHttpOptions optionsHandler );
	public void setPost( RequestHttpPost postHandler );
	public void setPut( RequestHttpPut putHandler );

	// Public get methods
	public RequestHttpConnect getConnect();
	public RequestHttpDelete getDelete();
	public RequestHttpGet getGet();
	public RequestHttpHead getHead();
	public RequestHttpOptions getOptions();
	public RequestHttpPost getPost();
	public RequestHttpPut getPut();
}