package com.cmbc.configserver.client;

import com.cmbc.configserver.domain.Configuration;

import java.util.List;

public interface ConfigClient {
	/**
	 * publish the configuration to the remote server
	 * 
	 * @return true if published success,else false
	 */
	public void publish(Configuration config);

	/**
	 * unPublish the configuration from the remote server
	 * 
	 * @return true if unPublish success,else false
	 */
	public void unpublish(Configuration config);

	public List<Configuration> subscribe(Configuration config, ResourceListener listener);

	public void unsubscribe(Configuration config, ResourceListener listener);
	
	public void close();

    public boolean isAvailable();
}
