package com.cmbc.configserver.core.service.impl;

import com.cmbc.configserver.core.dao.ConfigDetailsDao;
import com.cmbc.configserver.core.service.ConfigDetailsService;
import com.cmbc.configserver.domain.Category;
import com.cmbc.configserver.domain.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/17
 * @Time 10:44
 */
@Service("configDetailsService")
public class ConfigDetailsServiceImpl implements ConfigDetailsService {
    @Autowired
    private ConfigDetailsDao configDetailsDao;

    public void setConfigDetailsDao(ConfigDetailsDao configDetailsDao) {
        this.configDetailsDao = configDetailsDao;
    }


    @Override
    public Configuration save(Configuration config) throws Exception {
        return configDetailsDao.save(config);
    }

    @Override
    public boolean update(Configuration config) throws Exception {
        return configDetailsDao.update(config);
    }

    @Override
    public boolean delete(Configuration config) throws Exception {
        return configDetailsDao.delete(config);
    }

    @Override
    public List<Configuration> getConfigurationList(Category category) throws Exception {
        return configDetailsDao.getConfigurationList(category);
    }

    @Override
    public List<Configuration> getConfigurationListByClientId(String clientId) throws Exception {
        return configDetailsDao.getConfigurationListByClientId(clientId);
    }

    @Override
    public boolean deleteConfigurationByClientId(String clientId) throws Exception {
        return configDetailsDao.deleteConfigurationByClientId(clientId);
    }

    @Override
    public List<Integer> getCategoryIdsByClientId(String clientId) throws Exception {
        return configDetailsDao.getCategoryIdsByClientId(clientId);
    }
}
