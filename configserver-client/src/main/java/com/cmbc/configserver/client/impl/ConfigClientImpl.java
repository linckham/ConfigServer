package com.cmbc.configserver.client.impl;

import com.cmbc.configserver.utils.Constants;
import io.netty.channel.Channel;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cmbc.configserver.client.ConfigClient;
import com.cmbc.configserver.client.ResourceListener;
import com.cmbc.configserver.common.RemotingSerializable;
import com.cmbc.configserver.common.protocol.RequestCode;
import com.cmbc.configserver.common.protocol.ResponseCode;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.domain.Notify;
import com.cmbc.configserver.remoting.ConnectionStateListener;
import com.cmbc.configserver.remoting.netty.NettyClientConfig;
import com.cmbc.configserver.remoting.netty.NettyRemotingClient;
import com.cmbc.configserver.remoting.protocol.RemotingCommand;
import com.cmbc.configserver.utils.ConcurrentHashSet;
import com.cmbc.configserver.utils.PathUtils;

public class ConfigClientImpl implements ConfigClient {
	private static final Logger logger = LoggerFactory.getLogger(ConfigClientImpl.class);
	private NettyRemotingClient remotingClient;
	private ClientRemotingProcessor clientRemotingProcessor;
	private ExecutorService publicExecutor;
	public Map<String,Set<ResourceListener>> subcribeMap = new ConcurrentHashMap<String,Set<ResourceListener>>();
	private final Lock subcribeMapLock = new ReentrantLock();
	public Map<String,Notify> notifyCache = new ConcurrentHashMap<String,Notify>();
	private AtomicInteger heartbeatFailedTimes = new AtomicInteger(0);

	public ConfigClientImpl(final NettyClientConfig nettyClientConfig,List<String> addrs,
								ConnectionStateListener stateListener) throws InterruptedException{
		this.remotingClient = new NettyRemotingClient(nettyClientConfig,new RemotingChannelListener(this));
		remotingClient.updateNameServerAddressList(addrs);
		this.clientRemotingProcessor = new ClientRemotingProcessor(this);
		remotingClient.registerProcessor(RequestCode.NOTIFY_CONFIG, clientRemotingProcessor, null);
		//start client
		remotingClient.start();
		remotingClient.setConnectionStateListener(stateListener);
		
		publicExecutor = remotingClient.getCallbackExecutor();
	}

	@Override
	public boolean publish(Configuration config) {
		RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.PUBLISH_CONFIG);
		byte[] body = RemotingSerializable.encode(config);
		request.setBody(body);
		try {
			RemotingCommand result = remotingClient.invokeSync(request, Constants.DEFAULT_SOCKET_READING_TIMEOUT);
			if(result.getCode() != ResponseCode.PUBLISH_CONFIG_OK){
				return false;
			}
		} catch (Exception e) {
			logger.info(e.toString());
			return false;
		} 
		
