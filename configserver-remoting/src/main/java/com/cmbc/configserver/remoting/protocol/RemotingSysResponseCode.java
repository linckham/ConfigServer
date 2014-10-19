package com.cmbc.configserver.remoting.protocol;

public class RemotingSysResponseCode {
	// Success
	public static final int SUCCESS = 0;
	// the uncaught exception has happened
	public static final int SYSTEM_ERROR = 1;
	// the system is busy because of the thread pool is overflow
	public static final int SYSTEM_BUSY = 2;
	// the request code doesn't support
	public static final int REQUEST_CODE_NOT_SUPPORTED = 3;
}