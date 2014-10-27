package com.cmbc.configserver.core.service;

import com.cmbc.configserver.domain.Configuration;
import io.netty.channel.Channel;
/**
 * the config server's core service.<br/>
 * Created by tongchuan.ling<linckham@gmail.com> on 2014/10/20.
 */
public interface ConfigServerService {
    /**
     * publish the configuration to the config server
     *
     * @param config the configuration that will being stored in server
     * @return true if publish successfully,else false
     */
    public boolean publish(Configuration config);

    /**
     * unPublish the configuration to the config server
     *
     * @param config the configuration that will being removed in server
     * @return true if unPublish successfully,else false
     */
    public boolean unPublish(Configuration config);

    /**
     * subscribe the specified configuration which is in the config server
     *
     * @param config the configuration that will being subscribed
     * @return true if subscribed successfully,else false
     */
    public boolean subscribe(Configuration config,Channel channel);

    /**
     * unSubscribe the specified configuration which is in the config server
     *
     * @param config the configuration that will being unSubscribed
     * @param channel the subscriber's channel
     * @return true if unSubscribed successfully,else false
     */
    public boolean unSubscribe(Configuration config,Channel channel);

    public void start() throws Exception;
    public void shutdown();
}