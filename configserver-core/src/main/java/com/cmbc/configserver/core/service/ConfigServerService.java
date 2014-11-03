package com.cmbc.configserver.core.service;

import com.cmbc.configserver.core.storage.ConfigStorage;
import com.cmbc.configserver.domain.Configuration;
import io.netty.channel.Channel;
/**
 * the config server's core service.<br/>
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/31
 * @Time 11:12
 */
public interface ConfigServerService {
    /**
     * publish the configuration to the config server
     *
     * @param config the configuration that will being stored in server
     * @return true if publish successfully,else false
     */
    public boolean publish(Configuration config)  throws Exception;

    /**
     * unPublish the configuration to the config server
     *
     * @param config the configuration that will being removed in server
     * @return true if unPublish successfully,else false
     */
    public boolean unPublish(Configuration config)  throws Exception;

    /**
     * subscribe the specified configuration which is in the config server
     *
     * @param config the configuration that will being subscribed
     * @return true if subscribed successfully,else false
     */
    public boolean subscribe(Configuration config,Channel channel) throws Exception;

    /**
     * unSubscribe the specified configuration which is in the config server
     *
     * @param config the configuration that will being unSubscribed
     * @param channel the subscriber's channel
     * @return true if unSubscribed successfully,else false
     */
    public boolean unSubscribe(Configuration config,Channel channel) throws Exception;

    public void start() throws Exception;
    public void shutdown();

    public ConfigStorage getConfigStorage();

    /**
     * delete the configuration list by the specified client id
     * @param clientId the client id which the configuration items belongs to
     * @return true if deleted success,else false
     * @throws Exception
     */
    public boolean deleteConfigurationByClientId(String clientId) throws Exception;
}