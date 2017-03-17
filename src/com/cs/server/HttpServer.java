package com.cs.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.cs.enums.LogLevels;
import com.cs.interfaces.Config;
import com.cs.interfaces.Logger;
import com.cs.interfaces.Server;

public class HttpServer implements Server
{
	private ServerSocket serverSocket;
	private ExecutorService workerPool;
	private ExecutorService dispatcherService;
	private Config configuration;
	private Logger logger;

	private volatile boolean RUNNING = true;

	public HttpServer( Config config, Logger log )
	{
		try
		{
			configuration = config;
			logger = log;
			serverSocket = new ServerSocket( config.getServerPort() );
			workerPool = Executors.newFixedThreadPool( config.getMaxThreads() );
			dispatcherService = Executors.newSingleThreadExecutor();
		}
		catch ( IOException e )
		{
			log.writeLog( LogLevels.ERROR, e.getMessage() );
			throw new RuntimeException( "Error while starting server", e );
		}
	}

	@Override
	public void start()
	{
		dispatcherService.submit( new ConnectionListener() );
		logger.writeLog( LogLevels.LOG, "Webserver started: http://localhost:" + configuration.getServerPort() );
	}

	@Override
	public void stop()
	{
		try
		{
			RUNNING = false;
			dispatcherService.shutdown();
			workerPool.shutdown();
			serverSocket.close();
		}
		catch ( IOException e )
		{
			logger.writeLog( LogLevels.WARNING, "Error while shutting down the server: " + e.getMessage() );
		}
		finally
		{
			logger.writeLog( LogLevels.LOG, "The server has been shut down successfully." );
		}
	}

	@Override
	public void dispatchRequest( Socket socket )
	{
		try
		{
			socket.setSoTimeout( configuration.getConnectionTimeOut() );
			workerPool.submit( new HttpWorker( socket, logger, configuration ) );
		}
		catch ( SocketException e )
		{
			logger.writeLog( LogLevels.ERROR, "Error while setting timeout to socket: " + e.getMessage() );
		}
		catch ( IOException e )
		{
			logger.writeLog( LogLevels.ERROR, "Error while getting IO stream from socket: " + e.getMessage() );
		}
	}

	// Private inner class
	private class ConnectionListener implements Runnable
	{
		@Override
		public void run()
		{
			while ( RUNNING )
			{
				Thread.currentThread().setName( "SocketConnectionListener" );
				listen();
			}
		}

		private void listen()
		{
			try
			{
				dispatchRequest( serverSocket.accept() );
			}
			catch ( IOException e )
			{
				logger.writeLog( LogLevels.ERROR, "Error while accepting a connection: " + e.getMessage() );
			}
		}
	}
}