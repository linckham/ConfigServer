package com.cmbc.configserver.client.impl;

import io.netty.channel.Channel;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
import com.cmbc.configserver.remoting.netty.NettyClientConfig;
import com.cmbc.configserver.remoting.netty.NettyRemotingClient;
import com.cmbc.configserver.remoting.protocol.RemotingCommand;
import com.cmbc.configserver.utils.ConcurrentHashSet;
import com.cmbc.configserver.utils.PathUtils;

public class ConfigClientImpl implements ConfigClient {
	private static final Logger logger = LoggerFactory.getLogger(ConfigClientImpl.class);
	private NettyRemotingClient remotingClient;
	private ClientRemotingProcessor clientRemotingProcessor;
	private AtomicInteger heartbeatFailedTimes = new AtomicInteger(0);
	//TODO configurable?
	private static int timeoutMillis = 30000;
	 
	public ConfigClientImpl(final NettyClientConfig nettyClientConfig,List<String> addrs){
		this.remotingClient = new NettyRemotingClient(nettyClientConfig,new RemotingChannelListener(this));
		remotingClient.updateNameServerAddressList(addrs);
		this.clientRemotingProcessor = new ClientRemotingProcessor(this);
		remotingClient.registerProcessor(RequestCode.NOTIFY_CONFIG, clientRemotingProcessor, null);
		//start client
		remotingClient.start();
	}

	@Override
	public boolean publish(Configuration config) {
		RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.PUBLISH_CONFIG);
		byte[] body = RemotingSerializable.encode(config);
		request.setBody(body);
		try {
			RemotingCommand result = remotingClient.invokeSync(null, request, timeoutMillis);
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
			RemotingCommand result = remotingClient.invokeSync(null, request, timeoutMillis);
			if(result.getCode() != ResponseCode.UNPUBLISH_CONFIG_OK){
				return false;
			}
		} catch (Exception e) {
			logger.info(e.toString());
			return false;
		} 
		
		return true;
	}

	public Map<String,Set<ResourceListener>> subcribeMap = new ConcurrentHashMap<String,Set<ResourceListener>>();
	private final Lock subcribeMapLock = new ReentrantLock();
	private static final long LockTimeoutMillis = 3000;
	
	public Map<String,Notify> notifyCache = new ConcurrentHashMap<String,Notify>();
	
	@Override
	public boolean subscribe(Configuration config, ResourceListener listener) {
		String subKey = PathUtils.getSubscriberPath(config);
		Set<ResourceListener> listeners =  subcribeMap.get(subKey);
		if (listeners == null || listeners.size() == 0) {
			try {
				if (subcribeMapLock.tryLock(LockTimeoutMillis,TimeUnit.MILLISECONDS)) {
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

							RemotingCommand response = remotingClient.invokeSync(null, request, timeoutMillis);

							if (response.getCode() != ResponseCode.SUBSCRIBE_CONFIG_OK) {
								return false;
							} else {
								if(response.getBody() != null){
									Notify notify = RemotingSerializable.decode(response.getBody(),Notify.class);;
									notifyCache.put(subKey,notify);
									listeners.add(listener);
									listener.notify(notify.getConfigLists());
								}else{
									logger.info("subscribe notify is null!");
									return false;
								}
							}

						} else {
							Notify notify = notifyCache.get(subKey);
							listeners.add(listener);
							listener.notify(notify.getConfigLists());
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
			listener.notify(notify == null? null : notify.getConfigLists());
		}
		
		return true;
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
				if (subcribeMapLock.tryLock(LockTimeoutMillis,TimeUnit.MILLISECONDS)) {
					try {
						if (listerners.size() == 0) {
							RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.UNSUBSCRIBE_CONFIG);
							byte[] body = RemotingSerializable.encode(config);
							request.setBody(body);
							RemotingCommand result = remotingClient.invokeSync(null, request, timeoutMillis);

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
		int retryTime = 3;
		boolean sendSuccessed = false;
		for(int i= 0; i< retryTime; i++){
			RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.HEARTBEAT);
			request.setBody(null);
			try {
				RemotingCommand result = remotingClient.invokeSyncImpl(channel, request, timeoutMillis);
				if(result.getCode() == ResponseCode.HEARTBEAT_OK){
					sendSuccessed = true;
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
				remotingClient.closeChannel(channel);
				heartbeatFailedTimes.set(0);
				//TODO reset works
				
			}
		}
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
}
