package com.cmbc.configserver.remoting;

import io.netty.channel.Channel;

public interface ChannelEventListener {
	public void onChannelConnect(final String remoteAddress,
			final Channel channel);

	public void onChannelClose(final String remoteAddress, final Channel channel);

	public void onChannelException(final String remoteAddres,
			final Channel channel);

	public void onChannelIdle(final String remoteAddress, final Channel channel);
}