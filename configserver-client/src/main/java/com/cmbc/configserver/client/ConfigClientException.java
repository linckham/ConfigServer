package com.cmbc.configserver.client;

public class ConfigClientException extends RuntimeException {
	private static final long serialVersionUID = 913629599944504678L;

	public ConfigClientException() {
		super();
	}

	public ConfigClientException(String message) {
		super(message);
	}

	public ConfigClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigClientException(Throwable cause) {
		super(cause);
	}
}
