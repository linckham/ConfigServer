package com.cmbc.configserver.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.cmbc.configserver.client.impl.ConfigClientImpl;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.domain.Node;
import com.cmbc.configserver.remoting.ConnectionStateListener;
import com.cmbc.configserver.remoting.netty.NettyClientConfig;

public class ClientPublish {
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
		
        
		
		for(int i=0 ; i<100 ;i++){
			
			Configuration config = new Configuration();
			Node node = new Node();
			node.setIp("127.0.0.1");
			node.setPort("21881");
			node.setData("p-"+i);
			config.setNode(node);

			config.setCell("test-cell");
			config.setResource("test-dubbo-rpc");
			config.setType("publisher");
			
			boolean publishResult = configClient.publish(config);
			System.out.println(String.format("the result of publish config is %s",publishResult));
			
			
			Thread.sleep(20*1000);
		}
		
			
		
		System.in.read();
		
		
		configClient.close();
	}
}
