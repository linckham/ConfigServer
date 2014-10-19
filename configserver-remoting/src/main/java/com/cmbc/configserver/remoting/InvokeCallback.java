package com.cmbc.configserver.remoting;

import com.cmbc.configserver.remoting.common.ResponseFuture;

/**
 * the callback of async invoking
 * 
 * @author tongchuan.lin<linckham@gmail.com>
 * @since 2014年10月17日 下午5:00:40
 */
public interface InvokeCallback {
	public void operationComplete(final ResponseFuture responseFuture);
}