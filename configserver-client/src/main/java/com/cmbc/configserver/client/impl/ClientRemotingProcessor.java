/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cmbc.configserver.client.impl;

import io.netty.channel.ChannelHandlerContext;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cmbc.configserver.client.ResourceListener;
import com.cmbc.configserver.common.RemotingSerializable;
import com.cmbc.configserver.common.protocol.RequestCode;
import com.cmbc.configserver.common.protocol.ResponseCode;
import com.cmbc.configserver.domain.Notify;
import com.cmbc.configserver.remoting.common.RequestProcessor;
import com.cmbc.configserver.remoting.protocol.RemotingCommand;
import com.cmbc.configserver.remoting.protocol.RemotingSysResponseCode;

public class ClientRemotingProcessor implements RequestProcessor {

	private static final Logger logger = LoggerFactory.getLogger(ClientRemotingProcessor.class);

	private ConfigClientImpl configClientImpl;

	public ClientRemotingProcessor(ConfigClientImpl configClientImpl) {
		this.configClientImpl = configClientImpl;
	}

	@Override
	public RemotingCommand processRequest(ChannelHandlerContext ctx,
			RemotingCommand request) {
		switch (request.getCode()) {
			case RequestCode.NOTIFY_CONFIG:
				return resolvePushConfig(ctx, request);
			default:
				break;
		}

		return null;
	}

	private RemotingCommand resolvePushConfig(ChannelHandlerContext ctx,
			RemotingCommand request) {
		int code = RemotingSysResponseCode.SYSTEM_ERROR;
		try {
			Notify notifyConfig = null;
			if (request.getBody() != null) {
				notifyConfig = RemotingSerializable.decode(request.getBody(),Notify.class);
				
				configClientImpl.getNotifyCache().put(notifyConfig.getPath(),notifyConfig);
				//TODO use thread pool to notify listeners? if notify listener's time too long,response may timeout
				Set<ResourceListener> listeners = configClientImpl.getSubcribeMap().get(notifyConfig.getPath());
				if(listeners != null){
					for(ResourceListener listener : listeners){
						listener.notify(notifyConfig.getConfigLists());
					}
				}
			}
			
			code = ResponseCode.NOTIFY_CONFIG_OK;
		} catch (Exception e) {
			logger.info(e.toString());
			code = ResponseCode.NOTIFY_CONFIG_FAILED;
		}
		return RemotingCommand.createResponseCommand(code, null,request.getRequestId());
	}
}
