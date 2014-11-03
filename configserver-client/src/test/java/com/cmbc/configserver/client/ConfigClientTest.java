package com.cmbc.configserver.client;

import com.cmbc.configserver.client.impl.ConfigClientImpl;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.remoting.netty.NettyClientConfig;
import com.cmbc.configserver.utils.PathUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
    public void initialize() throws InterruptedException {
        nettyClientConfig = new NettyClientConfig();
        List<String> configServerAddress = new ArrayList<String>(1);
        configServerAddress.add("127.0.0.1:19999");
        this.configClient = new ConfigClientImpl(nettyClientConfig, configServerAddress, null);
        System.out.println("---the config client initialized successfully!---");
    }

    @Test
    public void testPublish() {
        Configuration config = new Configuration();
        String content = new StringBuilder(128).append("{\"ip\":\"127.0.0.1\",\"port\":21881,\"meta\":\"just for test\"").toString();
        config.setContent(content);
        config.setCell("test-cell");
        config.setResource("test-dubbo-rpc");
        config.setType("publisher");
        long start = System.currentTimeMillis();
        boolean publishResult = this.configClient.publish(config);
        System.out.println(String.format("the consuming time of  publish config is %s ms", System.currentTimeMillis() - start));
       System.out.println(String.format("the result of publish config is %s",publishResult));
        Assert.assertTrue(publishResult);
    }

    @Test
    public void testPubSub() {
        Configuration config = new Configuration();
        String content = new StringBuilder(128).append("{\"ip\":\"127.0.0.1\",\"port\":21881,\"meta\":\"just for test\"").toString();
        config.setContent(content);

        config.setCell("test-cell");
        config.setResource("test-dubbo-rpc");
        config.setType("publisher");
        long start = System.currentTimeMillis();
        boolean publishResult = this.configClient.publish(config);
        System.out.println(String.format("the consuming time of  publish config is %s ms", System.currentTimeMillis() - start));
        System.out.println(String.format("the result of publish config is %s",publishResult));
        Assert.assertTrue(publishResult);

        final Configuration subConfig = new Configuration();
        subConfig.setCell("test-cell");
        subConfig.setResource("test-dubbo-rpc");
        subConfig.setType("publisher");
        boolean subscribe = this.configClient.subscribe(config, new ResourceListener() {
            public void notify(List<Configuration> configs) {
                if (null != configs && !configs.isEmpty()) {
                    System.out.println(String.format("the subscribe path %s has %s configuration items,", PathUtils.getSubscriberPath(subConfig), configs.size()));
                    for (Configuration item : configs){
                        System.out.println(item);
                    }
                }
                else
                {
                    System.out.println("the configs is empty.");
                }
            }
        });
        System.out.println("the result of subscriber is "+ subscribe);
        Assert.assertTrue(subscribe);
    }

    @After
    public void destroy() {
        if (null != configClient) {
            System.out.println("---the config client destroy successfully!---");
        }
    }
}
