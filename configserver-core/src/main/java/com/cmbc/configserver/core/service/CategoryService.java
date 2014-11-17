package com.cmbc.configserver.core.service;

import com.cmbc.configserver.domain.Category;

import java.util.List;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/17
 * @Time 10:33
 */
public interface CategoryService {
    public Category save(Category category) throws Exception;

    public boolean update(Category category) throws Exception;

    public boolean delete(Category category) throws Exception;

    public List<Category> getAllCategory() throws Exception;

    public List<String> getResources(String cell) throws Exception;

    public List<String> getTypes(String cell, String resource) throws Exception;

    /**
     * get the category according to the specified cell/resource/type
     *
     * @param cell     the category's cell
     * @param resource the category's resource
     * @param type     the category's type
     * @return the category which is applied for the query condition
     * @throws Exception
     */
    public Category getCategory(String cell, String resource, String type) throws Exception;

    /**
     * get the category by specified id
     *
     * @param categoryId the specified id
     * @return the category which is applied for the query condition
     * @throws Exception
     */
    public Category getCategoryById(int categoryId) throws Exception;

    public List<Category> getCategories(Object[] categoryIds) throws Exception;
}
