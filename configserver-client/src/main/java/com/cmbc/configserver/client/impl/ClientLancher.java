package com.cmbc.configserver.client.impl;

import java.util.ArrayList;
import java.util.List;

import com.cmbc.configserver.client.ConfigClient;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.domain.Node;
import com.cmbc.configserver.remoting.netty.NettyClientConfig;

public class ClientLancher {
	public static void main(String[] args) {
		List<String> serverList = new ArrayList<String>();
		
		ConfigClient configClient = new ConfigClientImpl(new NettyClientConfig(),serverList);
		
		Configuration config = new Configuration();
		config.setCell("idc1");
		config.setResource("com.cmbc.demoService");
		config.setType("publish");
		Node node = new Node();
		//TODO set node prop
		config.setNode(node);
		configClient.publish(config);
		
	}
}
