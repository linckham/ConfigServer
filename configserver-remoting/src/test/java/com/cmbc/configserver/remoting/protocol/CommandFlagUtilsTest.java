package com.cmbc.configserver.remoting.protocol;

import com.cmbc.configserver.common.Version;
import com.cmbc.configserver.common.compress.CompressType;
import com.cmbc.configserver.common.serialize.SerializeType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/6
 * @Time 16:08
 */
public class CommandFlagUtilsTest {
    private RemotingHeader header;

    @Before
    public void setUp() {
        header = createRemotingHeader();
    }

    @Test
    public void testSetSerializeType() {
        int flag = header.getFlag();
        Assert.assertEquals(0, flag);
        CommandFlagUtils.setSerializeType(header, SerializeType.FAST_JSON);
        flag = header.getFlag();
        Assert.assertEquals(0, flag);
        CommandFlagUtils.setSerializeType(header, SerializeType.HESSIAN);
        flag = header.getFlag();
        Assert.assertEquals(32, flag);

        CommandFlagUtils.setSerializeType(header, SerializeType.THRIFT);
        flag = header.getFlag();
        Assert.assertEquals(48, flag);
    }

    @Test
    public void testGetSerializeType() {
        CommandFlagUtils.setSerializeType(header, SerializeType.FAST_JSON);
        SerializeType type;
        type = CommandFlagUtils.getSerializeType(header);
        Assert.assertEquals(SerializeType.FAST_JSON,type);

        CommandFlagUtils.setSerializeType(header, SerializeType.THRIFT);
        type = CommandFlagUtils.getSerializeType(header);
        Assert.assertEquals(SerializeType.THRIFT,type);

        CommandFlagUtils.setSerializeType(header, SerializeType.PROTOCOL_BUFFER);
        type = CommandFlagUtils.getSerializeType(header);
        Assert.assertEquals(SerializeType.PROTOCOL_BUFFER,type);

        CommandFlagUtils.setSerializeType(header, SerializeType.HESSIAN);
        type = CommandFlagUtils.getSerializeType(header);
        Assert.assertEquals(SerializeType.HESSIAN,type);
    }

    @Test
    public void testCompressFlag(){
        int flag = header.getFlag();
        Assert.assertEquals(0, flag);
        CommandFlagUtils.markCompressFlag(header);
        flag = header.getFlag();
        Assert.assertEquals(8, flag);

        boolean isCompressed = CommandFlagUtils.isCompressed(header);
        Assert.assertTrue(isCompressed);
    }

    @Test
    public void testSetCompressType(){
        int flag = header.getFlag();
        Assert.assertEquals(0,flag);

        CommandFlagUtils.setCompressType(header,CompressType.GZIP);
        flag = header.getFlag();
        Assert.assertEquals(0,flag);

        CommandFlagUtils.setCompressType(header,CompressType.QUICK_LZ);
        flag = header.getFlag();
        Assert.assertEquals(1,flag);

        CommandFlagUtils.setCompressType(header,CompressType.ZLIB);
        flag = header.getFlag();
        Assert.assertEquals(2,flag);
    }

    @Test
    public void testGetCompressType() {
        CompressType type;
        CommandFlagUtils.setCompressType(header, CompressType.GZIP);
        type = CommandFlagUtils.getCompressType(header);
        Assert.assertEquals(CompressType.GZIP,type);

        CommandFlagUtils.setCompressType(header, CompressType.QUICK_LZ);
        type = CommandFlagUtils.getCompressType(header);
        Assert.assertEquals(CompressType.QUICK_LZ,type);

        CommandFlagUtils.setCompressType(header, CompressType.ZLIB);
        type = CommandFlagUtils.getCompressType(header);
        Assert.assertEquals(CompressType.ZLIB,type);
    }

    @Test
    public void testCompositeCase(){
        CommandFlagUtils.setCompressType(header,CompressType.ZLIB);
        CommandFlagUtils.setSerializeType(header,SerializeType.HESSIAN);
        int flag = header.getFlag();
        Assert.assertEquals(34,flag);

        CommandFlagUtils.markCompressFlag(header);
        flag = header.getFlag();
        Assert.assertEquals(42,flag);
    }

    private RemotingHeader createRemotingHeader() {
        RemotingHeader header = new RemotingHeader();
        header.setVersion(Version.V1.getVersion());
        header.setLanguageCode(LanguageCode.JAVA.getCode());
        return header;
    }

    @After
    public void tearDown(){
        header =null;
    }
}
