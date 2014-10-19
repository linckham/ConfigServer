package com.cmbc.configserver.remoting;

import com.cmbc.configserver.remoting.exception.RemotingCommandException;

public interface CommandCustomHeader {
	public void checkFields() throws RemotingCommandException;
}