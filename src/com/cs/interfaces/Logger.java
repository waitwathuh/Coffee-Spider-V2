package com.cs.interfaces;

import com.cs.enums.LogLevels;

public interface Logger
{
	public void writeLog( LogLevels logType, String message );
}