package com.cmbc.configserver.client.impl;

import io.netty.channel.Channel;

import java.util.Date;

import com.cmbc.configserver.remoting.ChannelEventListener;

public class RemotingChannelListener implements ChannelEventListener{
	
	private ConfigClientImpl clientImpl;
	
	public RemotingChannelListener(ConfigClientImpl clientImpl){
		this.clientImpl = clientImpl;
	}

	@Override
	public void onChannelConnect(String remoteAddress, Channel channel) {
		//System.out.println("connected "+ new Date());
	}

	@Override
	public void onChannelClose(String remoteAddress, Channel channel) {
		// TODO Auto-generated method stub
		//System.out.println("close "+ new Date());
	}

	@Override
	public void onChannelException(String remoteAddres, Channel channel) {
		// TODO Auto-generated method stub
		//System.out.println("exception "+ new Date());
	}

	@Override
	public void onChannelIdle(String remoteAddress, Channel channel) {
		clientImpl.sendHeartbeat(channel);
	}

}
