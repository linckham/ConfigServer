package com.cmbc.configserver.core.heartbeat;

import io.netty.channel.Channel;

import com.cmbc.configserver.remoting.ChannelEventListener;

public class ClientConnectionListener implements ChannelEventListener{
    private HeartbeatService heartbeatService;

    public void setHeartbeatService(HeartbeatService heartbeatService) {
        this.heartbeatService = heartbeatService;
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
