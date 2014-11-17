package com.cmbc.configserver.core.service;

import com.cmbc.configserver.domain.Category;
import com.cmbc.configserver.domain.Configuration;

import java.util.List;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/17
 * @Time 10:34
 */
public interface ConfigDetailsService {
    public Configuration save(Configuration config) throws Exception;

    public boolean update(Configuration config) throws Exception;

    public boolean delete(Configuration config) throws Exception;

    public List<Configuration> getConfigurationList(Category category) throws Exception;

    public List<Configuration> getConfigurationListByClientId(String clientId) throws Exception;

    public boolean deleteConfigurationByClientId(String clientId) throws Exception;

    public List<Integer> getCategoryIdsByClientId(String clientId) throws Exception;
}
