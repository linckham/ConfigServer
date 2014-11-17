package com.cmbc.configserver.core.service.impl;

import com.cmbc.configserver.core.event.Event;
import com.cmbc.configserver.core.event.EventType;
import com.cmbc.configserver.core.notify.ConfigChangedNotifyService;
import com.cmbc.configserver.core.notify.NotifyService;
import com.cmbc.configserver.core.service.CategoryService;
import com.cmbc.configserver.core.service.ConfigChangeLogService;
import com.cmbc.configserver.core.service.ConfigDetailsService;
import com.cmbc.configserver.core.service.ConfigServerService;
import com.cmbc.configserver.core.subscriber.SubscriberService;
import com.cmbc.configserver.domain.Category;
import com.cmbc.configserver.domain.ConfigChangeLog;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.utils.ConfigServerLogger;
import com.cmbc.configserver.utils.PathUtils;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * the implementation of ConfigServerService
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/31
 * @Time 11:12
 */
@Service("configServerService")
public class ConfigServerServiceImpl implements ConfigServerService {
    @Autowired
    private NotifyService notifyService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ConfigDetailsService configDetailsService;
    @Autowired
    private ConfigChangeLogService configChangeLogService;
    @Autowired
    private ConfigChangedNotifyService configChangedNotifyService;
    @Autowired
    private SubscriberService subscriberService;

