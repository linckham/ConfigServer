package com.cmbc.configserver.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

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
    }

    @Test
    public void testGetConfigServerAddressList(){
        //remove the key.
        System.getProperties().remove(Constants.CONFIG_SERVER_ADDRESS_FILE_NAME_KEY);
        String configServerAddressFile = ConfigUtils.getProperty(Constants.CONFIG_SERVER_ADDRESS_FILE_NAME_KEY,Constants.DEFAULT_CONFIG_SERVER_ADDRESS_FILE_NAME);;
        List<String> addressList = ConfigUtils.getConfigServerAddressList(configServerAddressFile,Constants.CONFIG_SERVER_ADDRESS_KEY);
        System.out.println(configServerAddressFile);
        Assert.assertNotNull(addressList);
        Assert.assertEquals(1, addressList.size());
        Assert.assertEquals("127.0.0.1:19999", addressList.get(0));
    }

    @Test
    public void testMultiAddress() {
        String tempDirectoryKey = "java.io.tmpdir";
        String addressFile = ConfigUtils.getProperty(tempDirectoryKey, "") + "tmp.properties";
        System.setProperty(Constants.CONFIG_SERVER_ADDRESS_FILE_NAME_KEY, addressFile);
        String configServerAddressFile = ConfigUtils.getProperty(Constants.CONFIG_SERVER_ADDRESS_FILE_NAME_KEY, Constants.DEFAULT_CONFIG_SERVER_ADDRESS_FILE_NAME);
        System.out.println(configServerAddressFile);
        File tempFile = new File(configServerAddressFile);
        if (!tempFile.exists()) {
            try {
                tempFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Assert.assertEquals(true, tempFile.exists());
        }
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(tempFile);
            StringBuilder contentBuilder = new StringBuilder(128);
            contentBuilder.append(Constants.CONFIG_SERVER_ADDRESS_KEY);
            contentBuilder.append("=");
            contentBuilder.append("127.0.0.1:19999").append(Constants.COMMA_SEPARATOR);
            contentBuilder.append("127.0.0.1:20000").append(Constants.COMMA_SEPARATOR);
            contentBuilder.append("127.0.0.1:20001").append(Constants.COMMA_SEPARATOR);
            outputStream.write(contentBuilder.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != outputStream){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        List<String> addressList = ConfigUtils.getConfigServerAddressList(configServerAddressFile,Constants.CONFIG_SERVER_ADDRESS_KEY);
        Assert.assertNotNull(addressList);
        Assert.assertEquals(3, addressList.size());
        Assert.assertEquals("127.0.0.1:19999", addressList.get(0));
        Assert.assertEquals("127.0.0.1:20000", addressList.get(1));

        //delete temp file
        tempFile.delete();
        addressList = ConfigUtils.getConfigServerAddressList(configServerAddressFile,Constants.CONFIG_SERVER_ADDRESS_KEY);
        Assert.assertNotNull(addressList);
        Assert.assertEquals(0, addressList.size());
    }
}
