package com.cmbc.configserver.core.processor;

import com.cmbc.configserver.common.RemotingSerializable;
import com.cmbc.configserver.common.protocol.RequestCode;
import com.cmbc.configserver.common.protocol.ResponseCode;
import com.cmbc.configserver.core.heartbeat.HeartbeatService;
import com.cmbc.configserver.core.service.ConfigServerService;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.domain.Notify;
import com.cmbc.configserver.remoting.common.RemotingHelper;
import com.cmbc.configserver.remoting.common.RequestProcessor;
import com.cmbc.configserver.remoting.protocol.RemotingCommand;
import com.cmbc.configserver.remoting.protocol.RemotingSysResponseCode;
import com.cmbc.configserver.utils.ConfigServerLogger;
import com.cmbc.configserver.utils.PathUtils;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * the default request processor of ConfigServer
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/31
 * @Time 11:12
 */
@Service("defaultRequestProcessor")
public class DefaultRequestProcessor implements RequestProcessor {
    @Autowired
    private ConfigServerService configServerService;
    @Autowired
    private HeartbeatService heartbeatService;

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) {
    	//update channel info
    	heartbeatService.updateHeartbeat(ctx.channel());
    	
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
        int code;
        try {
            Configuration config = null;
            if (null != request.getBody()) {
                config = Configuration.decode(request.getBody(), Configuration.class);
            }
            if(null != config) {
                //valid the client id
                if (null == config.getClientId() || config.getClientId().isEmpty()) {
                    config.setClientId(RemotingHelper.getChannelId(ctx.channel()));
                }

                //validate channel
                if (ctx.channel() != null && ctx.channel().isActive()) {
                    this.configServerService.publish(config);
                    code = ResponseCode.PUBLISH_CONFIG_OK;
                } else {
                    code = ResponseCode.PUBLISH_CONFIG_FAILED;
                }
            }
            else{
                code = ResponseCode.PUBLISH_CONFIG_FAILED;
                responseBody = "configuration is null or empty!";
            }
        } catch (Exception e) {
            code = ResponseCode.PUBLISH_CONFIG_FAILED;
            responseBody = e.getMessage();
        }
        return RemotingCommand.createResponseCommand(code, responseBody.getBytes(), request.getRequestId());
    }

    private RemotingCommand unPublishConfig(ChannelHandlerContext ctx, RemotingCommand request) {
        String responseBody = "OK";
        int code;
        try {
            Configuration config = null;
            if (null != request.getBody()) {
                config = Configuration.decode(request.getBody(), Configuration.class);
            }
            if (null != config) {
                //valid the client id
                if (null == config.getClientId() || config.getClientId().isEmpty()) {
                    config.setClientId(RemotingHelper.getChannelId(ctx.channel()));
                }

                if (null != ctx.channel() && ctx.channel().isActive()) {
                    this.configServerService.unPublish(config);
                    code = ResponseCode.UNPUBLISH_CONFIG_OK;
                } else {
                    code = ResponseCode.UNPUBLISH_CONFIG_FAILED;
                    responseBody = "channel is not active,so ignore this un publish request!";
                }
            } else {
                code = ResponseCode.UNPUBLISH_CONFIG_FAILED;
                responseBody = "configuration is null or empty!";
            }
        } catch (Exception e) {
            code = ResponseCode.UNPUBLISH_CONFIG_FAILED;
            responseBody = e.getMessage();
        }
        return RemotingCommand.createResponseCommand(code, responseBody.getBytes(), request.getRequestId());
    }

    private RemotingCommand subscribeConfig(ChannelHandlerContext ctx, RemotingCommand request) {
        int code = RemotingSysResponseCode.SYSTEM_ERROR;
        byte[]  body = "OK".getBytes();
        try {
            Configuration config = null;
            if (null != request.getBody()) {
                config = Configuration.decode(request.getBody(), Configuration.class);
            }

            if (null != ctx.channel() && ctx.channel().isActive()) {
                boolean bSubscribe = this.configServerService.subscribe(config, ctx.channel());
                if (bSubscribe) {
                    code = ResponseCode.SUBSCRIBE_CONFIG_OK;
                    List<Configuration> configs = this.configServerService.getConfigurationList(config);
                    Notify notify = new Notify();
                    notify.setPath(PathUtils.getSubscriberPath(config));
                    notify.setConfigLists(configs);
                    body = RemotingSerializable.encode(notify);
                }
            } else {
                code = ResponseCode.SUBSCRIBE_CONFIG_FAILED;
                body = "channel is not active,so ignore the subscribe request.".getBytes();
            }
        } catch (Exception e) {
            code = ResponseCode.SUBSCRIBE_CONFIG_FAILED;
            body = e.getMessage().getBytes();
        }
        return RemotingCommand.createResponseCommand(code, body, request.getRequestId());
    }

    private RemotingCommand heartBeat(ChannelHandlerContext ctx, RemotingCommand request) {
        if (null != ctx.channel() && ctx.channel().isActive()) {
            return RemotingCommand.createResponseCommand(ResponseCode.HEARTBEAT_OK, null, request.getRequestId());
        } else {
            ConfigServerLogger.warn(String.format("channel %s is not active,so ignore this heart beat request.", RemotingHelper.getChannelId(ctx.channel())));
            return null;
        }
    }

    private RemotingCommand unSubscribeConfig(ChannelHandlerContext ctx, RemotingCommand request) {
        String responseBody = "OK";
        int code;
        try {
            Configuration config = null;
            if (null != request.getBody()) {
                config = Configuration.decode(request.getBody(), Configuration.class);
            }
            if (null != ctx.channel() && ctx.channel().isActive()) {
                this.configServerService.unSubscribe(config, ctx.channel());
                code = ResponseCode.UNSUBSCRIBE_CONFIG_OK;
            }
            else
            {
                code = ResponseCode.UNSUBSCRIBE_CONFIG_FAILED;
                responseBody = "channel is not active,so ignore this un subscribe request!";
            }
        } catch (Exception e) {
            code = ResponseCode.UNSUBSCRIBE_CONFIG_FAILED;
            responseBody = e.getMessage();
        }
        return RemotingCommand.createResponseCommand(code, responseBody.getBytes(), request.getRequestId());
    }
}
