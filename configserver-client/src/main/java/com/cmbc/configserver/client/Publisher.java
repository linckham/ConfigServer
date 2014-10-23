package com.cmbc.configserver.client;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.domain.HeartBeatData;
/**
 * the class represents the publisher that will publish/unPublish the specified
 * configuration to the ConfigServer.
 * 
 * @author tongchuan.lin<linckham@gmail.com>
 * @since 2014/10/17 3:01:22PM
 */
public interface Publisher {
	/**
	 * publish the configuration to the remote server
	 * 
	 * @return true if published success,else false
	 */
	public boolean publish(Configuration config);

	/**
	 * unPublish the configuration from the remote server
	 * 
	 * @return true if unPublish success,else false
	 */
	public boolean unPublish(Configuration config);

	public boolean heartBeat(HeartBeatData heartBeat);
}