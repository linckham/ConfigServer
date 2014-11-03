package com.cmbc.configserver.utils;

import com.cmbc.configserver.domain.Category;
import com.cmbc.configserver.domain.Configuration;
import org.apache.commons.lang3.StringUtils;

/**
 * the helper class that use to operate the path
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @author tongchuan.lin<linckham@gmail.com>.
 *         Date: 2014/10/24
 *         Time: 14:26
 */
public class PathUtils {
    /**
     * get the path which is interested by the subscriber
     * @param config the subscriber's configuration
     * @return the interested path of the subscriber
     */
    public static String getSubscriberPath(Configuration config){

        if(null == config){
            throw new IllegalArgumentException("configuration can not be null!");
        }

        if(StringUtils.isBlank(config.getCell())){
            throw new IllegalArgumentException("configuration's cell can not be null or empty!");
        }

        StringBuilder pathBuilder = new StringBuilder(64);
        pathBuilder.append(Constants.PATH_SEPARATOR).append(config.getCell());
        if(StringUtils.isNotBlank(config.getResource())){
            pathBuilder.append(Constants.PATH_SEPARATOR).append(config.getResource());
            if(StringUtils.isNotBlank(config.getType())){
                pathBuilder.append(Constants.PATH_SEPARATOR).append(config.getType());
            }
        }
        return pathBuilder.toString();
    }

    /**
     * according the path,get the specified category object
     * @param path the specified path
     * @return the Category of the path
     */
    public static  Category path2Category(String path){
        if (StringUtils.isBlank(path)) {
            throw new IllegalArgumentException("path can not be null or empty!");
        }

        if (!path.startsWith("/")) {
            path = new StringBuilder(path.length() + 1).append("/").append(path).toString();
        }

        //split the path
        String[] paths = path.split("/");

        if (paths.length != 4) {
            throw new IllegalArgumentException(String.format("%s format is invalid. the correct format is /XXX/XXX/XXX", path));
        }

        Category category = new Category();
        category.setCell(paths[1]);
        category.setResource(paths[2]);
        category.setType(paths[3]);
        return category;
    }

    /**
     * according the path,get the specified configuration object
     *
     * @param path the specified path
     * @return the Configuration of the path
     */
    public static Configuration path2Configuration(String path) {
        Category category = path2Category(path);
        if (null != category) {
            Configuration config = new Configuration();
            config.setCell(category.getCell());
            config.setResource(category.getResource());
            config.setType(category.getType());
            return config;
        }
        return null;
    }

    /**
     * according the category,get the specified path
     *
     * @param category the specified category
     * @return the path of the category
     */
    public static String category2Path(Category category) {
        if (null == category) {
            throw new IllegalArgumentException("category can not be null!");
        }

        if (StringUtils.isBlank(category.getCell())) {
            throw new IllegalArgumentException("category's cell can not be null or empty!");
        }

        StringBuilder pathBuilder = new StringBuilder(64);
        pathBuilder.append(Constants.PATH_SEPARATOR).append(category.getCell());
        if (StringUtils.isNotBlank(category.getResource())) {
            pathBuilder.append(Constants.PATH_SEPARATOR).append(category.getResource());
            if (StringUtils.isNotBlank(category.getType())) {
                pathBuilder.append(Constants.PATH_SEPARATOR).append(category.getType());
            }
        }
        return pathBuilder.toString();
    }

}