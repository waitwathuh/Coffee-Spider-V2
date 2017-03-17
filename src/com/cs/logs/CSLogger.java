package com.cs.logs;

import com.cs.interfaces.Config;
import com.cs.interfaces.Logger;
import com.cs.enums.LogLevels;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CSLogger implements Logger
{
	private Config config;
	private LogLevels logLevel;
	private String logFilePath;
	private SimpleDateFormat logDateFormat;
	private final String NEWLINESPACE = "\n\t\t";

	public CSLogger( Config configFile )
	{
		config = configFile;
	}

	public void initialize()
	{
		logFilePath = config.getLogPath() + System.getProperty( "file.separator" ) + config.getLogFileName();
		logLevel = LogLevels.valueOf( config.getLogLevel() );
		logDateFormat = new SimpleDateFormat( config.getLogDateFormat() );
	}

	public void writeLog( LogLevels logType, String message )
	{
		if ( logLevel.getCode() >= logType.getCode() )
		{
			String formatMessage = formatLogString( logType.getReasonPhrase(), message );
			WriteToLog( formatMessage );
		}
	}

	private String formatLogString( String logType, String message )
	{
		String logLine = logDateFormat.format( new Date() ) + "\t[" + logType.toUpperCase() + "]";
		return logLine + NEWLINESPACE + getTrace() + NEWLINESPACE + message;
	}

	private void WriteToLog( String data )
	{
		PrintWriter out = null;

		try
		{
			out = new PrintWriter( new BufferedWriter( new FileWriter( logFilePath, true ) ) );
			out.println( data );
		}
		catch ( IOException e )
		{
			System.out.println( "ERROR: IO Exception." );
			System.out.println( "\t" + e.getMessage() );
			System.exit( 2 );
		}
		finally
		{
			if ( out != null )
			{
				out.close();
			}
		}
	}

	private String getTrace()
	{
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		String callingMethod = ste[5].getFileName() + "_" + ste[5].getClassName() + "_" + ste[5].getMethodName() + "_" + ste[5].getLineNumber();
		String currentMethod = ste[4].getFileName() + "_" + ste[4].getClassName() + "_" + ste[4].getMethodName() + "_" + ste[4].getLineNumber();
		return callingMethod + NEWLINESPACE + currentMethod;
	}

	// Public setters
	public void setConfig( Config configFile )
	{
		config = configFile;
	}
}