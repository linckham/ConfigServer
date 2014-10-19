package com.cmbc.configserver.remoting.netty;

public class NettyClientConfig {
	// 处理Server Response/Request
	private int clientWorkerThreads = 4;
	private int clientCallbackExecutorThreads = Runtime.getRuntime()
			.availableProcessors();
	private int clientOnewaySemaphoreValue = 256;
	private int clientAsyncSemaphoreValue = 128;
	private long connectTimeoutMillis = 3000;
	// channel超过1分钟不被访问 就关闭
	private long channelNotActiveInterval = 1000 * 60;

	private int clientChannelMaxIdleTimeSeconds = 120;

	public int getClientWorkerThreads() {
		return clientWorkerThreads;
	}

	public void setClientWorkerThreads(int clientWorkerThreads) {
		this.clientWorkerThreads = clientWorkerThreads;
	}

	public int getClientOnewaySemaphoreValue() {
		return clientOnewaySemaphoreValue;
	}

	public void setClientOnewaySemaphoreValue(int clientOnewaySemaphoreValue) {
		this.clientOnewaySemaphoreValue = clientOnewaySemaphoreValue;
	}

	public long getConnectTimeoutMillis() {
		return connectTimeoutMillis;
	}

	public void setConnectTimeoutMillis(long connectTimeoutMillis) {
		this.connectTimeoutMillis = connectTimeoutMillis;
	}

	public int getClientCallbackExecutorThreads() {
		return clientCallbackExecutorThreads;
	}

	public void setClientCallbackExecutorThreads(
			int clientCallbackExecutorThreads) {
		this.clientCallbackExecutorThreads = clientCallbackExecutorThreads;
	}

	public long getChannelNotActiveInterval() {
		return channelNotActiveInterval;
	}

	public void setChannelNotActiveInterval(long channelNotActiveInterval) {
		this.channelNotActiveInterval = channelNotActiveInterval;
	}

	public int getClientAsyncSemaphoreValue() {
		return clientAsyncSemaphoreValue;
	}

	public void setClientAsyncSemaphoreValue(int clientAsyncSemaphoreValue) {
		this.clientAsyncSemaphoreValue = clientAsyncSemaphoreValue;
	}

	public int getClientChannelMaxIdleTimeSeconds() {
		return clientChannelMaxIdleTimeSeconds;
	}

	public void setClientChannelMaxIdleTimeSeconds(
			int clientChannelMaxIdleTimeSeconds) {
		this.clientChannelMaxIdleTimeSeconds = clientChannelMaxIdleTimeSeconds;
	}
}