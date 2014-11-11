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
        configClient.subscribe(subConfig, new ResourceListener() {
            public void notify(List<Configuration> configs) {
                Date nowDate = new Date();
                if (null != configs && !configs.isEmpty()) {
                    System.out.println(String.format(" the subscribe path %s has %s configuration items [%s %s]", PathUtils.getSubscriberPath(subConfig), configs.size(),nowDate,nowDate.getTime()));
                    for (Configuration item : configs){
                        System.out.println(item);
                    }
                }
                else
                {
                    System.out.println(String.format(" the subscribe path %s has no configuration items [%s %s]", PathUtils.getSubscriberPath(subConfig),nowDate,nowDate.getTime()));
                }
            }
        });
		System.in.read();
		configClient.close();
	}
}
