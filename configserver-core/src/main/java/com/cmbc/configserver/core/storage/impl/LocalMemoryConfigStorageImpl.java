package com.cmbc.configserver.core.storage.impl;

import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.core.storage.ConfigStorage;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * the implementation of the ConfigStorage that use the local memory to storage the configuration.<br/>
 * @author  tongchuan.lin<linckham@gmail.com><br/>
 */
public class LocalMemoryConfigStorageImpl implements ConfigStorage {
    private ConcurrentMap</*cell*/String, ConcurrentMap</*resource*/String, List<Configuration>>> publisherMap;
    private ConcurrentMap</*cell*/String, ConcurrentMap</*resource*/String, List<Configuration>>> subscriberMap;

    public LocalMemoryConfigStorageImpl() {
        this.publisherMap = new ConcurrentHashMap(32);
        this.subscriberMap = new ConcurrentHashMap(32);
    }

    /**
     * publish the configuration to the config server
     *
     * @param config the configuration that will being stored in server
     * @return true if publish successfully,else false
     */
    @Override
    public boolean publish(Configuration config) {
        return false;
    }

    /**
     * unPublish the configuration to the config server
     *
     * @param config the configuration that will being removed in server
     * @return true if unPublish successfully,else false
     */
    @Override
    public boolean unPublish(Configuration config) {
        return false;
    }

    /**
     * subscribe the specified configuration which is in the config server
     *
     * @param config the configuration that will being subscribed
     * @return true if subscribed successfully,else false
     */
    @Override
    public boolean subscribe(Configuration config) {
        return false;
    }
}
