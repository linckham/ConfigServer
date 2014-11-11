package com.cmbc.configserver.core.heartbeat;

import com.cmbc.configserver.remoting.common.RemotingUtil;
import com.cmbc.configserver.remoting.netty.NettyServerConfig;
import com.cmbc.configserver.utils.ConfigServerLogger;
import io.netty.channel.Channel;

import com.cmbc.configserver.remoting.ChannelEventListener;

import java.util.concurrent.atomic.AtomicInteger;

public class ClientConnectionListener implements ChannelEventListener{
    private HeartbeatService heartbeatService;
    private NettyServerConfig nettyServerConfig;
    /**
     * the total connection number of the config server
     */
    private AtomicInteger totalConnectionNumber = new AtomicInteger(0);

    public void setHeartbeatService(HeartbeatService heartbeatService) {
        this.heartbeatService = heartbeatService;
    }

    public void setNettyServerConfig(NettyServerConfig nettyServerConfig) {
        this.nettyServerConfig = nettyServerConfig;
    }

    public NettyServerConfig getNettyServerConfig() {
        return nettyServerConfig;
    }

    @Override
    public void onChannelConnect(String remoteAddress, Channel channel) {
    }

    @Override
	public void onChannelActive(Channel channel) {
		//add channel info
		heartbeatService.channelCreated(channel);
        if (totalConnectionNumber.get() > this.getNettyServerConfig().getServerMaxConnectionNumbers()) {
            //close the channel
            RemotingUtil.closeChannel(channel);
            //throw an runtime exception
            throw new RuntimeException(String.format("channel connection exceeds the max_connection_number [%s]. close the channel [%s]", this.getNettyServerConfig().getServerMaxConnectionNumbers(), channel));

        }
        int connectionCount = this.totalConnectionNumber.incrementAndGet();
        ConfigServerLogger.info(String.format("channel[%s] is connected to server. the total connection count is %s", channel, connectionCount));
	}

	@Override
	public void onChannelClose(String remoteAddress, Channel channel) {
		heartbeatService.clearChannel(channel);
        this.decrementConnection(channel);
	}

	@Override
	public void onChannelException(String remoteAddress, Channel channel) {
		heartbeatService.clearChannel(channel);
        this.decrementConnection(channel);
	}

	@Override
	public void onChannelIdle(String remoteAddress, Channel channel) {
        //TODO: why on channel idle, we should close the channel????
		heartbeatService.clearChannel(channel);
        this.decrementConnection(channel);
	}

    private void decrementConnection(Channel channel){
        int connectionCount = this.totalConnectionNumber.decrementAndGet();
        //reset the connection number
        if(this.totalConnectionNumber.get() < 0 ){
            this.totalConnectionNumber.set(0);
        }
        ConfigServerLogger.info(String.format("channel[%s] is closed from server. the total connection count is %s", channel, connectionCount));
    }
	

}
