package com.cmbc.configserver.core.dao;

import com.cmbc.configserver.domain.ConfigChangeLog;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/30
 * @Time 19:45
 */
public interface ConfigChangeLogDao {
    public boolean add(ConfigChangeLog changeLog) throws Exception;

    public boolean update(ConfigChangeLog changeLog) throws Exception;

    public ConfigChangeLog getConfigChangeLog(String path) throws Exception;
}
