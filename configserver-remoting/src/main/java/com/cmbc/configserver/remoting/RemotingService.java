package com.cmbc.configserver.remoting;

import com.cmbc.configserver.remoting.RPCHook;

/**
 * the base interface of remote communicating
 * 
 * @author tongchuan.lin<linckham@gmail.com>
 * @since 2014年10月17日 下午3:00:07
 */
public interface RemotingService {
	public void start();

	public void shutdown();

	public void registerRPCHook(RPCHook rpcHook);
}