package com.cmbc.configserver.client;

import java.util.List;

import com.cmbc.configserver.domain.Configuration;

public interface ResourceListener {
	/**
	 * full config of one resource.
	 * 
	 * @param configsss
	 */
	void notify(List<Configuration> configs);

}