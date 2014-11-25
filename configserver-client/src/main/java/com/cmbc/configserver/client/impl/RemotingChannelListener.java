package com.cmbc.configserver.client.impl;

import com.cmbc.configserver.remoting.ChannelEventListener;
import com.cmbc.configserver.remoting.netty.NettyEvent;

public class RemotingChannelListener implements ChannelEventListener{
	
	private ConfigClientImpl clientImpl;
	
	public RemotingChannelListener(ConfigClientImpl clientImpl){
		this.clientImpl = clientImpl;
	}

	@Override
	public void onChannelConnect(final NettyEvent event) {
	}

	@Override
	public void onChannelClose(final NettyEvent event) {
		clientImpl.clear(event.getChannel());
	}

	@Override
	public void onChannelException(final NettyEvent event) {
		clientImpl.clear(event.getChannel());
	}

	@Override
	public void onChannelIdle(final NettyEvent event) {
		clientImpl.sendHeartbeat(event.getChannel());
	}

	@Override
	public void onChannelActive(final NettyEvent event) {
	}

}
