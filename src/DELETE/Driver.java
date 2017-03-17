package DELETE;

import com.cs.config.CSConfig;
import com.cs.logs.CSLogger;
import com.cs.server.HttpServer;

public class Driver
{
	private static CSConfig config;
	private static CSLogger log;
	private static HttpServer server;
	
	public static void main( String[] args )
	{
		createConfig();
		createLog();
		createHttpServer();
	}
	
	private static void createConfig()
	{
		config = new CSConfig( System.getProperty( "user.dir" ) + "/config.xml" );
		config.initialize();
	}
	
	private static void createLog()
	{
		log = new CSLogger( config );
		log.initialize();
	}
	
	private static void createHttpServer()
	{
		server = new HttpServer( config, log );
		server.start();
	}
}