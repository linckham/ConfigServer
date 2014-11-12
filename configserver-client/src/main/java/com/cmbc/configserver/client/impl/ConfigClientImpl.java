package com.cmbc.configserver.client.impl;

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
import com.cmbc.configserver.client.ConfigClientException;
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
import com.cmbc.configserver.utils.Constants;
import com.cmbc.configserver.utils.PathUtils;

public class ConfigClientImpl implements ConfigClient {
	private static final Logger logger = LoggerFactory.getLogger(ConfigClientImpl.class);
	private NettyRemotingClient remotingClient;
	private ClientRemotingProcessor clientRemotingProcessor;
	private ExecutorService publicExecutor;
	public Map<String,Set<ResourceListener>> subscribeMap = new ConcurrentHashMap<String,Set<ResourceListener>>();
	private final Lock subscribeMapLock = new ReentrantLock();
	public Map<String,Notify> notifyCache = new ConcurrentHashMap<String,Notify>();
	private AtomicInteger heartbeatFailedTimes = new AtomicInteger(0);

	public ConfigClientImpl(final NettyClientConfig nettyClientConfig,List<String> addrs,
								ConnectionStateListener stateListener){
		this.remotingClient = new NettyRemotingClient(nettyClientConfig,new RemotingChannelListener(this));
		remotingClient.updateNameServerAddressList(addrs);
		this.clientRemotingProcessor = new ClientRemotingProcessor(this);
		remotingClient.registerProcessor(RequestCode.NOTIFY_CONFIG, clientRemotingProcessor, null);
		//start client
        try {
            remotingClient.start();
        } catch (InterruptedException e) {
            logger.error("Failed in starting remote client,cause=",e);
        }
        remotingClient.setConnectionStateListener(stateListener);
		
		publicExecutor = remotingClient.getCallbackExecutor();
	}

	@Override
	public void publish(Configuration config) {
		RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.PUBLISH_CONFIG);
		byte[] body = RemotingSerializable.encode(config);
		request.setBody(body);
		try {
			RemotingCommand result = remotingClient.invokeSync(request, Constants.DEFAULT_SOCKET_READING_TIMEOUT);
			if(result.getCode() != ResponseCode.PUBLISH_CONFIG_OK){
				throw new ConfigClientException("publish config failed");
			}
		} catch (Exception e) {
			logger.info(e.toString());
			throw new ConfigClientException(e);
		} 
		
		
	}

	@Override
	public void unpublish(Configuration config) {
		RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.UNPUBLISH_CONFIG);
		byte[] body = RemotingSerializable.encode(config);
		request.setBody(body);
		try {
			RemotingCommand result = remotingClient.invokeSync(request, Constants.DEFAULT_SOCKET_READING_TIMEOUT);
			if(result.getCode() != ResponseCode.UNPUBLISH_CONFIG_OK){
				throw new ConfigClientException("unpublish config failed");
			}
		} catch (Exception e) {
			logger.info(e.toString());
			throw new ConfigClientException(e);
		} 
	}

	@Override
	public List<Configuration> subscribe(Configuration config, ResourceListener listener){
		String subKey = PathUtils.getSubscriberPath(config);
		Set<ResourceListener> listeners =  subscribeMap.get(subKey);
		if (listeners == null || listeners.size() == 0) {
			try {
				if (subscribeMapLock.tryLock(Constants.DEFAULT_READ_WRITE_LOCK_TIMEOUT,TimeUnit.MILLISECONDS)) {
					try {
						listeners = subscribeMap.get(subKey);
						if (listeners == null || listeners.size() == 0) {
							if(listeners == null){
								listeners = new ConcurrentHashSet<ResourceListener>();
								subscribeMap.put(subKey, listeners);
							}

							RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.SUBSCRIBE_CONFIG);
							byte[] body = RemotingSerializable.encode(config);
							request.setBody(body);

							RemotingCommand response = remotingClient.invokeSync(request, Constants.DEFAULT_SOCKET_READING_TIMEOUT);

							if (response.getCode() != ResponseCode.SUBSCRIBE_CONFIG_OK) {
								throw new Exception();
							} else {
								if(response.getBody() != null){
									Notify notify = RemotingSerializable.decode(response.getBody(),Notify.class);;
									notifyCache.put(subKey,notify);
									listeners.add(listener);
                                    return notify.getConfigLists();
								}else{
									logger.info("subscribe notify is null!");
									throw new ConfigClientException("subscribe notify is null!");
								}
							}

						} else {
							Notify notify = notifyCache.get(subKey);
							listeners.add(listener);
                            return notify.getConfigLists();
						}
					} catch (Exception e) {
						logger.info(e.toString());
						throw new ConfigClientException(e);
					} finally {
						subscribeMapLock.unlock();
					}
				}else{
					throw new ConfigClientException("get subscribeMapLock timeout");
				}
			} catch (InterruptedException e) {
				logger.info(e.toString());
				throw new ConfigClientException(e);
			}
		} else {
			Notify notify = notifyCache.get(subKey);
			listeners.add(listener);
            return notify.getConfigLists();
		}
		
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
	public void unsubscribe(Configuration config, ResourceListener listener) {
		String subKey = PathUtils.getSubscriberPath(config);
		Set<ResourceListener> listeners =  subscribeMap.get(subKey);
		if(listeners == null){
			throw new ConfigClientException("subscribeMap don't have the listener");
		}
		
		listeners.remove(listeners);
		
		if(listeners.size() == 0){
			try {
				if (subscribeMapLock.tryLock(Constants.DEFAULT_READ_WRITE_LOCK_TIMEOUT,TimeUnit.MILLISECONDS)) {
					try {
						if (listeners.size() == 0) {
							RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.UNSUBSCRIBE_CONFIG);
							byte[] body = RemotingSerializable.encode(config);
							request.setBody(body);
							RemotingCommand result = remotingClient.invokeSync(request, Constants.DEFAULT_SOCKET_READING_TIMEOUT);

							if (result.getCode() != ResponseCode.UNSUBSCRIBE_CONFIG_OK) {
								throw new ConfigClientException("unsubscribe failed");
							}else{
								//success un subscribed,than remove cache
								notifyCache.remove(subKey);
							}
						}
					} catch (Exception e) {
						logger.info(e.toString());
						throw new ConfigClientException(e);
					} finally {
						subscribeMapLock.unlock();
					}
				}else{
					throw new ConfigClientException("get subscribeMapLock timeout");
				}
			} catch (InterruptedException e) {
				logger.info(e.toString());
				throw new ConfigClientException(e);
			}
		}
	}
	
	
	
	public void sendHeartbeat(Channel channel){
		int retryTime = 2;
		boolean sendSuccess = false;
		for(int i= 0; i< retryTime; i++){
			RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.HEARTBEAT);
			request.setBody(null);
			try {
				RemotingCommand result = remotingClient.invokeSyncImpl(channel, request, Constants.DEFAULT_SOCKET_READING_TIMEOUT);
				if(result.getCode() == ResponseCode.HEARTBEAT_OK){
					logger.info("send heartbeat ok");
					sendSuccess = true;
					break;
				}
			} catch (Exception e) {
				//do nothing
				logger.info(e.toString());
			} 
		}
		
		if(!sendSuccess){
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
		this.subscribeMap.clear();
		this.notifyCache.clear();
		
		logger.info("client clear channel: " + channel);
	}
	
	public Map<String, Set<ResourceListener>> getSubscribeMap() {
		return subscribeMap;
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

    @Override
    public boolean isAvailable(){
        return remotingClient.isAvailable();
    }
}
