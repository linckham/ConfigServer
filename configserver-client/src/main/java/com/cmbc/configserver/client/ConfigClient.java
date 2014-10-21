package com.cmbc.configserver.client;

import com.cmbc.configserver.domain.Configuration;

public interface ConfigClient {
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
	public boolean unpublish(Configuration config);

	public boolean subscribe(Configuration config, ResourceListener listener);

	public boolean unsubscribe(Configuration config, ResourceListener listener);
}
