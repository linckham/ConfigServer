package com.cmbc.configserver.core.storage.impl;

import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.utils.ConfigServerLogger;
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
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/31
 * @Time 11:12
 */
public class LocalMemoryConfigStorageImpl extends AbstractConfigStorage {
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
}