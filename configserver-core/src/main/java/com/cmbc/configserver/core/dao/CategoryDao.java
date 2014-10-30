package com.cmbc.configserver.core.dao;

import com.cmbc.configserver.domain.Category;

import java.util.List;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/30
 * @Time 15:43
 */
public interface CategoryDao {
    public boolean save(Category category) throws Exception;

    public boolean update(Category category) throws Exception;

    public boolean delete(Category category) throws Exception;

    public List<Category> getAllCategory() throws Exception;

    public List<String> getResources(String cell) throws Exception;

    public List<String> getTypes(String cell, String resource) throws Exception;
}
