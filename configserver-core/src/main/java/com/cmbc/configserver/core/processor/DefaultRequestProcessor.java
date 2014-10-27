package com.cmbc.configserver.core.processor;

import com.cmbc.configserver.common.protocol.RequestCode;
import com.cmbc.configserver.common.protocol.ResponseCode;
import com.cmbc.configserver.core.server.ConfigServerController;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.remoting.common.RequestProcessor;
import com.cmbc.configserver.remoting.protocol.RemotingCommand;
import com.cmbc.configserver.remoting.protocol.RemotingSysResponseCode;
import io.netty.channel.ChannelHandlerContext;

/**
 * the default request processor of ConfigServer
 */
public class DefaultRequestProcessor implements RequestProcessor {
    private final ConfigServerController configServerController;

    public DefaultRequestProcessor(ConfigServerController controller) {
        this.configServerController = controller;
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) {
        switch (request.getCode()) {
            case RequestCode.PUBLISH_CONFIG:
                return this.publishConfig(ctx, request);
            case RequestCode.UNPUBLISH_CONFIG:
                return this.unPublishConfig(ctx, request);
            case RequestCode.SUBSCRIBE_CONFIG:
                return this.subscribeConfig(ctx, request);
            case RequestCode.UNSUBSCRIBE_CONFIG:
                return this.unSubscribeConfig(ctx, request);
            case RequestCode.HEARTBEAT:
                return this.heartBeat(ctx, request);
            default:
                break;
        }
        return null;
    }

    private RemotingCommand publishConfig(ChannelHandlerContext ctx, RemotingCommand request) {
        String responseBody = "OK";
        int code = RemotingSysResponseCode.SYSTEM_ERROR;
        try {
            Configuration config = null;
            if (null != request.getBody()) {
                config = Configuration.decode(request.getBody(), Configuration.class);
            }
            this.configServerController.getConfigServerService().publish(config);
            code = ResponseCode.PUBLISH_CONFIG_OK;
        } catch (Exception e) {
            code = ResponseCode.PUBLISH_CONFIG_FAILED;
            responseBody = e.getMessage();
        }
        return RemotingCommand.createResponseCommand(code, responseBody.getBytes(), request.getRequestId());
    }

    private RemotingCommand unPublishConfig(ChannelHandlerContext ctx, RemotingCommand request) {
        String responseBody = "OK";
        int code = RemotingSysResponseCode.SYSTEM_ERROR;
        try {
            Configuration config = null;
            if (null != request.getBody()) {
                config = Configuration.decode(request.getBody(), Configuration.class);
            }
            this.configServerController.getConfigServerService().unPublish(config);
            code = ResponseCode.UNPUBLISH_CONFIG_OK;
        } catch (Exception e) {
            code = ResponseCode.UNPUBLISH_CONFIG_FAILED;
            responseBody = e.getMessage();
        }
        return RemotingCommand.createResponseCommand(code, responseBody.getBytes(), request.getRequestId());
    }

    private RemotingCommand subscribeConfig(ChannelHandlerContext ctx, RemotingCommand request) {
        String responseBody = "OK";
        int code = RemotingSysResponseCode.SYSTEM_ERROR;
        try {
            Configuration config = null;
            if (null != request.getBody()) {
                config = Configuration.decode(request.getBody(), Configuration.class);
            }
            this.configServerController.getConfigServerService().subscribe(config,ctx.channel());
            code = ResponseCode.SUBSCRIBE_CONFIG_OK;
        } catch (Exception e) {
            code = ResponseCode.SUBSCRIBE_CONFIG_FAILED;
            responseBody = e.getMessage();
        }
        return RemotingCommand.createResponseCommand(code, responseBody.getBytes(), request.getRequestId());
    }

    private RemotingCommand heartBeat(ChannelHandlerContext ctx, RemotingCommand request) {
        return null;
    }

    private RemotingCommand unSubscribeConfig(ChannelHandlerContext ctx, RemotingCommand request) {
        String responseBody = "OK";
        int code = RemotingSysResponseCode.SYSTEM_ERROR;
        try {
            Configuration config = null;
            if (null != request.getBody()) {
                config = Configuration.decode(request.getBody(), Configuration.class);
            }
            this.configServerController.getConfigServerService().unSubscribe(config,ctx.channel());
            code = ResponseCode.UNSUBSCRIBE_CONFIG_OK;
        } catch (Exception e) {
            code = ResponseCode.UNSUBSCRIBE_CONFIG_FAILED;
            responseBody = e.getMessage();
        }
        return RemotingCommand.createResponseCommand(code, responseBody.getBytes(), request.getRequestId());
    }
}
