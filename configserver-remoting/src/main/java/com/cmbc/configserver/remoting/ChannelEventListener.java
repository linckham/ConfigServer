package com.cmbc.configserver.remoting;

import com.cmbc.configserver.remoting.netty.NettyEvent;

public interface ChannelEventListener {
	public void onChannelConnect(final NettyEvent event);

	public void onChannelClose(final NettyEvent event);

	public void onChannelException(final NettyEvent event);

	public void onChannelIdle(final NettyEvent event);
	
	public void onChannelActive(final NettyEvent event);
}