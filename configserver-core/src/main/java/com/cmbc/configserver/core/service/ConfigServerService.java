package com.cmbc.configserver.core.service;

import com.cmbc.configserver.domain.Configuration;

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
    public boolean subscribe(Configuration config);

    /**
     * unSubscribe the specified configuration which is in the config server
     *
     * @param config the configuration that will being unSubscribed
     * @return true if unSubscribed successfully,else false
     */
    public boolean unSubscribe(Configuration config);
}