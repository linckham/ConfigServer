package com.cmbc.configserver.client.impl;

import com.cmbc.configserver.utils.*;
import io.netty.channel.Channel;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
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

public class ConfigClientImpl implements ConfigClient {
	private static final Logger logger = LoggerFactory.getLogger(ConfigClientImpl.class);
	private NettyRemotingClient remotingClient;
	private ClientRemotingProcessor clientRemotingProcessor;
	private ExecutorService publicExecutor;
	public Map<String,Set<ResourceListener>> subscribeMap = new ConcurrentHashMap<String,Set<ResourceListener>>();
	private final Lock subscribeMapLock = new ReentrantLock();
	public Map<String,Notify> notifyCache = new ConcurrentHashMap<String,Notify>();
	private AtomicInteger heartbeatFailedTimes = new AtomicInteger(0);
    /**
     * the schedule that uses to scan the config server address file with fix rate
     */
    private ScheduledExecutorService  scheduleService = Executors.newSingleThreadScheduledExecutor();

    private final String serverAddressFile;
    private volatile  SnapshotFile snapshotFile;

	public ConfigClientImpl(final NettyClientConfig nettyClientConfig,List<String> serverAddress,
								ConnectionStateListener stateListener){
		this.remotingClient = new NettyRemotingClient(nettyClientConfig,new RemotingChannelListener(this));
        serverAddressFile = ConfigUtils.getProperty(Constants.CONFIG_SERVER_ADDRESS_FILE_NAME_KEY,Constants.DEFAULT_CONFIG_SERVER_ADDRESS_FILE_NAME);
        // the serverAddress priority is higher
        if (null != serverAddress && !serverAddress.isEmpty()) {
            remotingClient.updateNameServerAddressList(serverAddress);
        } else {
            File file = new File(serverAddressFile);
            //the config server address file priority is higher than the client
            if (file.exists()) {
                List<String> tmpList = ConfigUtils.getConfigServerAddressList(serverAddressFile, Constants.CONFIG_SERVER_ADDRESS_KEY);
                if (null != tmpList && !tmpList.isEmpty()) {
                    snapshotFile = new SnapshotFile(new File(serverAddressFile).lastModified(), tmpList);
                    remotingClient.updateNameServerAddressList(tmpList);
                    schedule();
                } else {
                    throw new RuntimeException(String.format("config server address in the file  %s is empty or its format is invalid, please check it", serverAddressFile));
                }
            } else {
                throw new RuntimeException(String.format("config server address file %s doesn't exists, please check it.", serverAddressFile));
            }
        }
        this.clientRemotingProcessor = new ClientRemotingProcessor(this);
		remotingClient.registerProcessor(RequestCode.NOTIFY_CONFIG, clientRemotingProcessor, null);
		//start client
        remotingClient.start();
        remotingClient.setConnectionStateListener(stateListener);
		publicExecutor = remotingClient.getCallbackExecutor();
    }

    private void schedule() {
        //schedule to scan the config server address file
        scheduleService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    scanConfigServerAddressFile();
                } catch (Throwable t) {
                    logger.warn("schedule the config server address file failed.", t);
                }
            }
        }, 10 * 1000, 30 * 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * scan the config server address file. determine the file's content whether has been modified
     */
    private void scanConfigServerAddressFile() {
        File file = new File(this.serverAddressFile);
        if (file.exists()) {
            long lastModifyTime = file.lastModified();
            if (snapshotFile.getLastModifyTime() != lastModifyTime) {
                logger.warn(String.format("config server address file %s has been modified at %s", this.serverAddressFile, lastModifyTime));
                List<String> tmpList = ConfigUtils.getConfigServerAddressList(serverAddressFile, Constants.CONFIG_SERVER_ADDRESS_KEY);
                if ((null!=tmpList && !tmpList.isEmpty())&& !Arrays.equals(tmpList.toArray(), snapshotFile.getContent().toArray())) {
                    ConfigServerLogger.warn(String.format("config server address file %s content has been changed. new content is %s ", this.serverAddressFile, tmpList));
                    snapshotFile.setLastModifyTime(lastModifyTime);
                    snapshotFile.setContent(tmpList);
                    getRemotingClient().updateNameServerAddressList(tmpList);
                }
            }
            logger.info(String.format("config server address file %s doesn't change.",serverAddressFile));
        } else {
            logger.warn(String.format("config server address file %s has been deleted.Please check it now!!", serverAddressFile));
        }
    }

    class SnapshotFile {
        private long lastModifyTime;
        private List<String> content;

        public SnapshotFile(long lastModifyTime, List<String> content) {
            this.lastModifyTime = lastModifyTime;
            this.content = content;
        }

        public long getLastModifyTime() {
            return lastModifyTime;
        }

        public void setLastModifyTime(long lastModifyTime) {
            this.lastModifyTime = lastModifyTime;
        }

        public List<String> getContent() {
            return content;
        }

        public void setContent(List<String> content) {
            this.content = content;
        }

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
		this.getPublicExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    listener.notify(notify.getConfigLists());
                } catch (Exception e) {
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
			//don't throw runtime exception,just return
            return;
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
					//logger.info("send heartbeat ok");
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

        if(this.scheduleService != null){
            this.scheduleService.shutdown();
        }

	}
	
	public ExecutorService getPublicExecutor() {
		return publicExecutor;
	}
	
	public NettyRemotingClient getRemotingClient() {
		return remotingClient;
	}

    @Override
    public boolean isAvailable(){
        return remotingClient.isAvailable();
    }
}