    public void setSubscriberService(SubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public void setConfigDetailsService(ConfigDetailsService configDetailsService) {
        this.configDetailsService = configDetailsService;
    }

    public void setConfigChangeLogService(ConfigChangeLogService configChangeLogService) {
        this.configChangeLogService = configChangeLogService;
    }

    public void setConfigChangedNotifyService(ConfigChangedNotifyService configChangedNotifyService) {
        this.configChangedNotifyService = configChangedNotifyService;
    }

    public void setNotifyService(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    /**
     * publish the configuration to the config server
     *
     * @param config the configuration that will being stored in server
     * @return true if publish successfully,else false
     */
    @Override
    public boolean publish(Configuration config) throws Exception {
        validConfiguration(config);
        //check the category where exists
        Category category = this.categoryService.getCategory(config.getCell(), config.getResource(), config.getType());
        if (Category.EMPTY_MESSAGE == category) {
            Category tmpCategory = new Category();
            tmpCategory.setCell(config.getCell());
            tmpCategory.setResource(config.getResource());
            tmpCategory.setType(config.getType());
            // add the category if not exists
            category = this.categoryService.save(tmpCategory);
            //publish the event
            Event event = new Event();
            event.setEventType(EventType.CATEGORY_CHANGED);
            event.setEventSource(category);
            event.setEventCreatedTime(System.currentTimeMillis());
            this.notifyService.publish(event);
        }

        config.setCategoryId(category.getId());

        config = this.configDetailsService.save(config);
        //save the configuration success
        if (config.getId() > 0) {
            //update the category's last modify time
            updateLastModifyTime(PathUtils.getSubscriberPath(config), true);
        }
        boolean bSuccess = config.getId() > 0;
        if (bSuccess) {
            //publish the event
            Event event = new Event();
            event.setEventType(EventType.PUBLISH);
            event.setEventSource(config);
            event.setEventCreatedTime(System.currentTimeMillis());
            this.notifyService.publish(event);
        }
        return bSuccess;
    }

    /**
     * unPublish the configuration to the config server
     *
     * @param config the configuration that will being removed in server
     * @return true if unPublish successfully,else false
     */
    @Override
    public boolean unPublish(Configuration config) throws Exception {
        validConfiguration(config);
        Category category = this.categoryService.getCategory(config.getCell(), config.getResource(), config.getType());
        config.setCategoryId(category.getId());

        boolean bDelConfig = this.configDetailsService.delete(config);

        if (bDelConfig) {
            //update the change log
            updateLastModifyTime(PathUtils.getSubscriberPath(config), false);
        }
        if (bDelConfig) {
            //publish the unPublish event
            Event event = new Event();
            event.setEventType(EventType.UN_PUBLISH);
            event.setEventSource(config);
            event.setEventCreatedTime(System.currentTimeMillis());
            this.notifyService.publish(event);
        }
        return bDelConfig;
    }

    /**
     * subscribe the specified configuration which is in the config server
     *
     * @param config  the configuration that will being subscribed
     * @param channel the subscriber's channel
     * @return true if subscribed successfully,else false
     */
    @Override
    public boolean subscribe(Configuration config, Channel channel) throws Exception {
        validConfiguration(config);
        return this.subscriberService.subscribe(PathUtils.getSubscriberPath(config), channel);
    }

    /**
     * unSubscribe the specified configuration which is in the config server
     *
     * @param config  the configuration that will being unSubscribed
     * @param channel the subscriber's channel
     * @return true if unSubscribed successfully,else false
     */
    @Override
    public boolean unSubscribe(Configuration config, Channel channel) throws Exception {
        validConfiguration(config);
        return this.subscriberService.unSubcribe(PathUtils.getSubscriberPath(config), channel);
    }

    @Override
    public void start() throws Exception {
        this.notifyService.start();
    }

    @Override
    public void shutdown() {
        this.notifyService.stop();
    }

    private void validConfiguration(Configuration config) throws Exception {
        if (null == config) {
            throw new NullPointerException("configuration is null");
        }
        if (StringUtils.isBlank(config.getCell())) {
            throw new IllegalArgumentException("config cell is null or empty!");
        }
    }

    /**
     * delete the configuration list by the specified client id
     *
     * @param clientId the client id which the configuration items belongs to
     * @return true if deleted success,else false
     * @throws Exception
     */
    @Override
    public boolean deleteConfigurationByClientId(String clientId) throws Exception {
        List<Integer> categoryIds = this.configDetailsService.getCategoryIdsByClientId(clientId);
        if (null == categoryIds || categoryIds.isEmpty()) {
            return false;
        }

        boolean bDeleteConfigs = this.configDetailsService.deleteConfigurationByClientId(clientId);
        List<String> paths = null;
        if (bDeleteConfigs) {
            List<Category> categories = this.categoryService.getCategories(categoryIds.toArray());
            if (null != categories && !categories.isEmpty()) {
                paths = new ArrayList<String>();
                for (Category category : categories) {
                    String path = PathUtils.category2Path(category);
                    updateLastModifyTime(path, false);
                    paths.add(path);
                }
            }
        }
        if (null == paths || paths.isEmpty()) {
            return false;
        }

        for (String path : paths) {
            //publish the PATH_DATA_CHANGED event
            Event event = new Event();
            event.setEventType(EventType.PATH_DATA_CHANGED);
            event.setEventSource(path);
            event.setEventCreatedTime(System.currentTimeMillis());
            this.notifyService.publish(event);
        }
        return true;
    }

    /**
     * get the configuration list by the specified configuration
     *
     * @param config the specified configuration
     * @return the configuration list
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Configuration> getConfigurationList(Configuration config) throws Exception {
        if (null == config) {
            return Collections.EMPTY_LIST;
        }
        //select the specified category's all configuration
        if (StringUtils.isNotBlank(config.getResource()) && StringUtils.isNotBlank(config.getType())) {
            Category category = this.categoryService.getCategory(config.getCell(), config.getResource(), config.getType());
            if (null == category) {
                return Collections.EMPTY_LIST;
            }
            List<Configuration> configurationList = this.configDetailsService.getConfigurationList(category);
            if (null == configurationList) {
                return Collections.EMPTY_LIST;
            }
            return configurationList;
        }

        //select the specified cell's all resources
        if (StringUtils.isBlank(config.getResource()) && StringUtils.isBlank(config.getType())) {
            List<String> resources = this.categoryService.getResources(config.getCell());
            //build configuration item
            if (null != resources && !resources.isEmpty()) {
                List<Configuration> configs = new ArrayList<Configuration>(resources.size());
                for (String resource : resources) {
                    Configuration temp = new Configuration();
                    temp.setCell(config.getCell());
                    temp.setResource(resource);
                    configs.add(temp);
                }
                return configs;
            }
            return Collections.EMPTY_LIST;
        }

        return Collections.EMPTY_LIST;
    }

    /**
     * update the md5 of the specified category
     */
    private void updateLastModifyTime(String path, boolean isPublish) {
        try {
            ConfigChangeLog changeLog = new ConfigChangeLog();
            changeLog.setLastModifiedTime(System.currentTimeMillis());
            changeLog.setPath(path);
            boolean bChangeLog;
            //only in the publish case, we should check the path where exists in config_change_log
            if (isPublish) {
                ConfigChangeLog dbChangeLog = this.configChangeLogService.getConfigChangeLog(path);
                if (ConfigChangeLog.EMPTY_MESSAGE != dbChangeLog) {
                    bChangeLog = this.configChangeLogService.update(changeLog);
                } else {
                    bChangeLog = this.configChangeLogService.add(changeLog);
                }
            } else {
                bChangeLog = this.configChangeLogService.update(changeLog);
            }

            if (!bChangeLog) {
                ConfigServerLogger.warn(String.format("add/update the path %s's last_modify_time failed.", path));
            } else {
                this.configChangedNotifyService.updatePathMd5Cache(path, changeLog.getLastModifiedTime());
            }
        } catch (Exception ex) {
            ConfigServerLogger.warn(String.format("update the md5 of the path [%]  failed.", path), ex);
        }
    }
}