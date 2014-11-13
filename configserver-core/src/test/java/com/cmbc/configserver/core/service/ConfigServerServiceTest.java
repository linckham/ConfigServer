package com.cmbc.configserver.core.service;

import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.remoting.common.RemotingHelper;
import io.netty.channel.Channel;
import org.junit.Assert;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/13
 * @Time 14:28
 */
public class ConfigServerServiceTest {
    private ConfigServerService configServerService;
    private Configuration configuration;
    private Channel channel;
    private String clientId;

    @Before
    public void setUp() throws Exception {
        configuration = new Configuration();
        configuration.setCell("test-dubbo");
        configuration.setResource(this.getClass().getName());
        configuration.setType("providers");
        configuration.setContent("dubbo://127.0.0.1:20881?interface=xxxx");

        // mock channel
        channel = EasyMock.createMock(Channel.class);
        EasyMock.expect(channel.localAddress()).andReturn(new InetSocketAddress("127.0.0.1", 19999)).anyTimes();
        EasyMock.expect(channel.remoteAddress()).andReturn(new InetSocketAddress("127.0.0.1", 20034)).anyTimes();
        EasyMock.replay(channel);
        clientId = RemotingHelper.getChannelId(channel);

        //mock config server service
        configServerService = EasyMock.createMock(ConfigServerService.class);
        EasyMock.expect(configServerService.publish(configuration)).andReturn(true).anyTimes();
        EasyMock.expect(configServerService.unPublish(configuration)).andReturn(true).anyTimes();
        EasyMock.expect(configServerService.subscribe(configuration, channel)).andReturn(true).anyTimes();
        EasyMock.expect(configServerService.unSubscribe(configuration, channel)).andReturn(true);
        EasyMock.expect(configServerService.deleteConfigurationByClientId(clientId)).andReturn(true).anyTimes();
        //replay
        EasyMock.replay(configServerService);
    }

    @Test
    public void testPublish() throws Exception {
        boolean bPublish = configServerService.publish(configuration);
        Assert.assertTrue(bPublish);
    }

    @Test
    public void testUnPublish() throws Exception {
        boolean bPublish = configServerService.unPublish(configuration);
        Assert.assertTrue(bPublish);
    }

    @Test
    public void testSubscribe() throws Exception {
        boolean bPublish = configServerService.subscribe(configuration, channel);
        Assert.assertTrue(bPublish);
    }

    @Test
    public void testUnSubscribe() throws Exception {
        boolean bPublish = configServerService.unSubscribe(configuration, channel);
        Assert.assertTrue(bPublish);
    }

    @Test
    public void testDeleteConfigurationByClientId() throws Exception {
        boolean bDeleted = configServerService.deleteConfigurationByClientId(clientId);
        Assert.assertTrue(bDeleted);
    }

}
