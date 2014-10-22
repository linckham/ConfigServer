package com.cmbc.configserver.core.service.impl;

import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.core.service.ConfigServerService;
import com.cmbc.configserver.core.storage.ConfigStorage;
/**
 * the implementation of ConfigServerService
 * Created by tongchuan.lin<linckham@gmail.com> on 2014/10/21.
 */
public class ConfigServerServiceImpl implements ConfigServerService {
    private ConfigStorage configStorage;
    /**
     * publish the configuration to the config server
     *
     * @param config the configuration that will being stored in server
     * @return true if publish successfully,else false
     */
    @Override
    public boolean publish(Configuration config){
        return this.configStorage.publish(config);
    }

    /**
     * unPublish the configuration to the config server
     *
     * @param config the configuration that will being removed in server
     * @return true if unPublish successfully,else false
     */
    @Override
    public boolean unPublish(Configuration config){
        return this.configStorage.unPublish(config);
    }

    /**
     * subscribe the specified configuration which is in the config server
     *
     * @param config the configuration that will being subscribed
     * @return true if subscribed successfully,else false
     */
    @Override
    public boolean subscribe(Configuration config){
        return this.configStorage.subscribe(config);
    }

    /**
     * unSubscribe the specified configuration which is in the config server
     *
     * @param config the configuration that will being unSubscribed
     * @return true if unSubscribed successfully,else false
     */
    @Override
    public boolean unSubscribe(Configuration config) {
        return this.configStorage.unSubscribe(config);
    }
}