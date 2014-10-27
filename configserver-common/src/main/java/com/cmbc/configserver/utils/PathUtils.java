package com.cmbc.configserver.utils;

import com.cmbc.configserver.domain.Configuration;
/**
 * the helper class that use to operate the path
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @author tongchuan.lin<linckham@gmail.com>.
 *         Date: 2014/10/24
 *         Time: 14:26
 */
public class PathUtils {
    /**
     * get the path which is interested by the subscriber
     * @param config the subscriber's configuration
     * @return the interested path of the subscriber
     */
    public static String getSubscriberPath(Configuration config){

        if(null == config){
            throw new IllegalArgumentException("configuration can not be null!");
        }

        if(null == config.getCell() || config.getCell().isEmpty()){
            throw new IllegalArgumentException("configuration's cell can not be null or empty!");
        }

        StringBuilder pathBuilder = new StringBuilder(64);
        pathBuilder.append(Constants.PATH_SEPARATOR).append(config.getCell());
        if(null != config.getResource() && !config.getResource().isEmpty()){
            pathBuilder.append(Constants.PATH_SEPARATOR).append(config.getResource());
            if(null != config.getType() && !config.getType().isEmpty()){
                pathBuilder.append(Constants.PATH_SEPARATOR).append(config.getType());
            }
        }
        return pathBuilder.toString();
    }
}