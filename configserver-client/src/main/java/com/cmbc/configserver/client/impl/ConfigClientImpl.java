package com.cmbc.configserver.client.impl;

import com.cmbc.configserver.client.ConfigClient;
import com.cmbc.configserver.client.ResourceListener;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.remoting.RemotingClient;
import com.cmbc.configserver.remoting.netty.NettyClientConfig;
import com.cmbc.configserver.remoting.netty.NettyRemotingClient;

public class ConfigClientImpl implements ConfigClient {
	private final RemotingClient remotingClient;
	private final ClientRemotingProcessor clientRemotingProcessor;
	 
	public ConfigClientImpl(final NettyClientConfig nettyClientConfig) {
		this.remotingClient = new NettyRemotingClient(nettyClientConfig);
		this.clientRemotingProcessor = new ClientRemotingProcessor();
		//TODO register processor?
	}

	@Override
	public boolean publish(Configuration config) {
		// TODO Auto-generated method stub
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
