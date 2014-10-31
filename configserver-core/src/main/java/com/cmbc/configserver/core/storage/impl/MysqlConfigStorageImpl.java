package com.cmbc.configserver.core.storage.impl;

import com.cmbc.configserver.core.dao.CategoryDao;
import com.cmbc.configserver.core.dao.ConfigChangeLogDao;
import com.cmbc.configserver.core.dao.ConfigDetailsDao;
import com.cmbc.configserver.domain.Category;
import com.cmbc.configserver.domain.ConfigChangeLog;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.utils.ClusterSignAlgorithm;
import com.cmbc.configserver.utils.ConfigServerLogger;
import com.cmbc.configserver.utils.PathUtils;

import java.util.Collections;
import java.util.List;

/**
 * the implementation of the ConfigStorage that use mysql to storage the configuration.<br/>
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/31
 * @Time 11:12
 */
public class MysqlConfigStorageImpl extends AbstractConfigStorage{
    private ConfigDetailsDao configDao;
    private CategoryDao categoryDao;
    private ConfigChangeLogDao configChangeLogDao;

    public void setConfigChangeLogDao(ConfigChangeLogDao configChangeLogDao) {
        this.configChangeLogDao = configChangeLogDao;
    }

    public void setCategoryDao(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    public void setConfigDao(ConfigDetailsDao configDao){
        this.configDao = configDao;
    }

    /**
     * publish the configuration to the config server
     *
     * @param config the configuration that will being stored in server
     * @return true if publish successfully,else false
     */
    public boolean publish(Configuration config) throws Exception{
        if(null == config){
            return false;
        }
        //check the category where exists
        Category category = this.categoryDao.getCategory(config.getCell(),config.getResource(),config.getType());
        if(null == category){
            Category tmpCategory =  new Category();
            tmpCategory.setCell(config.getCell());
            tmpCategory.setResource(config.getResource());
            tmpCategory.setType(config.getType());
            // add the category if not exists
            category = this.categoryDao.save(tmpCategory);
        }

        config.setCategoryId(category.getId());

        config = this.configDao.save(config);
        //save the configuration success
        if(config.getId() > 0){
            //update the category's md5
            updateMd5(category,PathUtils.getSubscriberPath(config));
        }

        return config.getId() > 0;
    }

    /**
     * update the md5 of the specified category
     *
     * @param category
     */
    private void updateMd5(Category category, String path) {
        try{
            //calculate the /cell/resource/type md5
            List<Configuration> configList = this.configDao.getConfigurationList(category);
            String md5 = ClusterSignAlgorithm.calculate(configList);
            ConfigChangeLog changeLog = new ConfigChangeLog();
            changeLog.setMd5(md5);
            changeLog.setPath(path);
            boolean bChangeLog = this.configChangeLogDao.add(changeLog);
            if (!bChangeLog) {
                ConfigServerLogger.warn(String.format("add/update the path %s's md5 %s failed.", path, md5));
                //TODO:add a event,repair this async in backend thread
            }
            //TODO:send a event to Notify thread,
        }
        catch(Exception ex){
            ConfigServerLogger.warn(String.format("update the md5 of the path [%]  failed.",path),ex);
        }
    }

    /**
     * unPublish the configuration to the config server
     *
     * @param config the configuration that will being removed in server
     * @return true if unPublish successfully,else false
     */
    public boolean unPublish(Configuration config) throws Exception{
        if(null == config){
            return false;
        }

        Category category = this.categoryDao.getCategory(config.getCell(),config.getResource(),config.getType());
        config.setCategoryId(category.getId());

        boolean bDelConfig = this.configDao.delete(config);

        if(bDelConfig){
            //update the change log
            updateMd5(category,PathUtils.getSubscriberPath(config));
        }

        return bDelConfig;
    }

    /**
     * get the configuration list by the specified configuration
     * @param config the specified configuration
     * @return the configuration list
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Configuration> getConfigurationList(Configuration config) throws Exception{
        if(null == config){
            return Collections.EMPTY_LIST;
        }

        Category category = this.categoryDao.getCategory(config.getCell(),config.getResource(),config.getType());
        if(null ==  category){
            return Collections.EMPTY_LIST;
        }

        return this.configDao.getConfigurationList(category);
    }
}
