package com.cmbc.configserver.client;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.domain.HeartBeatData;
/**
 * the class represents the publisher that will publish/unPublish the specified
 * configuration to the ConfigServer.
 * 
 * @author tongchuan.lin<linckham@gmail.com>
 * @since 2014年10月17日 下午4:13:35
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