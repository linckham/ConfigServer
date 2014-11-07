package com.cmbc.configserver.utils;

import com.cmbc.configserver.domain.Category;
import org.junit.Assert;
import org.junit.Test;

/**
 * the test class of PathUtils
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/30
 * @Time 15:08
 */
public class PathUtilsTest {
    @Test
    public void testPath2Category() {
        String path = "/dubbo/serviceA/providers";
        Category category = PathUtils.path2Category(path);
        Assert.assertNotNull(category);
        Assert.assertEquals("dubbo", category.getCell());
    }

    @Test
    public void testInvalidPath() {
        String path = "dubbo/serviceA";
        Exception ex = null;
        try {
            PathUtils.path2Category(path);
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex instanceof IllegalArgumentException);
    }

    @Test
    public void testEmptyPath() {
        String path = "";
        Exception ex = null;
        try {
            PathUtils.path2Category(path);
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex instanceof IllegalArgumentException);
        Assert.assertEquals("path can not be null or empty!",ex.getMessage());
    }
}
