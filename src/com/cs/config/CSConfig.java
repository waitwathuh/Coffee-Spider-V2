package com.cs.config;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.cs.interfaces.Config;

public class CSConfig implements Config
{
	// Web Server
	private String confLoc;
	private String serverType;
	private String serverPort;
	private String threadCount;
	private String connectionTimeOut;
	private String keyFile;
	private String password1;
	private String password2;
	private boolean	folderCreation = true;
	private boolean	defaultWebSettings = false;

	// Log files
	private String logPath;
	private String logFile;
	private String logDateFormat;
	private String logLevel;
	private boolean	defaultLogSettings = false;

	// Web resources
	private String resourcePath;

	// Database settings
	private String dbIP;
	private String dbPort;
	private String dbName;
	private String dbUser;
	private String dbPass;

	public CSConfig( String fileLocation )
	{
		confLoc = fileLocation;
	}

	/**
	 * Initialize the config controller.
	 * 
	 * @param confLoc Full path to the config file
	 */
	public void initialize()
	{
		readConfig();
		processDefaults();
		setLogFileName();
		createFolders();
	}

	/**
	 * Validates the config file.
	 * <br><br>
	 * The function will return false if there is an error in the config file
	 * and true if the config file is valid.
	 * 
	 * @return Boolean value whether the config file is valid or not.
	 */
	public boolean validateConfig()
	{
		if ( serverType.toLowerCase().equals( "http" ) == false && serverType.toLowerCase().equals( "https" ) == false )
		{
			System.out.println( "ERROR: Invalid server type. \n\t Only http and https is accepted values." );
			return false;
		}
		
		try
		{
			if ( Integer.parseInt( serverPort ) < 1 )
			{
				System.out.println( "ERROR: Invalid server port. \n\t Port numbers can not be less than 1." );
				return false;
			}
			else if ( Integer.parseInt( serverPort ) > 65535 )
			{
				System.out.println( "ERROR: Invalid server port. \n\t Port numbers can not be more than 65,535." );
				return false;
			}
		}
		catch ( NumberFormatException e )
		{
			System.out.println( "ERROR: Number Format Exception." );
			System.out.println( "\t" + e.getMessage() );
			System.exit( 3 );
		}

		if ( threadCount == null || threadCount.equals( "" ) || Integer.parseInt( serverPort ) <= 0 )
		{
			System.out.println( "ERROR: Invalid number for 'maxThreadCount'. This must be a numeric value more than 0." );
			return false;
		}

		if ( connectionTimeOut == null || connectionTimeOut.equals( "" ) || Integer.parseInt( connectionTimeOut ) < 0 )
		{
			System.out.println( "ERROR: Invalid number for 'connectionTimeOut'. This must be a numeric value of 0 or more." );
			return false;
		}

		if ( serverType.toLowerCase().equals( "https" ) && keyFile.equals( "" ) || keyFile == null )
		{
			System.out.println( "ERROR: Invalid HTTPS keyfile location. \n\t A full system path is needed for the HTTPS Certificate location." );
			return false;
		}
		
		if ( serverType.toLowerCase().equals( "https" ) && password1.equals( "" ) || password1 == null )
		{
			System.out.println( "ERROR: Invalid keystore password. \n\t A first password needs to be specified for the keystore file." );
			return false;
		}
		
		if ( serverType.toLowerCase().equals( "https" ) && password2.equals( "" ) || password2 == null )
		{
			System.out.println( "ERROR: Invalid keystore password. \n\t A second password needs to be specified for the keystore file." );
			return false;
		}
		
		if ( logPath == null || logPath.equals( "" ) )
		{
			System.out.println( "ERROR: Invalid system log path. \n\t A full system path is needed for system logs to be created." );
			return false;
		}
		else if ( new File(logPath).exists() == false )
		{
			System.out.println( "ERROR: Invalid system log path. \n\t The folder logs will be saved in, does not exist." );
			return false;
		}
		
		if ( logDateFormat == null || logDateFormat.equals( "" ) )
		{
			System.out.println( "ERROR: Invalid log date format. \n\t A valid date format is needed to dateTime stamp the system logs." );
			return false;
		}
		
		if ( logLevel.contains( "1" ) == false && logLevel.contains( "2" ) == false && logLevel.contains( "3" ) == false )
		{
			System.out.println( "ERROR: Invalid log level. \n\t Valid log level values are only '1', '2' and '3'. Or any combanation of these numbers." );
			return false;
		}
		
		if ( resourcePath == null || resourcePath.equals( "" ) )
		{
			System.out.println( "ERROR: Invalid resource path. \n\t A valid path is needed for the web resources." );
			return false;
		}
		
		return true;
	}

