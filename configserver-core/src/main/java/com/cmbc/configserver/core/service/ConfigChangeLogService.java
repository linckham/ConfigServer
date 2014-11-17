package com.cmbc.configserver.core.service;

import com.cmbc.configserver.domain.ConfigChangeLog;

import java.util.List;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/17
 * @Time 10:33
 */
public interface ConfigChangeLogService {
    public boolean add(ConfigChangeLog changeLog) throws Exception;

    public boolean update(ConfigChangeLog changeLog) throws Exception;

    public ConfigChangeLog getConfigChangeLog(String path) throws Exception;

    public List<ConfigChangeLog> getAllConfigChangeLogs() throws Exception;
}
