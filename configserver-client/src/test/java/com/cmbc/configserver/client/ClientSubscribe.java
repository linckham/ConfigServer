package com.cmbc.configserver.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import com.cmbc.configserver.client.impl.ConfigClientImpl;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.remoting.ConnectionStateListener;
import com.cmbc.configserver.remoting.netty.NettyClientConfig;
import com.cmbc.configserver.utils.PathUtils;

public class ClientSubscribe {
	public static void main(String[] args) throws IOException, InterruptedException {
        List<String> configServerAddress = new ArrayList<String>(1);
        configServerAddress.add("127.0.0.1:19999");
		ConfigClientImpl configClient = new ConfigClientImpl(
				new NettyClientConfig(), configServerAddress,
				new ConnectionStateListener() {
					@Override
					public void reconnected() {
						//do recover
						System.out.println("client reconnected,do recover");
					}
				});
		
		final Configuration subConfig = new Configuration();
        subConfig.setCell("test-cell");
        subConfig.setResource("test-dubbo-rpc");
        subConfig.setType("publisher");
        boolean subscribe = configClient.subscribe(subConfig, new ResourceListener() {
            public void notify(List<Configuration> configs) {
                if (null != configs && !configs.isEmpty()) {
                    System.out.println(String.format("[%s] the subscribe path %s has %s configuration items,",new Date(), PathUtils.getSubscriberPath(subConfig), configs.size()));
                    for (Configuration item : configs){
                        System.out.println(item);
                    }
                }
                else
                {
                    System.out.println(String.format("[%s] the subscribe path %s has no configuration items.",new Date(), PathUtils.getSubscriberPath(subConfig)));
                }
            }
        });
        System.out.println(String.format("[%s] the result of subscriber is %s",new Date(),subscribe));
		System.in.read();
		configClient.close();
	}
}
