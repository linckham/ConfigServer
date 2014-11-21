package com.cmbc.configserver.core.service.impl;

import com.cmbc.configserver.common.ThreadFactoryImpl;
import com.cmbc.configserver.common.cache.local.Cache;
import com.cmbc.configserver.common.cache.local.CacheFactory;
import com.cmbc.configserver.core.dao.CategoryDao;
import com.cmbc.configserver.core.event.Event;
import com.cmbc.configserver.core.event.EventService;
import com.cmbc.configserver.core.event.EventType;
import com.cmbc.configserver.core.service.CategoryService;
import com.cmbc.configserver.domain.Category;
import com.cmbc.configserver.utils.ConcurrentHashSet;
import com.cmbc.configserver.utils.ConfigServerLogger;
import com.cmbc.configserver.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/17
 * @Time 10:37
 */
@Service("categoryService")
public class CategoryServiceImpl implements CategoryService, InitializingBean, DisposableBean {
    private static final long LOCAL_CACHE_LIFE_TIME = 1000 * 60 * 30;
    private final Cache<Integer, Category> categoryCache = CacheFactory.createCache("category.cache", LOCAL_CACHE_LIFE_TIME);
    private ConcurrentHashMap</*cell*/String, ConcurrentHashSet<String>> categoryMap = new ConcurrentHashMap<String, ConcurrentHashSet<String>>(Constants.DEFAULT_INITIAL_CAPACITY);
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, new ThreadFactoryImpl("category-changed-"));

    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private EventService<Event> eventService;

    public void start(){
        this.scheduler.scheduleAtFixedRate(new CategoryWorker(),0,3*60*1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public Category save(Category category) throws Exception {
        category = categoryDao.save(category);
        if(category.getId() > 0){
            this.updateCategoryCache(category);
            this.categoryCache.put(category.getId(),category);
        }
        return category;
    }

    @Override
    public boolean update(Category category) throws Exception {
        boolean bUpdate = categoryDao.update(category);
        if(bUpdate){
            this.categoryCache.put(category.getId(), category);
        }
        return bUpdate;
    }

    @Override
    public boolean delete(Category category) throws Exception {
        boolean bDelete = categoryDao.delete(category);
        if(bDelete){
            this.categoryCache.remove(category.getId());
        }
        return bDelete;
    }

    @Override
    public List<Category> getAllCategory() throws Exception {
        return categoryDao.getAllCategory();
    }

    @Override
    public List<String> getResources(String cell) throws Exception {
        List<String> resourceList = new ArrayList<String>();
        for (Category temp : this.categoryCache.values()) {
            if (StringUtils.equals(cell, temp.getCell())) {
                resourceList.add(temp.getResource());
            }
        }
        if (!resourceList.isEmpty()) {
            return resourceList;
        } else {
            List<String> resources = categoryDao.getResources(cell);
            //don't set the local cache
            ConfigServerLogger.warn(String.format("getResources of cell=%s from database,result is %s", cell, resources));
            return resources;
        }
    }

    @Override
    public List<String> getTypes(String cell, String resource) throws Exception {
        List<String> typeList = new ArrayList<String>();
        for (Category temp : this.categoryCache.values()) {
            if (StringUtils.equals(cell, temp.getCell())&&StringUtils.equals(resource,temp.getResource())) {
                typeList.add(temp.getType());
            }
        }
        if (!typeList.isEmpty()) {
            return typeList;
        } else {
            List<String> types = categoryDao.getTypes(cell, resource);
            //don't set the local cache
            ConfigServerLogger.warn(String.format("getTypes of cell=%s,resource=%s from database.result is %s",cell,resource,types));
            return types;
        }
    }

    /**
     * get the category according to the specified cell/resource/type
     *
     * @param cell     the category's cell
     * @param resource the category's resource
     * @param type     the category's type
     * @return the category which is applied for the query condition
     * @throws Exception
     */
    @Override
    public Category getCategory(String cell, String resource, String type) throws Exception {
        Category category = null;
        boolean bMatched = false;
        for (Category temp : this.categoryCache.values()) {
            if (StringUtils.equals(cell, temp.getCell()) &&
                    StringUtils.equals(resource, temp.getResource()) &&
                    StringUtils.equals(type, temp.getType())) {
                category = temp;
                bMatched = true;
                break;
            }
        }
        //find it in the local cache
        if(bMatched){
            return category;
        }

        category = this.categoryDao.getCategory(cell, resource, type);
        ConfigServerLogger.warn(String.format("getCategory of cell=%s,resource=%s,type=%s from database. result is %s ", cell, resource, type, category));
        if (null != category && Category.EMPTY_MESSAGE != category) {
            this.categoryCache.put(category.getId(), category);
        }
        return category;
    }

    /**
     * get the category by specified id
     *
     * @param categoryId the specified id
     * @return the category which is applied for the query condition
     * @throws Exception
     */
    @Override
    public Category getCategoryById(int categoryId) throws Exception {
        Category category = this.categoryCache.get(categoryId);
        if (null == category) {
            category = this.categoryDao.getCategoryById(categoryId);
            ConfigServerLogger.warn(String.format("getCategoryById of id=%s from database,result is %s", categoryId,category));
            if (null != category && Category.EMPTY_MESSAGE != category) {
                this.categoryCache.put(categoryId, category);
            }
        }
        return category;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public List<Category> getCategories(Object[] categoryIds) throws Exception {
        List<Category> categories = new ArrayList<Category>();
        int[] ids = new int[categoryIds.length];
        for (int i = 0; i < categoryIds.length; i++) {
            ids[i] = (Integer) categoryIds[i];
        }

        for (int id : ids) {
            boolean bMatched = false;
            for (Category temp : this.categoryCache.values()) {
                if (id == temp.getId()) {
                    categories.add(temp);
                    bMatched = true;//find it in local cache
                    break;
                }
            }
            //local cache doesn't have this category id,so get it from database;
            if (!bMatched) {
                Category temp = this.getCategoryById(id);
                if (null != temp && temp != Category.EMPTY_MESSAGE) {
                    categories.add(temp);
                }
            }
        }
        return categories;
    }

    @Override
    public void destroy() throws Exception {
        this.scheduler.shutdown();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    /**
     * update the category cache
     * @param category the category that will be update to cache
     */
    @SuppressWarnings("unchecked")
    private void updateCategoryCache(Category category){
        ConcurrentHashSet set = this.categoryMap.get(category.getCell());
        if (null == set) {
            categoryMap.putIfAbsent(category.getCell(), new ConcurrentHashSet<String>());
            set = categoryMap.get(category.getCell());
        }
        set.add(category.getResource());

    }

    class CategoryWorker implements  Runnable{
        @Override
        @SuppressWarnings("unchecked")
        public void run() {
            try{
                List<Category> categories = getAllCategory();
                ConfigServerLogger.info(String.format("CategoryWorker getAllCategory from database. size = %s, categories = %s",
                        categories ==null?0: categories.size(),categories));
                if(null != categories && !categories.isEmpty()){
                    for(Category category : categories){
                        //update the categoryCache period
                        categoryCache.put(category.getId(), category);
                        //check whether has new resource of the cell
                        ConcurrentHashSet set = categoryMap.get(category.getCell());
                        if (null == set) {
                            categoryMap.putIfAbsent(category.getCell(), new ConcurrentHashSet<String>());
                            set = categoryMap.get(category.getCell());
                        }
                        if (!set.contains(category.getResource())) {
                            ConfigServerLogger.warn(String.format("the cell %s has new resource %s", category.getCell(), category.getResource()));
                            //notify
                            Event event = new Event();
                            event.setEventType(EventType.CATEGORY_CHANGED);
                            event.setEventSource(category);
                            event.setEventCreatedTime(System.currentTimeMillis());
                            eventService.publish(event);
                        }
                        set.add(category.getResource());
                    }
                }
            }
            catch (Throwable t){
                ConfigServerLogger.warn("error happens when category worker running.", t);
            }
        }
    }
}
