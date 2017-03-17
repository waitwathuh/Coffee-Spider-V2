package com.cs.interfaces;

public interface Config
{
	// Public methods
	public void initialize();
	public boolean validateConfig();

	// Getters
	public String getServerType();
	public int getServerPort();
	public int getMaxThreads();
	public int getConnectionTimeOut();
	public String keyFilePath();
	public String keyStorePassword1();
	public String keyStorePassword2();
	public String getLogPath();
	public String getLogFileName();
	public String getLogDateFormat();
	public String getLogLevel();
	public String getResourcePath();

	// Setters
	public void setConfigLocation( String fileLocation );
	public void setLogFileName( String fileName );
}