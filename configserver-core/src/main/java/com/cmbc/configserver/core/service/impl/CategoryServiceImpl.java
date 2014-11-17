package com.cmbc.configserver.core.service.impl;

import com.cmbc.configserver.core.dao.CategoryDao;
import com.cmbc.configserver.core.service.CategoryService;
import com.cmbc.configserver.domain.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/17
 * @Time 10:37
 */
@Service("categoryService")
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryDao categoryDao;

    public void setCategoryDao(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }


    @Override
    public Category save(Category category) throws Exception {
        return categoryDao.save(category);
    }

    @Override
    public boolean update(Category category) throws Exception {
        return categoryDao.update(category);
    }

    @Override
    public boolean delete(Category category) throws Exception {
        return false;
    }

    @Override
    public List<Category> getAllCategory() throws Exception {
        return categoryDao.getAllCategory();
    }

    @Override
    public List<String> getResources(String cell) throws Exception {
        return categoryDao.getResources(cell);
    }

    @Override
    public List<String> getTypes(String cell, String resource) throws Exception {
        return categoryDao.getTypes(cell, resource);
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
        return categoryDao.getCategory(cell, resource, type);
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
        return categoryDao.getCategoryById(categoryId);
    }

    @Override
    public List<Category> getCategories(Object[] categoryIds) throws Exception {
        return categoryDao.getCategories(categoryIds);
    }
}
