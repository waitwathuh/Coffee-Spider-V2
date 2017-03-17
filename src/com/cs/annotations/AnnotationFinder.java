package com.cs.annotations;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class AnnotationFinder
{
	private final String PACKAGE_NAME = "";

	private ClassLoader classLoader;
	private Enumeration< URL > resources;
	private List< File > dirs;
	private List< Class< ? > > classes;
	private List< Method > methodsWithRequestMappingAnnotation;

	public AnnotationFinder()
	{
		try
		{
			initializeObjects();
			getAllSubDirectories();
			getAllClassesInPackage();
			getAllClassesWithCustomAnnotation();
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
		catch ( URISyntaxException e )
		{
			e.printStackTrace();
		}
		catch ( ClassNotFoundException e )
		{
			e.printStackTrace();
		}
	}

	private void initializeObjects() throws IOException
	{
		classLoader = Thread.currentThread().getContextClassLoader();
		resources = classLoader.getResources( PACKAGE_NAME.replace( '.', '/' ) );
		dirs = new ArrayList< File >();
		classes = new ArrayList< Class< ? > >();
		methodsWithRequestMappingAnnotation = new ArrayList< Method >();
	}

	private void getAllSubDirectories() throws URISyntaxException
	{
		while ( resources.hasMoreElements() )
		{
			URL resource = resources.nextElement();
			URI uri = new URI( resource.toString() );

			dirs.add( new File( uri.getPath() ) );
		}
	}

	private void getAllClassesInPackage() throws ClassNotFoundException
	{
		for ( File directory : dirs )
		{
			findClassesInDirectory( directory, PACKAGE_NAME );
		}
	}

	private void findClassesInDirectory( File directory, String packageName )
	{
		try
		{
			validateDirectory( directory );
			loopThroughFilesInDirectory( directory.listFiles(), packageName );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
		catch ( ClassNotFoundException e )
		{
			e.printStackTrace();
		}
	}

	private void validateDirectory( File directory ) throws IOException
	{
		if ( directory.exists() == false )
		{
			throw new IOException();
		}
	}

	private void loopThroughFilesInDirectory( File[] filesInDirectory, String packageName ) throws ClassNotFoundException
	{
		for ( File file : filesInDirectory )
		{
			if ( file.isDirectory() )
			{
				processDirectory( file, packageName );
			}
			else if ( file.getName().endsWith( ".class" ) )
			{
				processFile( file, packageName );
			}
		}
	}

	private void processDirectory( File file, String packageName ) throws ClassNotFoundException
	{
		if ( packageName.equals( "" ) )
		{
			findClassesInDirectory( file, file.getName() );
		}
		else
		{
			findClassesInDirectory( file, packageName + "." + file.getName() );
		}
	}

	private void processFile( File file, String packageName ) throws ClassNotFoundException
	{
		if ( packageName.equals( "" ) )
		{
			String str = file.getName().substring( 0, file.getName().length() - 6 );
			classes.add( Class.forName( str ) );
		}
		else
		{
			String str = packageName + '.' + file.getName().substring( 0, file.getName().length() - 6 );
			classes.add( Class.forName( str ) );
		}
	}

	private void getAllClassesWithCustomAnnotation()
	{
		for ( Class< ? > singleClass : classes )
		{
			for ( Method method : singleClass.getMethods() )
			{
				if ( method.isAnnotationPresent( RequestMapping.class ) )
				{
					methodsWithRequestMappingAnnotation.add( method );
				}
			}
		}
	}

	public Method getMethod( String value, String requestMethod ) throws NoSuchMethodException
	{
		for ( Method method : methodsWithRequestMappingAnnotation )
		{
			RequestMapping requestMapping = ( RequestMapping ) method.getAnnotation( RequestMapping.class );

			if ( requestMapping.value().equals( value ) && requestMapping.method().equals( requestMethod ) )
			{
				return method;
			}
		}

		throw new NoSuchMethodException();
	}
}