package com.cmbc.configserver.core.dao;

import com.cmbc.configserver.domain.ConfigClientMapping;

import java.util.List;

/**
 * the interface uses to manage the mapping between the client connection id and configuration id
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/30
 * @Time 17:59
 */
public interface ConfigClientMappingDao {
    public boolean save(ConfigClientMapping ccMapping) throws Exception;

    public boolean delete(ConfigClientMapping ccMapping) throws Exception;

    /**
     * get the specified client id's all configuration id list
     *
     * @param clientId the specified client id
     * @throws Exception
     */
    public List<ConfigClientMapping> getMappingList(String clientId) throws Exception;
}