package com.cmbc.configserver.remoting;

import com.cmbc.configserver.remoting.RPCHook;

/**
 * the base interface of remote communicating
 * 
 * @author tongchuan.lin<linckham@gmail.com>
 * @since 2014/10/17 3:01:22PM
 */
public interface RemotingService {
	public void start();

	public void shutdown();

	public void registerRPCHook(RPCHook rpcHook);
}