		return true;
	}

	@Override
	public boolean unpublish(Configuration config) {
		RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.UNPUBLISH_CONFIG);
		byte[] body = RemotingSerializable.encode(config);
		request.setBody(body);
		try {
			RemotingCommand result = remotingClient.invokeSync(request, Constants.DEFAULT_SOCKET_READING_TIMEOUT);
			if(result.getCode() != ResponseCode.UNPUBLISH_CONFIG_OK){
				return false;
			}
		} catch (Exception e) {
			logger.info(e.toString());
			return false;
		} 
		
		return true;
	}

	@Override
	public boolean subscribe(Configuration config, ResourceListener listener) {
		String subKey = PathUtils.getSubscriberPath(config);
		Set<ResourceListener> listeners =  subcribeMap.get(subKey);
		if (listeners == null || listeners.size() == 0) {
			try {
				if (subcribeMapLock.tryLock(Constants.DEFAULT_READ_WRITE_LOCK_TIMEOUT,TimeUnit.MILLISECONDS)) {
					try {
						listeners = subcribeMap.get(subKey);
						if (listeners == null || listeners.size() == 0) {
							if(listeners == null){
								listeners = new ConcurrentHashSet<ResourceListener>();
								subcribeMap.put(subKey,listeners);
							}

							RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.SUBSCRIBE_CONFIG);
							byte[] body = RemotingSerializable.encode(config);
							request.setBody(body);

							RemotingCommand response = remotingClient.invokeSync(request, Constants.DEFAULT_SOCKET_READING_TIMEOUT);

							if (response.getCode() != ResponseCode.SUBSCRIBE_CONFIG_OK) {
								return false;
							} else {
								if(response.getBody() != null){
									Notify notify = RemotingSerializable.decode(response.getBody(),Notify.class);;
									notifyCache.put(subKey,notify);
									listeners.add(listener);
									this.notifyListener(listener, notify);
								}else{
									logger.info("subscribe notify is null!");
									return false;
								}
							}

						} else {
							Notify notify = notifyCache.get(subKey);
							listeners.add(listener);
							this.notifyListener(listener, notify);
						}
					} catch (Exception e) {
						logger.info(e.toString());
						return false;
					} finally {
						subcribeMapLock.unlock();
					}
				}else{
					return false;
				}
			} catch (InterruptedException e) {
				logger.info(e.toString());
				return false;
			}
		} else {
			Notify notify = notifyCache.get(subKey);
			listeners.add(listener);
			this.notifyListener(listener, notify);
		}
		
		return true;
	}
	
	public void notifyListener(final ResourceListener listener,final Notify notify){
		this.publicExecutor.execute(new Runnable(){
			@Override
			public void run() {
				try{
					listener.notify(notify.getConfigLists());
				}catch(Exception e){
					logger.info(e.toString());
				}
			}
		});
	}

	@Override
	public boolean unsubscribe(Configuration config, ResourceListener listener) {
		String subKey = PathUtils.getSubscriberPath(config);
		Set<ResourceListener> listerners =  subcribeMap.get(subKey);
		if(listerners == null){
			return true;
		}
		
		listerners.remove(listerners);
		
		if(listerners.size() == 0){
			try {
				if (subcribeMapLock.tryLock(Constants.DEFAULT_READ_WRITE_LOCK_TIMEOUT,TimeUnit.MILLISECONDS)) {
					try {
						if (listerners.size() == 0) {
							RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.UNSUBSCRIBE_CONFIG);
							byte[] body = RemotingSerializable.encode(config);
							request.setBody(body);
							RemotingCommand result = remotingClient.invokeSync(request, Constants.DEFAULT_SOCKET_READING_TIMEOUT);

							if (result.getCode() != ResponseCode.UNSUBSCRIBE_CONFIG_OK) {
								return false;
							}else{
								//success unsubscribed,than remove cache
								notifyCache.remove(subKey);
							}
						}
					} catch (Exception e) {
						logger.info(e.toString());
						return false;
					} finally {
						subcribeMapLock.unlock();
					}
				}else{
					return false;
				}
			} catch (InterruptedException e) {
				logger.info(e.toString());
				return false;
			}
		}
		
		return true;
	}
	
	
	
	public void sendHeartbeat(Channel channel){
		int retryTime = 2;
		boolean sendSuccessed = false;
		for(int i= 0; i< retryTime; i++){
			RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.HEARTBEAT);
			request.setBody(null);
			try {
				RemotingCommand result = remotingClient.invokeSyncImpl(channel, request, Constants.DEFAULT_SOCKET_READING_TIMEOUT);
				if(result.getCode() == ResponseCode.HEARTBEAT_OK){
					sendSuccessed = true;
					logger.info("heartbeat successed!");
					break;
				}
			} catch (Exception e) {
				//do nothing
				logger.info(e.toString());
			} 
		}
		
		if(!sendSuccessed){
			int times = heartbeatFailedTimes.incrementAndGet();
			if(times >= 3){
				logger.info("heartbeat failed, do reset works");
				this.clear(channel);
			}
		}
	}
	
	public void clear(Channel channel){
		remotingClient.closeChannel(channel);
		this.heartbeatFailedTimes.set(0);
		this.subcribeMap.clear();
		this.notifyCache.clear();
	}
	
	public Map<String, Set<ResourceListener>> getSubcribeMap() {
		return subcribeMap;
	}

	public void setSubcribeMap(Map<String, Set<ResourceListener>> subcribeMap) {
		this.subcribeMap = subcribeMap;
	}
	
	public Map<String, Notify> getNotifyCache() {
		return notifyCache;
	}
	
	@Override
	public void close(){
		remotingClient.shutdown();
	}
	
	public ExecutorService getPublicExecutor() {
		return publicExecutor;
	}

	public void setPublicExecutor(ExecutorService publicExecutor) {
		this.publicExecutor = publicExecutor;
	}
	
	public NettyRemotingClient getRemotingClient() {
		return remotingClient;
	}
}
