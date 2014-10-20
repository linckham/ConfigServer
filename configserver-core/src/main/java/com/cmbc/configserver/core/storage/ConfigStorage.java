package com.cmbc.configserver.core.storage;

import com.cmbc.configserver.domain.Configuration;

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
}
