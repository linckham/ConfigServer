package com.cmbc.configserver.core.dao;

import com.cmbc.configserver.domain.Configuration;

import java.util.List;
import com.cmbc.configserver.domain.Category;

/**
 * the interface uses to manage table of the configuration details.<br/>
 * It provides the basic CRUD API of config_details.<br/>
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/28
 * @Time 15:47
 */
public interface ConfigDetailsDao {
    public Configuration save(Configuration config) throws Exception;

    public boolean update(Configuration config) throws Exception;

    public boolean delete(Configuration config) throws Exception;

    public List<Configuration> getConfigurationList(Category category) throws Exception;

    public List<Configuration> getConfigurationListByClientId(String clientId) throws Exception;
}
