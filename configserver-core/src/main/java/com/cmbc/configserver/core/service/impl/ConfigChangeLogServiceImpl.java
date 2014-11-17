package com.cmbc.configserver.core.service.impl;

import com.cmbc.configserver.core.dao.ConfigChangeLogDao;
import com.cmbc.configserver.core.service.ConfigChangeLogService;
import com.cmbc.configserver.domain.ConfigChangeLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/17
 * @Time 10:42
 */
@Service("configChangeLogService")
public class ConfigChangeLogServiceImpl implements ConfigChangeLogService {
    @Autowired
    private ConfigChangeLogDao configChangeLogDao;

    public void setConfigChangeLogDao(ConfigChangeLogDao configChangeLogDao) {
        this.configChangeLogDao = configChangeLogDao;
    }


    @Override
    public boolean add(ConfigChangeLog changeLog) throws Exception {
        return configChangeLogDao.add(changeLog);
    }

    @Override
    public boolean update(ConfigChangeLog changeLog) throws Exception {
        return configChangeLogDao.update(changeLog);
    }

    @Override
    public ConfigChangeLog getConfigChangeLog(String path) throws Exception {
        return configChangeLogDao.getConfigChangeLog(path);
    }

    @Override
    public List<ConfigChangeLog> getAllConfigChangeLogs() throws Exception {
        return configChangeLogDao.getAllConfigChangeLogs();
    }
}