	/**
	 * Read the config file. 
	 * 
	 * @param confLoc Full path to config file
	 */
	private void readConfig()
	{
		try
		{
			File fXmlFile = new File( confLoc );
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			Document doc = dBuilder.parse( fXmlFile );
			doc.getDocumentElement().normalize();

			processWebSettings( doc );
			processLogSettings( doc );
			processResourceSettings( doc );
			processDatabaseSettings( doc );
		}
		catch ( ParserConfigurationException e )
		{
			System.out.println( "ERROR: Parser Configuration Exception." );
			System.out.println( e.getMessage() );
			System.exit( 2 );
		}
		catch ( SAXException e )
		{
			System.out.println( "ERROR: SAX Exception." );
			System.out.println( e.getMessage() );
			System.exit( 2 );
		}
		catch ( IOException e )
		{
			System.out.println( "ERROR: IO Exception." );
			System.out.println( e.getMessage() );
			System.exit( 2 );
		}
	}

	/**
	 * Use default settings for components specified in config file.
	 */
	private void processDefaults()
	{
		if ( defaultWebSettings == true )
		{
			serverType = "http";
			serverPort = "80";
			threadCount = "16";
			connectionTimeOut = "0";
			folderCreation = true;
		}
		
		if ( defaultLogSettings == true )
		{
			logPath = System.getProperty( "user.dir" ) + System.getProperty( "file.separator" ) + "logs";
			logDateFormat = "yyyy-MM-dd HH.mm.ss";
		}
		
		if ( logLevel.length() < 1 )
		{
			logLevel = "123";
		}
	}

	/**
	 * This method creates a log file name using the 'dateFormat' in the config.xml
	 * 
	 * @see https://docs.oracle.com/javase/tutorial/i18n/format/simpleDateFormat.html
	 */
	private void setLogFileName()
	{
		SimpleDateFormat sdfDate = new SimpleDateFormat( logDateFormat );
		Date now = new Date();
	    String strDate = sdfDate.format(now);
		
		logFile = strDate + ".log";
	}

	/**
	 * Creates application folders if they don't exist
	 */
	private void createFolders()
	{
		if ( folderCreation == true )
		{
			new File( logPath ).mkdir();
			new File( resourcePath ).mkdir();
		}
	}



	/**
	 * Refactors paths to allow keywords to be used in the config file.
	 * <br>
	 * <br>
	 * This method only allows the keyword "curdir" to be translated to the
	 * working directory of this application. The keyword is NOT case sensitive. 
	 * 
	 * @param line Path string that needs refactoring.
	 * @return Refactored path
	 */
	private String refactorConfigPathString( String line )
	{
		if ( line.toLowerCase().startsWith( "curdir" ) )
		{
			String curDir = line.substring( 0, 6 );
			return line.replace( curDir, System.getProperty( "user.dir" ) );
		}
		else
		{
			return line;
		}
	}

	/**
	 * Reads and returns a XML element string value.
	 * 
	 * @param eElement XML element.
	 * @param tagName Tag name as is specified in the config file.
	 * @param itemNumber Element number based on tag name.
	 * @return String found in the XML based on the tag name and item number.
	 */
	private String getXmlValue( Element eElement, String tagName, int itemNumber )
	{
		try
		{
			return eElement.getElementsByTagName( tagName ).item( itemNumber ).getTextContent();
		}
		catch ( Exception e )
		{
			// There is no value in the config file. Return an empty string
			return "";
		}
	}

