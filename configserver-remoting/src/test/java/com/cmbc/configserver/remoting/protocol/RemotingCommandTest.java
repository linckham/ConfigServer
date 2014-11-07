package com.cmbc.configserver.remoting.protocol;

import com.cmbc.configserver.common.RemotingSerializable;
import com.cmbc.configserver.common.protocol.RequestCode;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.utils.Constants;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/7
 * @Time 10:03
 */
public class RemotingCommandTest {
    private final String TEST_CELL = "test-compress";

    @Test
    public void testEncode() throws Exception {
        RemotingCommand command = RemotingCommand.createRequestCommand(RequestCode.PUBLISH_CONFIG);
        Configuration config = this.createConfiguration();
        byte[] body = RemotingSerializable.encode(config);
        Assert.assertNotNull(body);
        System.out.println(String.format("before FAST JSON encoding,the length of configuration content content is %s", config.getContent().length()));
        command.setBody(body);
        Assert.assertNotNull(command.getBody());
        System.out.println(String.format("after compress,the length of configuration content is %s ", command.getBody().length));

        //encode header
        ByteBuffer buffer = command.encodeHeader();
        Assert.assertNotNull(buffer);
        Assert.assertEquals(Constants.MAGIC_CODE, buffer.getShort());
    }

    @Test
    public void testDecode() throws Exception {
        RemotingCommand command = RemotingCommand.createRequestCommand(RequestCode.PUBLISH_CONFIG);
        Configuration config = this.createConfiguration();
        byte[] body = RemotingSerializable.encode(config);
        command.setBody(body);
        //encode header
        ByteBuffer headerBuffer = command.encodeHeader();
        int bodyLength = command.getBody().length;
        //write the header and body to ByteBuffer
        ByteBuffer packetBuffer = ByteBuffer.allocate(headerBuffer.capacity() + bodyLength);
        packetBuffer.put(headerBuffer);
        packetBuffer.put(command.getBody());
        packetBuffer.flip();

        //decode the ByteBuffer
        RemotingCommand decodeCommand = RemotingCommand.decode(packetBuffer);
        Assert.assertNotNull(decodeCommand);
        Assert.assertEquals(RequestCode.PUBLISH_CONFIG, decodeCommand.getCode());

        Configuration decodeConfig = RemotingSerializable.decode(decodeCommand.getBody(), Configuration.class);
        Assert.assertNotNull(decodeConfig);
        Assert.assertEquals(TEST_CELL, decodeConfig.getCell());
    }

    @Test
    public void testInvalidMagicCode() {
        System.out.println("------begin testInvalidMagicCode-------");
        ByteBuffer packetBuffer = ByteBuffer.allocate(Constants.MIN_PACKET_LENGTH);
        packetBuffer.putShort((short) 0xedff);
        packetBuffer.putInt(0);
        packetBuffer.putShort((short) 2);
        packetBuffer.put(new byte[2]);
        packetBuffer.put((byte) 1);
        packetBuffer.put((byte) 0);
        packetBuffer.flip();
        //decode the ByteBuffer
        try {
            RemotingCommand decodeCommand = RemotingCommand.decode(packetBuffer);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("request/response command magic code is invalid"));
            System.out.println(e.getMessage());
        }
        System.out.println("------end testInvalidMagicCode-------");
    }

    @Test
    public void testNoBodyPacket() throws Exception {
        RemotingCommand command = RemotingCommand.createRequestCommand(RequestCode.PUBLISH_CONFIG);
        command.setBody(null);
        Assert.assertNull(command.getBody());

        //encode header
        ByteBuffer buffer = command.encodeHeader();
        Assert.assertNotNull(buffer);
        Assert.assertEquals(Constants.MAGIC_CODE, buffer.getShort());
    }

    @Test
    public void testMaxBodyPacket() {
        System.out.println("------begin testMaxBodyPacket-------");
        RemotingCommand command = RemotingCommand.createRequestCommand(RequestCode.PUBLISH_CONFIG);
        byte[] body = new byte[Constants.MAX_PACKET_LENGTH + 1];
        Random random = new Random();
        for (int i=0;i<body.length;i++){
            body[i] = (byte)random.nextInt(255);
        }

        command.setBody(body);
        //encode header
        try {
            command.encodeHeader();
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("request/response packet's body length is invalid."));
            System.out.println(e.getMessage());
        }
        System.out.println("------end testMaxBodyPacket-------");
    }

    private Configuration createConfiguration() {
        Configuration config = new Configuration();
        config.setCell(TEST_CELL);
        config.setResource("quick-lz");
        config.setType("compress");
        config.setCreateTime(System.currentTimeMillis());
        config.setClientId("compress-quicklz-1");
        config.setContent(createContent());
        return config;
    }

    private String createContent() {
        return "`15`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671b6253625`15625362563`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKllllll`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671bjdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf0830`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671b4--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671b528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671b63528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoro`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671bpqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfd`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671bjfka8671b`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-0`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671b9+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671b--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671b`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671b3473dfjdkfdjfka8671b";
    }
}