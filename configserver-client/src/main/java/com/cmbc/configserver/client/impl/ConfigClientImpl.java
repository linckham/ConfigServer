package com.cmbc.configserver.client.impl;

import java.util.List;

import com.cmbc.configserver.client.ConfigClient;
import com.cmbc.configserver.client.ResourceListener;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.remoting.netty.NettyClientConfig;
import com.cmbc.configserver.remoting.netty.NettyRemotingClient;

public class ConfigClientImpl implements ConfigClient {
	private final NettyRemotingClient remotingClient;
	private final ClientRemotingProcessor clientRemotingProcessor;
	 
	public ConfigClientImpl(final NettyClientConfig nettyClientConfig,List<String> addrs){
		this.remotingClient = new NettyRemotingClient(nettyClientConfig);
		remotingClient.updateNameServerAddressList(addrs);
		this.clientRemotingProcessor = new ClientRemotingProcessor();
		//TODO register processor
		remotingClient.registerProcessor(0, clientRemotingProcessor, null);
	}

	@Override
	public boolean publish(Configuration config) {
		// TODO construct command,
		//remotingClient.invokeSync(addr, request, timeoutMillis);
		return false;
	}

	@Override
	public boolean unpublish(Configuration config) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean subscribe(Configuration config, ResourceListener listener) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean unsubscribe(Configuration config, ResourceListener listener) {
		// TODO Auto-generated method stub
		return false;
	}

}
