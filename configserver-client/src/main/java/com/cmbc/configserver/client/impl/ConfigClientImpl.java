package com.cmbc.configserver.client.impl;

import java.util.List;

import com.cmbc.configserver.client.ConfigClient;
import com.cmbc.configserver.client.ResourceListener;
import com.cmbc.configserver.common.protocol.RequestCode;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.remoting.netty.NettyClientConfig;
import com.cmbc.configserver.remoting.netty.NettyRemotingClient;
import com.cmbc.configserver.remoting.protocol.RemotingCommand;
import com.cmbc.configserver.remoting.protocol.RemotingSerializable;

public class ConfigClientImpl implements ConfigClient {
	private final NettyRemotingClient remotingClient;
	private final ClientRemotingProcessor clientRemotingProcessor;
	 
	public ConfigClientImpl(final NettyClientConfig nettyClientConfig,List<String> addrs){
		this.remotingClient = new NettyRemotingClient(nettyClientConfig);
		remotingClient.updateNameServerAddressList(addrs);
		this.clientRemotingProcessor = new ClientRemotingProcessor(this);
		//TODO register processor
		remotingClient.registerProcessor(0, clientRemotingProcessor, null);
	}

	@Override
	public boolean publish(Configuration config) {
		RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.PUBLISH_CONFIG);
		byte[] body = RemotingSerializable.encode(config);
		request.setBody(body);
		try {
			remotingClient.invokeSync(null, request, 3000);
		} catch (Exception e) {
			// TODO 
			return false;
		} 
		
		return true;
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
