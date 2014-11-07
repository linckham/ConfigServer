package com.cmbc.configserver.common.compress;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/6
 * @Time 22:36
 */
public class QuickLZTest {
    @Test
    public void testQuickLzCompress() {
        Compress compress = CompressFactory.createCompress(CompressType.QUICK_LZ);
        Assert.assertNotNull(compress);
        String sourceData = createSource();
        int sourceLength = sourceData.length();
        byte[] compressData = compress.compress(sourceData.getBytes());
        Assert.assertNotNull(compressData);
        int compressLength = compressData.length;
        System.out.println(String.format("the length before compressed is %s, after compress is %s", sourceLength, compressLength));

        byte[] decompressData = compress.decompress(compressData);
        Assert.assertNotNull(decompressData);
        Assert.assertEquals(sourceLength,decompressData.length);
        String decompressStr = new String(decompressData);
        System.out.println(decompressStr);
        Assert.assertEquals(sourceData,decompressStr);
    }

    private String createSource() {
        return "`15`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671b6253625`15625362563`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKllllll`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671bjdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf0830`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671b4--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671b528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671b63528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoro`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671bpqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfd`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671bjfka8671b`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-0`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671b9+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671b--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671b`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671b3473dfjdkfdjfka8671b";
    }
}
