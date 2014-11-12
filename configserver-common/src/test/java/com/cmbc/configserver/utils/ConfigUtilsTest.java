package com.cmbc.configserver.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/12
 * @Time 15:43
 */
public class ConfigUtilsTest {
    @Test
    public void testDefaultConfigServerAddressFile(){
        String fileName = Constants.DEFAULT_CONFIG_SERVER_ADDRESS_FILE_NAME;
        Assert.assertNotNull(fileName);
        System.out.println(fileName);
    }
}
