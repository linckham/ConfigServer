package com.cmbc.configserver.client;

import com.cmbc.configserver.client.impl.ConfigClientImpl;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.remoting.ConnectionStateListener;
import com.cmbc.configserver.remoting.netty.NettyClientConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class ClientPublish {
	public static void main(String[] args) throws IOException, InterruptedException {
		
        List<String> configServerAddress = new ArrayList<String>(1);
        configServerAddress.add("127.0.0.1:19999");
		ConfigClient configClient = new ConfigClientImpl(
				new NettyClientConfig(), configServerAddress,
				new ConnectionStateListener() {
					@Override
					public void reconnected() {
						//do recover
						System.out.println("client reconnected,do recover");
					}

				});
		
        
		
		for(int i=0 ; i<10 ;i++){
			
			Configuration config = new Configuration();
            String content = new StringBuilder(128)
                    .append("{\"ip\":\"127.0.0.1\",\"port\":21881,\"meta\":\"15:53:30.961 [main] DEBUG i.n.util.internal.PlatformDependent - You don't have Javassist in your class path or you don't have enough permission to load dynamically generated classes.  Please check the configuration for better performance just for test.15`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671b6253625`15625362563`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKllllll`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf08304--232038dfjdklfj83rhdhf237r893r3473dfjdkfdjfka8671bjdkjfoepufeoropqeure9r7e9b;cn;aPJH0UADJFjf19uiodf0830`15625362563528gdflhadfjhaljkfhdjkqfhafhoafvafkhdkfyho vnkjkjfl;afjpu-09+=-0fajkfjalfj+JKLJKF:DJkFJKlllllljdkjfoepufeoropqeure\",\"count\":")
                    .append(i+1).append("}").toString();
            config.setContent(content);

			config.setCell("test-cell");
			config.setResource("test-dubbo-rpc");
			config.setType("publisher");
			
			configClient.publish(config);
            Date nowDate = new Date();
			System.out.println(String.format("the result of publish config is  [%s %s]",nowDate,nowDate.getTime()));
			
			
			Thread.sleep(20*1000);
		}
		System.in.read();
		configClient.close();
	}
}
