package com.cmbc.configserver.core.service.impl;

import com.cmbc.configserver.core.event.Event;
import com.cmbc.configserver.core.event.EventType;
import com.cmbc.configserver.core.notify.NotifyService;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.core.service.ConfigServerService;
import com.cmbc.configserver.core.storage.ConfigStorage;

import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;

/**
 * the implementation of ConfigServerService
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/31
 * @Time 11:12
 */
public class ConfigServerServiceImpl implements ConfigServerService {
    private ConfigStorage configStorage;
    private NotifyService notifyService;

    public ConfigServerServiceImpl(ConfigStorage configStorage){
        super();
        this.configStorage = configStorage;
    }

    public ConfigServerServiceImpl(ConfigStorage configStorage,NotifyService notifyService){
        this(configStorage);
        this.notifyService = notifyService;
    }

    public void setConfigStorage(ConfigStorage configStorage){
        this.configStorage = configStorage;
    }

    /**
     *
     * @return the configuration storage
     */
    @Override
    public ConfigStorage getConfigStorage(){
        return this.configStorage;
    }

    public void setNotifyService(NotifyService notifyService){
        this.notifyService = this.notifyService;
    }

    public NotifyService getNotifyService(){
        return this.notifyService;
    }

    /**
     * publish the configuration to the config server
     *
     * @param config the configuration that will being stored in server
     * @return true if publish successfully,else false
     */
    @Override
    public boolean publish(Configuration config) throws Exception{
        validConfiguration(config);
        boolean bSuccess = this.configStorage.publish(config);
        if(bSuccess){
            //publish the publish event
            Event event = new Event();
            event.setEventType(EventType.PUBLISH);
            event.setEventSource(config);
            event.setEventCreatedTime(System.currentTimeMillis());
            this.notifyService.publish(event);
        }
        return bSuccess;
    }

    /**
     * unPublish the configuration to the config server
     *
     * @param config the configuration that will being removed in server
     * @return true if unPublish successfully,else false
     */
    @Override
    public boolean unPublish(Configuration config)  throws Exception{
        validConfiguration(config);
        boolean bSuccess = this.configStorage.unPublish(config);
        if(bSuccess){
            //publish the unPublish event
            Event event = new Event();
            event.setEventType(EventType.UN_PUBLISH);
            event.setEventSource(config);
            event.setEventCreatedTime(System.currentTimeMillis());
            this.notifyService.publish(event);
        }
        return bSuccess;
    }

    /**
     * subscribe the specified configuration which is in the config server
     *
     * @param config the configuration that will being subscribed
     * @param channel the subscriber's channel
     * @return true if subscribed successfully,else false
     */
    @Override
    public boolean subscribe(Configuration config,Channel channel) throws Exception{
        validConfiguration(config);
        boolean bSuccess = this.configStorage.subscribe(config,channel);
        return bSuccess;
    }

    /**
     * unSubscribe the specified configuration which is in the config server
     *
     * @param config the configuration that will being unSubscribed
     * @param channel the subscriber's channel
     * @return true if unSubscribed successfully,else false
     */
    @Override
    public boolean unSubscribe(Configuration config,Channel channel) throws Exception {
        validConfiguration(config);
        return this.configStorage.unSubscribe(config,channel);
    }

    @Override
    public void start() throws Exception{
        this.notifyService.start();
    }

    @Override
    public void shutdown(){
        this.notifyService.stop();
    }

    private void validConfiguration(Configuration config) throws Exception{
        if(null == config){
            throw new NullPointerException("configuration is null");
        }
        if(StringUtils.isBlank(config.getCell())){
            throw new IllegalArgumentException("config cell is null or empty!");
        }

        if(StringUtils.isBlank(config.getResource())){
            throw new IllegalArgumentException("config resource is null or empty!");
        }

        if(StringUtils.isBlank(config.getType())){
            throw new IllegalArgumentException("config type is null or empty!");
        }
    }
}