package com.cmbc.configserver.core.storage.impl;

import com.cmbc.configserver.core.dao.ConfigDetailsDao;
import com.cmbc.configserver.core.storage.ConfigStorage;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.utils.PathUtils;
import io.netty.channel.Channel;

import java.util.Collections;
import java.util.List;

/**
 * the implementation of the ConfigStorage that use mysql to storage the configuration.<br/>
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @author tongchuan.lin<linckham@gmail.com>.
 *         Date: 2014/10/28
 *         Time: 15:42
 */
public class MysqlConfigStorageImpl implements ConfigStorage{
    private ConfigDetailsDao configDao;

    public void setConfigDao(ConfigDetailsDao configDao){
        this.configDao = configDao;
    }

    public ConfigDetailsDao getConfigDao(){
        return this.configDao;
    }

    /**
     * publish the configuration to the config server
     *
     * @param config the configuration that will being stored in server
     * @return true if publish successfully,else false
     */
    public boolean publish(Configuration config) throws Exception{
        if(null == config){
            return false;
        }
        return  this.configDao.save(config);
    }

    /**
     * unPublish the configuration to the config server
     *
     * @param config the configuration that will being removed in server
     * @return true if unPublish successfully,else false
     */
    public boolean unPublish(Configuration config) throws Exception{
        if(null == config){
            return false;
        }
        return  this.configDao.delete(config);
    }
    /**
     * subscribe the specified configuration which is in the config server
     *
     * @param config the configuration that will being subscribed
     * @param channel the channel of the subscriber
     * @return true if subscribed successfully,else false
     */
    public boolean subscribe(Configuration config,Channel channel){
        return false;
    }

    /**
     * unSubscribe the specified configuration which is in the config server
     *
     * @param config the configuration that will being unSubscribed
     * @param channel the channel of the subscriber
     * @return true if unSubscribed successfully,else false
     */
    public boolean unSubscribe(Configuration config,Channel channel){
        return false;
    }

    /**
     * get the configuration list by the specified configuration
     * @param config the specified configuration
     * @return the configuration list
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Configuration> getConfigurationList(Configuration config) throws Exception{
        if(null == config){
            return Collections.EMPTY_LIST;
        }

        return null;
    }

    /**
     * get the subscriber's channel  list of the specified subscribe path
     * @param subscribePath the subscribe path which the subscriber is interested in.
     * @return the subscriber's channel list
     */
    public List<Channel> getSubscribeChannel(String subscribePath) throws Exception{
        throw new NoSuchMethodException("");
    }
}