	/**
	 * Process the web settings section of the config file.
	 * 
	 * @param doc XML document.
	 */
	private void processWebSettings( Document doc )
	{
		NodeList nList = doc.getElementsByTagName( "web" );
		Node node = nList.item( 0 );

		if ( node != null && node.getNodeType() == Node.ELEMENT_NODE )
		{
			Element eElement = ( Element ) node;
			
			serverType = getXmlValue( eElement, "type", 0 );
			serverPort = getXmlValue( eElement, "port", 0 );
			threadCount = getXmlValue( eElement, "maxThreadCount", 0 );
			connectionTimeOut = getXmlValue( eElement, "connectionTimeOut", 0 );
			keyFile = refactorConfigPathString( getXmlValue( eElement, "keyfile", 0 ) );
			password1 = getXmlValue( eElement, "password1", 0 );
			password2 = getXmlValue( eElement, "password2", 0 );
			folderCreation = Boolean.parseBoolean( getXmlValue( eElement, "createFolders", 0 ) );
			defaultWebSettings = Boolean.parseBoolean( getXmlValue( eElement, "defaultSettings", 0 ) );
		}
	}

	/**
	 * Process the log settings section of the config file.
	 * 
	 * @param doc XML document.
	 */
	private void processLogSettings( Document doc )
	{
		NodeList nList = doc.getElementsByTagName( "log" );
		Node node = nList.item( 0 );

		if ( node != null && node.getNodeType() == Node.ELEMENT_NODE )
		{
			Element eElement = ( Element ) node;

			logPath = refactorConfigPathString( getXmlValue( eElement, "path", 0 ) );
			logDateFormat = getXmlValue( eElement, "dateFormat", 0 );
			logLevel = getXmlValue( eElement, "logLevel", 0 );
			defaultLogSettings = Boolean.parseBoolean( getXmlValue( eElement, "defaultSettings", 0 ) );
		}
	}

	/**
	 * Process the web resource section of the config file.
	 * 
	 * @param doc XML document.
	 */
	private void processResourceSettings( Document doc )
	{
		NodeList nList = doc.getElementsByTagName( "resource" );
		Node node = nList.item( 0 );

		if ( node != null && node.getNodeType() == Node.ELEMENT_NODE )
		{
			Element eElement = ( Element ) node;

			resourcePath = refactorConfigPathString( getXmlValue( eElement, "path", 0 ) );
		}
	}

	/**
	 * Process the database section of the config file.
	 * 
	 * @param doc XML document.
	 */
	private void processDatabaseSettings( Document doc )
	{
		NodeList nList = doc.getElementsByTagName( "database" );
		Node node = nList.item( 0 );

		if ( node != null && node.getNodeType() == Node.ELEMENT_NODE )
		{
			Element eElement = ( Element ) node;
			
			dbIP = getXmlValue( eElement, "ip", 0 );
			dbPort = getXmlValue( eElement, "port", 0 );
			dbName = getXmlValue( eElement, "dbName", 0 );
			dbUser = getXmlValue( eElement, "userName", 0 );
			dbPass = getXmlValue( eElement, "password", 0 );
		}
	}



	// Public set methods
	@Override
	public void setConfigLocation( String fileLocation )
	{
		confLoc = fileLocation;
	}

	@Override
	public void setLogFileName( String fileName )
	{
		logFile = fileName;
	}

	// Public get methods
	@Override
	public String getServerType()
	{
		return serverType.toLowerCase();
	}

	@Override
	public int getServerPort()
	{
		return Integer.parseInt( serverPort );
	}

	@Override
	public int getMaxThreads()
	{
		return Integer.parseInt( threadCount );
	}

	@Override
	public int getConnectionTimeOut()
	{
		return Integer.parseInt( connectionTimeOut );
	}

	@Override
	public String keyFilePath()
	{
		return keyFile;
	}

	@Override
	public String keyStorePassword1()
	{
		return password1;
	}

	@Override
	public String keyStorePassword2()
	{
		return password2;
	}

	@Override
	public String getLogPath()
	{
		return logPath;
	}

	@Override
	public String getLogFileName()
	{
		return logFile;
	}

	@Override
	public String getLogDateFormat()
	{
		return logDateFormat;
	}

	@Override
	public String getLogLevel()
	{
		return logLevel;
	}

	@Override
	public String getResourcePath()
	{
		return resourcePath;
	}

	public String getDbIP()
	{
		return dbIP;
	}

	public String getDbPort()
	{
		return dbPort;
	}

	public String getDbName()
	{
		return dbName.toLowerCase();
	}

	public String getDbUser()
	{
		return dbUser;
	}

	public String getDbPass()
	{
		return dbPass;
	}
}