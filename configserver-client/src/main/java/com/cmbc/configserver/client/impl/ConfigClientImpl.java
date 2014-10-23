package com.cmbc.configserver.client.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cmbc.configserver.client.ConfigClient;
import com.cmbc.configserver.client.ResourceListener;
import com.cmbc.configserver.common.RemotingSerializable;
import com.cmbc.configserver.common.protocol.RequestCode;
import com.cmbc.configserver.common.protocol.ResponseCode;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.remoting.netty.NettyClientConfig;
import com.cmbc.configserver.remoting.netty.NettyRemotingClient;
import com.cmbc.configserver.remoting.protocol.RemotingCommand;

public class ConfigClientImpl implements ConfigClient {
	private static final Logger logger = LoggerFactory.getLogger(ConfigClientImpl.class);
	private final NettyRemotingClient remotingClient;
	private final ClientRemotingProcessor clientRemotingProcessor;
	 
	public ConfigClientImpl(final NettyClientConfig nettyClientConfig,List<String> addrs){
		this.remotingClient = new NettyRemotingClient(nettyClientConfig);
		remotingClient.updateNameServerAddressList(addrs);
		this.clientRemotingProcessor = new ClientRemotingProcessor(this);
		//TODO register processor
		remotingClient.registerProcessor(RequestCode.PUSH_CONFIG, clientRemotingProcessor, null);
		
		remotingClient.start();
	}

	@Override
	public boolean publish(Configuration config) {
		RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.PUBLISH_CONFIG);
		byte[] body = RemotingSerializable.encode(config);
		request.setBody(body);
		try {
			//TODO timeout
			RemotingCommand result = remotingClient.invokeSync(null, request, 3000);
			logger.info(result.toString());
			if(result.getCode() != ResponseCode.PUBLISH_CONFIG_OK){
				return false;
			}
		} catch (Exception e) {
			logger.info(e.toString());
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
