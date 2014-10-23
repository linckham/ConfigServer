package com.cmbc.configserver.client;

import com.cmbc.configserver.client.impl.ConfigClientImpl;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.domain.Node;
import com.cmbc.configserver.remoting.netty.NettyClientConfig;

import java.util.List;
import java.util.ArrayList;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @author tongchuan.lin<linckham@gmail.com>.
 *         Date: 2014/10/23
 *         Time: 13:51
 */
public class ConfigClientTest {
    private ConfigClient configClient;
    private NettyClientConfig nettyClientConfig;

    @Before
    public void initialize() {
        nettyClientConfig = new NettyClientConfig();
        List<String> configServerAddress = new ArrayList<String>(1);
        configServerAddress.add("127.0.0.1:19999");
        this.configClient = new ConfigClientImpl(nettyClientConfig, configServerAddress);
        System.out.println("---the config client initialized successfully!---");
    }

    @Test
    public void testPublish() {
        Configuration config = new Configuration();
        Node node = new Node();
        node.setIp("127.0.0.1");
        node.setPort("21881");
        config.setNode(node);

        config.setCell("test-cell");
        config.setResource("test-dubbo-rpc");
        config.setType("publisher");
        long start = System.currentTimeMillis();
        boolean publishResult = this.configClient.publish(config);
        System.out.println(String.format("the consuming time of  publish config is %s ms", System.currentTimeMillis() - start));
       System.out.println(String.format("the result of publish config is %s",publishResult));
        Assert.assertTrue(publishResult);
    }

    @After
    public void destroy() {
        if (null != configClient) {
            System.out.println("---the config client destroy successfully!---");
        }
    }
}
