package com.cmbc.configserver.core.storage.impl;

import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.core.storage.ConfigStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * the implementation of the ConfigStorage that use the local memory to storage the configuration.<br/>
 * @author  tongchuan.lin<linckham@gmail.com><br/>
 */
public class LocalMemoryConfigStorageImpl implements ConfigStorage {
    private static final Logger LOGGER =LoggerFactory.getLogger(LocalMemoryConfigStorageImpl.class);
    private ConcurrentMap</*cell*/String, ConcurrentMap</*resource*/String, List<Configuration>>> publisherMap;
    private ConcurrentMap</*cell*/String, ConcurrentMap</*resource*/String, List<Configuration>>> subscriberMap;

    public LocalMemoryConfigStorageImpl() {
        this.publisherMap = new ConcurrentHashMap<String,ConcurrentMap<String, List<Configuration>>>(32);
        this.subscriberMap = new ConcurrentHashMap<String,ConcurrentMap<String, List<Configuration>>>(32);
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
        //TODO:It may be better that using read write lock to control the multi-thread safe.
        synchronized (this.publisherMap) {
            ConcurrentMap<String, List<Configuration>> resourceMap = this.publisherMap.get(config.getCell());
            if (null == resourceMap) {
                resourceMap = new ConcurrentHashMap<String, List<Configuration>>();
                this.publisherMap.put(config.getCell(), resourceMap);
            }

            List<Configuration> configurationList = resourceMap.get(config.getResource());
            if (null == configurationList) {
                configurationList = new ArrayList<Configuration>();
                resourceMap.put(config.getResource(), configurationList);
            }

            configurationList.add(config);
            //TODO: Notify the subscriber that the configuration has changed.
            resourceMap.put(config.getCell(), configurationList);
            //log the configuration for debug and trace
            String logInfo = String.format("publish configuration successful! %s ",config);
            //TODO: just for debug,when the logger configuration well,remove it
            System.out.println(logInfo);
            LOGGER.info(logInfo);
        }
        return true;
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
        //TODO:It may be better that using read write lock to control the multi-thread safe.
        synchronized (this.publisherMap) {
            ConcurrentMap<String, List<Configuration>> resourceMap = this.publisherMap.get(config.getCell());
            //the config doesn't exists
            if (null == resourceMap) {
                return false;
            }

            List<Configuration> configurationList = resourceMap.get(config.getResource());
            if (null == configurationList) {
                return false;
            }
            //TODO: Notify the subscriber that the configuration has changed.
            //log the configuration for debug and trace
            LOGGER.info(String.format("unPublish configuration successful! %s ",config));
            return configurationList.remove(config);
        }
    }

    /**
     * subscribe the specified configuration which is in the config server
     *
     * @param config the configuration that will being subscribed
     * @return true if subscribed successfully,else false
     */
    @Override
    public boolean subscribe(Configuration config) {
        if (null == config) {
            return false;
        }

        //TODO:It may be better that using read write lock to control the multi-thread safe.
        synchronized (this.subscriberMap) {
            ConcurrentMap<String, List<Configuration>> resourceMap = this.subscriberMap.get(config.getCell());
            if (null == resourceMap) {
                resourceMap = new ConcurrentHashMap<String, List<Configuration>>();
                this.subscriberMap.put(config.getCell(), resourceMap);
            }

            List<Configuration> configurationList = resourceMap.get(config.getResource());
            if (null == configurationList) {
                configurationList = new ArrayList<Configuration>();
                resourceMap.put(config.getResource(), configurationList);
            }

            configurationList.add(config);
            //TODO: Notify the subscriber that the configuration has changed.
            //log the configuration for debug and trace
            LOGGER.info(String.format("subscribe configuration successful! %s ",config));
            resourceMap.put(config.getCell(), configurationList);
        }
        return true;
    }

    /**
     * unSubscribe the specified configuration which is in the config server
     *
     * @param config the configuration that will being unSubscribed
     * @return true if unSubscribed successfully,else false
     */
    @Override
    public boolean unSubscribe(Configuration config) {
        if (null == config) {
            return false;
        }
        //TODO:It may be better that using read write lock to control the multi-thread safe.
        synchronized (this.subscriberMap) {
            ConcurrentMap<String, List<Configuration>> resourceMap = this.subscriberMap.get(config.getCell());
            //the config doesn't exists
            if (null == resourceMap) {
                return false;
            }

            List<Configuration> configurationList = resourceMap.get(config.getResource());
            if (null == configurationList) {
                return false;
            }
            //TODO: Notify the subscriber that the configuration has changed.
            //log the configuration for debug and trace
            LOGGER.info(String.format("unSubscribe configuration successful! %s ",config));
            return configurationList.remove(config);
        }
    }
}