package com.cmbc.configserver.core.storage;

import com.cmbc.configserver.domain.Configuration;
import io.netty.channel.Channel;

import java.util.List;

/**
 * the storage interface of the configuration server<br/>
 * @author  tongchuan.lin<linckham@gmail.com><br/>
 */
public interface ConfigStorage {
    /**
     * publish the configuration to the config server
     *
     * @param config the configuration that will being stored in server
     * @return true if publish successfully,else false
     */
    public boolean publish(Configuration config) throws Exception;

    /**
     * unPublish the configuration to the config server
     *
     * @param config the configuration that will being removed in server
     * @return true if unPublish successfully,else false
     */
    public boolean unPublish(Configuration config) throws Exception;

    /**
     * subscribe the specified configuration which is in the config server
     *
     * @param config the configuration that will being subscribed
     * @param channel the channel of the subscriber
     * @return true if subscribed successfully,else false
     */
    public boolean subscribe(Configuration config,Channel channel);

    /**
     * unSubscribe the specified configuration which is in the config server
     *
     * @param config the configuration that will being unSubscribed
     * @param channel the channel of the subscriber
     * @return true if unSubscribed successfully,else false
     */
    public boolean unSubscribe(Configuration config,Channel channel);

    /**
     * get the configuration list by the specified configuration
     * @param config the specified configuration
     * @return the configuration list
     */
    public List<Configuration> getConfigurationList(Configuration config) throws Exception;

    /**
     * get the subscriber's channel  list of the specified subscribe path
     * @param subscribePath the subscribe path which the subscriber is interested in.
     * @return the subscriber's channel list
     */
    public List<Channel> getSubscribeChannel(String subscribePath) throws Exception;
}