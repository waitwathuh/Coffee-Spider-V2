package com.cs.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import com.cs.enums.LogLevels;
import com.cs.interfaces.Config;
import com.cs.interfaces.Logger;
import com.cs.interfaces.Server;

public class HttpsServer implements Server
{
	private ServerSocket serverSocket;
	private ExecutorService workerPool;
	private ExecutorService dispatcherService;
	private Config configuration;
	private Logger logger;

	private KeyStore keyStore;
	private KeyManagerFactory keyManagerFactory;
	private SSLContext sslContext;

	private volatile boolean RUNNING = true;

	public HttpsServer( Config config, Logger log )
	{
		this.configuration = config;
		this.logger = log;
		initializeKeyStore();
		initializeKeyManagerFactory();
		initializeSSLContext();
		initializeServer();
	}

	private void initializeKeyStore()
	{
		try
		{
			keyStore = KeyStore.getInstance( "JKS" );
			keyStore.load( new FileInputStream( configuration.keyFilePath() ), configuration.keyStorePassword1().toCharArray() );
		}
		catch ( KeyStoreException | CertificateException | NoSuchAlgorithmException e )
		{
			logger.writeLog( LogLevels.ERROR, "Error with SSL key: " + e.getMessage() );
			throw new RuntimeException( "Error while starting server", e );
		}
		catch ( IOException e )
		{
			logger.writeLog( LogLevels.ERROR, "Error while starting server: " + e.getMessage() );
			throw new RuntimeException( "Error while starting server", e );
		}
	}

	private void initializeKeyManagerFactory()
	{
		try
		{
			keyManagerFactory = KeyManagerFactory.getInstance( "SunX509" );
			keyManagerFactory.init( keyStore, configuration.keyStorePassword2().toCharArray() );
		}
		catch ( NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e )
		{
			logger.writeLog( LogLevels.ERROR, "Error with SSL key: " + e.getMessage() );
			throw new RuntimeException( "Error while starting server", e );
		}
	}

	private void initializeSSLContext()
	{
		try
		{
			sslContext = SSLContext.getInstance( "TLS" );
			sslContext.init( keyManagerFactory.getKeyManagers(), null, null );
		}
		catch ( NoSuchAlgorithmException | KeyManagementException e )
		{
			logger.writeLog( LogLevels.ERROR, "Error with SSL key: " + e.getMessage() );
			throw new RuntimeException( "Error while starting server", e );
		}
	}

	private void initializeServer()
	{
		try
		{
			this.serverSocket = ( SSLServerSocket ) sslContext.getServerSocketFactory().createServerSocket( configuration.getServerPort() );
			this.workerPool = Executors.newFixedThreadPool( configuration.getMaxThreads() );
			this.dispatcherService = Executors.newSingleThreadExecutor();
		}
		catch ( IOException e )
		{
			logger.writeLog( LogLevels.ERROR, "Error while starting server: " + e.getMessage() );
			throw new RuntimeException( "Error while starting server", e );
		}
	}

	@Override
	public void start()
	{
		dispatcherService.submit( new ConnectionListener() );
		logger.writeLog( LogLevels.LOG, "Webserver started: https://localhost:" + configuration.getServerPort() );
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
				listen();
			}
		}

		private void listen()
		{
			try
			{
				dispatchRequest( ( SSLSocket ) serverSocket.accept() );
			}
			catch ( IOException e )
			{
				logger.writeLog( LogLevels.ERROR, "Error while accepting a connection: " + e.getMessage() );
			}
		}
	}
}