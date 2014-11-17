package com.cmbc.configserver.core.service;

import com.cmbc.configserver.domain.ConfigHeartBeat;

import java.util.List;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/17
 * @Time 10:34
 */
public interface ConfigHeartBeatService {
    public boolean save(ConfigHeartBeat heartBeat) throws Exception;

    public boolean update(ConfigHeartBeat heartBeat) throws Exception;

    public boolean delete(String clientId) throws Exception;

    public ConfigHeartBeat get(String clientId) throws Exception;

    public List<ConfigHeartBeat> getTimeout() throws Exception;
}
