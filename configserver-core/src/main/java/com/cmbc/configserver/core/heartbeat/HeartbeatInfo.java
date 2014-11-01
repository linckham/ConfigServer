package com.cmbc.configserver.core.heartbeat;

import io.netty.channel.Channel;

public class HeartbeatInfo {
	public static long SYNC_DB_INTERVAL = 10 * 1000;
	public static long TIMEOUT = 30 * 1000;
	
	private String clientId;
	private Channel channel;
	private long lastUpdateMillis;
	private long lastDBSyncMillis;
	
	public HeartbeatInfo(String clientId,Channel channel,long lastUpdated){
		this.clientId = clientId;
		this.channel = channel;
		this.lastUpdateMillis = lastUpdated;
	}
	
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public long getLastUpdateMillis() {
		return lastUpdateMillis;
	}
	public void setLastUpdateMillis(long lastUpdateMillis) {
		this.lastUpdateMillis = lastUpdateMillis;
	}
	public long getLastDBSyncMillis() {
		return lastDBSyncMillis;
	}
	public void setLastDBSyncMillis(long lastDBSyncMillis) {
		this.lastDBSyncMillis = lastDBSyncMillis;
	}
	
	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
}
