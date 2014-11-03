package com.cmbc.configserver.core.storage.impl;

import com.cmbc.configserver.core.storage.ConfigStorage;
import com.cmbc.configserver.core.subscriber.SubscriberService;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.utils.PathUtils;
import io.netty.channel.Channel;

import java.util.List;
import java.util.Set;

/**
 * the abstract implementation class of ConfigStorage
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/31
 * @Time 13:27
 */
public abstract class AbstractConfigStorage implements ConfigStorage {
    private SubscriberService subscriberService;

    public void setSubscriberService(SubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

    /**
     * subscribe the specified configuration which is in the config server
     *
     * @param config  the configuration that will being subscribed
     * @param channel the channel of the subscriber
     * @return true if subscribed successfully,else false
     */
    public boolean subscribe(Configuration config, Channel channel) {
        return this.subscriberService.subscribe(PathUtils.getSubscriberPath(config), channel);
    }

    /**
     * unSubscribe the specified configuration which is in the config server
     *
     * @param config  the configuration that will being unSubscribed
     * @param channel the channel of the subscriber
     * @return true if unSubscribed successfully,else false
     */
    public boolean unSubscribe(Configuration config, Channel channel) {
        return this.subscriberService.unSubcribe(PathUtils.getSubscriberPath(config), channel);
    }

    /**
     * get the subscriber's channel  list of the specified subscribe path
     *
     * @param subscribePath the subscribe path which the subscriber is interested in.
     * @return the subscriber's channel list
     */
    public Set<Channel> getSubscribeChannel(String subscribePath) throws Exception{
        return this.subscriberService.getSubscriberChannels(subscribePath);
    }

    @Override
    public List<String> deleteConfigurationByClientId(String clientId) throws Exception {
        return null;
    }

    @Override
    public List<Integer> getCategoryIdsByClientId(String clientId) throws Exception{
        return null;
    }
}
