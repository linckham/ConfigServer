package com.cmbc.configserver.remoting;

import java.util.concurrent.ExecutorService;

import com.cmbc.configserver.remoting.common.RequestProcessor;
import com.cmbc.configserver.remoting.exception.RemotingConnectException;
import com.cmbc.configserver.remoting.exception.RemotingSendRequestException;
import com.cmbc.configserver.remoting.exception.RemotingTimeoutException;
import com.cmbc.configserver.remoting.exception.RemotingTooMuchRequestException;
import com.cmbc.configserver.remoting.protocol.RemotingCommand;

/**
 * the client of remote communicating
 * 
 * @author tongchuan.lin<linckham@gmail.com>
 * @since 2014/10/17 3:01:22PM
 */
public interface RemotingClient extends RemotingService {
	public RemotingCommand invokeSync(final String addr,
			final RemotingCommand request, final long timeoutMillis)
			throws InterruptedException, RemotingConnectException,
			RemotingSendRequestException, RemotingTimeoutException;

	public void invokeAsync(final String addr, final RemotingCommand request,
			final long timeoutMillis, final InvokeCallback invokeCallback)
			throws InterruptedException, RemotingConnectException,
			RemotingTooMuchRequestException, RemotingTimeoutException,
			RemotingSendRequestException;

	public void invokeOneway(final String addr, final RemotingCommand request,
			final long timeoutMillis) throws InterruptedException,
			RemotingConnectException, RemotingTooMuchRequestException,
			RemotingTimeoutException, RemotingSendRequestException;

	public void registerProcessor(final int requestCode,
			final RequestProcessor processor, final ExecutorService executor);
}