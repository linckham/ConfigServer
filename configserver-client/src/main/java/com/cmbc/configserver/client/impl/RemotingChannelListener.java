package com.cmbc.configserver.client.impl;

import io.netty.channel.Channel;

import com.cmbc.configserver.remoting.ChannelEventListener;

public class RemotingChannelListener implements ChannelEventListener{
	
	private ConfigClientImpl clientImpl;
	
	public RemotingChannelListener(ConfigClientImpl clientImpl){
		this.clientImpl = clientImpl;
	}

	@Override
	public void onChannelConnect(String remoteAddress, Channel channel) {
	}

	@Override
	public void onChannelClose(String remoteAddress, Channel channel) {
		clientImpl.clear(channel);
	}

	@Override
	public void onChannelException(String remoteAddres, Channel channel) {
		clientImpl.clear(channel);
	}

	@Override
	public void onChannelIdle(String remoteAddress, Channel channel) {
		clientImpl.sendHeartbeat(channel);
	}

	@Override
	public void onChannelActive(Channel channel) {
	}

}
