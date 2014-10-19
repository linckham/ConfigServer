package com.cmbc.configserver.client;
import com.cmbc.configserver.domain.HeartBeatData;
/**
 * the class represents the subscriber that will subscribe the specified
 * configuration from the ConfigServer.
 * 
 * @author tongchuan.lin<linckham@gmail.com>
 * @since 2014年10月17日 下午4:14:57
 */
public interface Subscriber {
	public boolean subscribe(String group);
	public boolean unSubscribe(String group);
	public boolean heartBeat(HeartBeatData heartBeat);
}