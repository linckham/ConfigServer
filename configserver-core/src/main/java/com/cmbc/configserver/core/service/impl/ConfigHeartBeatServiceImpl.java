package com.cmbc.configserver.core.service.impl;

import com.cmbc.configserver.core.dao.ConfigHeartBeatDao;
import com.cmbc.configserver.core.service.ConfigHeartBeatService;
import com.cmbc.configserver.domain.ConfigHeartBeat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/17
 * @Time 10:48
 */
@Service("configHeartBeatService")
public class ConfigHeartBeatServiceImpl implements ConfigHeartBeatService {
    @Autowired
    private ConfigHeartBeatDao configHeartBeatDao;

    public void setConfigHeartBeatDao(ConfigHeartBeatDao configHeartBeatDao) {
        this.configHeartBeatDao = configHeartBeatDao;
    }

    @Override
    public boolean save(ConfigHeartBeat heartBeat) throws Exception {
        return configHeartBeatDao.save(heartBeat);
    }

    @Override
    public boolean update(ConfigHeartBeat heartBeat) throws Exception {
        return configHeartBeatDao.update(heartBeat);
    }

    @Override
    public boolean delete(String clientId) throws Exception {
        return configHeartBeatDao.delete(clientId);
    }

    @Override
    public ConfigHeartBeat get(String clientId) throws Exception {
        return configHeartBeatDao.get(clientId);
    }

    @Override
    public List<ConfigHeartBeat> getTimeout() throws Exception {
        return configHeartBeatDao.getTimeout();
    }
}
