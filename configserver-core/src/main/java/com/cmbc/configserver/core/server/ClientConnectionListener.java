package com.cmbc.configserver.core.server;

import com.cmbc.configserver.core.heartbeat.HeartbeatService;
import com.cmbc.configserver.remoting.ChannelEventListener;
import com.cmbc.configserver.remoting.netty.NettyEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("clientConnectionListener")
public class ClientConnectionListener implements ChannelEventListener {
	@Autowired
	private HeartbeatService heartbeatService;

	@Override
	public void onChannelConnect(final NettyEvent event) {
	}

	@Override
	public void onChannelActive(final NettyEvent event) {
		// add channel info
		if (null != event.getChannel() && event.getChannel().isActive()) {
			heartbeatService.channelCreated(event.getChannel());
		}
	}

	@Override
	public void onChannelClose(final NettyEvent event) {
		heartbeatService.clearChannel(event.getChannel());
	}

	@Override
	public void onChannelException(final NettyEvent event) {
		heartbeatService.clearChannel(event.getChannel());
	}

	@Override
	public void onChannelIdle(final NettyEvent event) {
		heartbeatService.clearChannel(event.getChannel());
	}
}
