package com.cmbc.configserver.core.storage.impl;

import com.cmbc.configserver.core.storage.ConfigStorage;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.utils.ConfigServerLogger;
import com.cmbc.configserver.utils.PathUtils;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * the implementation of the ConfigStorage that use the local memory to storage the configuration.<br/>
 *
 * @author tongchuan.lin<linckham@gmail.com><br/>
 */
public class LocalMemoryConfigStorageImpl implements ConfigStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalMemoryConfigStorageImpl.class);
    private static final int LOCK_TIMEOUT = 1 * 1000;
    //TODO: storage the path as the Map's key
    private Map</*cell*/String, Map</*resource*/String, Map</*type*/String, List<Configuration>>>> publisherMap;
    private Map</*subscribe path*/String,/*subscribe channels*/List<Channel>> subscriberMap;
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    public LocalMemoryConfigStorageImpl() {
        this.publisherMap = new ConcurrentHashMap<String, Map<String, Map<String, List<Configuration>>>>(32);
        this.subscriberMap = new ConcurrentHashMap<String, List<Channel>>(32);
    }

    /**
     * publish the configuration to the config server
     *
     * @param config the configuration that will being stored in server
     * @return true if publish successfully,else false
     */
    @Override
    public boolean publish(Configuration config) {
        if (null == config) {
            return false;
        }

        try {
            writeLock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
            Map<String, Map<String, List<Configuration>>> resourceMap = this.publisherMap.get(config.getCell());
            if (null == resourceMap) {
                resourceMap = new ConcurrentHashMap<String, Map<String, List<Configuration>>>();
                this.publisherMap.put(config.getCell(), resourceMap);
            }

            //type Map represents the configuration is publisher/subscriber/config/router.
            Map<String, List<Configuration>> typeMap = resourceMap.get(config.getResource());
            if (null == typeMap) {
                typeMap = new ConcurrentHashMap<String, List<Configuration>>();
                resourceMap.put(config.getResource(), typeMap);
            }

            List<Configuration> configurationList = typeMap.get(config.getType());
            if (null == configurationList) {
                configurationList = new ArrayList<Configuration>();
                typeMap.put(config.getType(), configurationList);
            }
            configurationList.add(config);
            //log the configuration for debug and trace
            String logInfo = String.format("publish configuration successful! %s ", config);
            ConfigServerLogger.info(logInfo);
            return true;
        } catch (InterruptedException e) {
            //log the exception
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * unPublish the configuration to the config server
     *
     * @param config the configuration that will being removed in server
     * @return true if unPublish successfully,else false
     */
    @Override
    public boolean unPublish(Configuration config) {
        if (null == config) {
            return false;
        }
        try {
            writeLock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
            //resource map of the specified cell
            Map<String, Map<String, List<Configuration>>> resourceMap = this.publisherMap.get(config.getCell());
            //the config doesn't exists
            if (null == resourceMap) {
                return false;
            }

            //type map of the specified resource
            Map<String, List<Configuration>> typeMap = resourceMap.get(config.getResource());
            if (null == typeMap) {
                return false;
            }

            //configuration list of specified type
            List<Configuration> configurationList = typeMap.get(config.getType());
            if (null == configurationList) {
                return false;
            }
            //log the configuration for debug and trace
            ConfigServerLogger.info(String.format("unPublish configuration successful! %s ", config));
            // remove the specified configuration
            boolean bRemoved = configurationList.remove(config);
            //check the map
            if (configurationList.isEmpty()) {
                typeMap.remove(config.getType());
            }

            if (typeMap.isEmpty()) {
                resourceMap.remove(config.getResource());
            }

            if (resourceMap.isEmpty()) {
                this.publisherMap.remove(config.getCell());
            }
            return bRemoved;
        } catch (InterruptedException e) {
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * subscribe the specified configuration which is in the config server
     *
     * @param config  the configuration that will being subscribed
     * @param channel the channel of the subscriber
     * @return true if subscribed successfully,else false
     */
    @Override
    public boolean subscribe(Configuration config, Channel channel) {
        if (null == config) {
            return false;
        }
        try {
            writeLock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
            String path = PathUtils.getSubscriberPath(config);
            List<Channel> subscriberChannels = this.subscriberMap.get(path);
            if (null == subscriberChannels) {
                subscriberChannels = new ArrayList<Channel>();
                this.subscriberMap.put(path, subscriberChannels);
            }
            subscriberChannels.add(channel);
            ConfigServerLogger.info(String.format("subscribe configuration successful! %s,subscriber channel=", config, channel));
            return true;
        } catch (InterruptedException e) {
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * unSubscribe the specified configuration which is in the config server
     *
     * @param config the configuration that will being unSubscribed
     * @return true if unSubscribed successfully,else false
     */
    @Override
    public boolean unSubscribe(Configuration config, Channel channel) {
        if (null == config) {
            return false;
        }
        try {
            writeLock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
            String path = PathUtils.getSubscriberPath(config);
            List<Channel> subscriberChannels = this.subscriberMap.get(path);
            if (null == subscriberChannels) {
                return false;
            }
            subscriberChannels.remove(channel);
            if (subscriberChannels.isEmpty()) {
                this.subscriberMap.remove(path);
            }
            return true;
        } catch (InterruptedException e) {
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * get the configuration list by the specified configuration
     *
     * @param config the specified configuration
     * @return the configuration list
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Configuration> getConfigurationList(Configuration config) {
        if (null == config) {
            return Collections.EMPTY_LIST;
        }
        try {
            readLock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
            Map<String, Map<String, List<Configuration>>> resourceMap = this.publisherMap.get(config.getCell());
            if (null == resourceMap || resourceMap.isEmpty()) {
                return Collections.EMPTY_LIST;
            }

            Map<String, List<Configuration>> typeMap = resourceMap.get(config.getResource());
            if (null == typeMap || typeMap.isEmpty()) {
                return Collections.EMPTY_LIST;
            }

            List<Configuration> configList = typeMap.get(config.getType());
            if (null == configList || configList.isEmpty()) {
                return configList;
            }
            return configList;
        } catch (InterruptedException e) {
            return Collections.EMPTY_LIST;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * get the subscriber's channel  list of the specified subscribe path
     *
     * @param subscribePath the subscribe path which the subscriber is interested in.
     * @return the subscriber's channel list
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Channel> getSubscribeChannel(String subscribePath) {
        if (null == subscribePath || subscribePath.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        try {
            readLock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
            List<Channel> channelList = this.subscriberMap.get(subscribePath);
            if (null == channelList || channelList.isEmpty()) {
                return Collections.EMPTY_LIST;
            }
            return channelList;
        } catch (InterruptedException e) {
            return Collections.EMPTY_LIST;
        } finally {
            readLock.unlock();
        }
    }
}