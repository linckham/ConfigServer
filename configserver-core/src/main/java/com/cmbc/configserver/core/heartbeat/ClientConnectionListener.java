package com.cmbc.configserver.core.heartbeat;

import com.cmbc.configserver.remoting.ChannelEventListener;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("clientConnectionListener")
public class ClientConnectionListener implements ChannelEventListener{
    @Autowired
    private HeartbeatService heartbeatService;

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
	public void onChannelException(String remoteAddress, Channel channel) {
		heartbeatService.clearChannel(channel);
	}

	@Override
	public void onChannelIdle(String remoteAddress, Channel channel) {
		heartbeatService.clearChannel(channel);
	}
}
