package com.cmbc.configserver.remoting.common;

import io.netty.channel.ChannelHandlerContext;

import com.cmbc.configserver.remoting.protocol.RemotingCommand;

public interface RequestProcessor {
	public RemotingCommand processRequest(ChannelHandlerContext ctx,
			RemotingCommand request) throws Exception;
}