package com.cmbc.configserver.core.storage;

import com.cmbc.configserver.domain.Configuration;
import io.netty.channel.Channel;

import java.util.List;
import java.util.Set;

/**
 * the storage interface of the configuration server<br/>
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/31
 * @Time 11:12
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
    public Set<Channel> getSubscribeChannel(String subscribePath) throws Exception;

    /**
     * delete the configuration list by the specified client id
     * @param clientId the client id which the configuration items belongs to
     * @return true if deleted success,else false
     * @throws Exception
     */
    public List<String> deleteConfigurationByClientId(String clientId) throws Exception;

    /**
     * get the category id list of the specified client id.
     * @param clientId the client id which the category id belongs to
     * @return true if query success,else false
     * @throws Exception
     */
    public List<Integer> getCategoryIdsByClientId(String clientId) throws Exception;
}