package com.cmbc.configserver.remoting.exception;

public class RemotingTimeoutException extends RemotingException {

	private static final long serialVersionUID = 4106899185095245979L;

	public RemotingTimeoutException(String message) {
		super(message);
	}

	public RemotingTimeoutException(String address, long timeoutMillis) {
		this(address, timeoutMillis, null);
	}

	public RemotingTimeoutException(String address, long timeoutMillis,
			Throwable cause) {
		super("wait response on the channel <" + address + "> timeout, "
				+ timeoutMillis + "(ms)", cause);
	}
}