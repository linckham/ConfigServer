package com.cmbc.configserver.remoting;

import com.cmbc.configserver.remoting.common.ResponseFuture;

/**
 * the callback of async invoking
 * 
 * @author tongchuan.lin<linckham@gmail.com>
 * @since 2014/10/17 3:01:22PM
 */
public interface InvokeCallback {
	public void operationComplete(final ResponseFuture responseFuture);
}