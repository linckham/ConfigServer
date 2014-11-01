package com.cmbc.configserver.core.heartbeat;

import io.netty.channel.Channel;

import com.cmbc.configserver.core.server.ConfigServerController;
import com.cmbc.configserver.remoting.ChannelEventListener;

public class ClientConnectionListener implements ChannelEventListener{
	private ConfigServerController configServerController;
	//TODO
	private HeartbeatService heartbeatService;
	
	public ClientConnectionListener(ConfigServerController configServerController){
		this.configServerController = configServerController;
	}

	@Override
	public void onChannelConnect(String remoteAddress, Channel channel) {
	}
	
	@Override
	public void onChannelActive(Channel channel) {
		//add channel info
		heartbeatService.channelCreated(channel);
	}

	@Override
	public void onChannelClose(String remoteAddress, Channel channel) {
		heartbeatService.clearChannel(channel);
	}

	@Override
	public void onChannelException(String remoteAddres, Channel channel) {
		heartbeatService.clearChannel(channel);
	}

	@Override
	public void onChannelIdle(String remoteAddress, Channel channel) {
		heartbeatService.clearChannel(channel);
	}

	

}
