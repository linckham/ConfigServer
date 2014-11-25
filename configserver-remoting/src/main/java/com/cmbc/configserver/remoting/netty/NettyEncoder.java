package com.cmbc.configserver.remoting.netty;

import com.cmbc.configserver.remoting.common.RemotingHelper;
import com.cmbc.configserver.remoting.exception.RemotingCommandException;
import com.cmbc.configserver.remoting.protocol.RemotingCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class NettyEncoder extends MessageToByteEncoder<RemotingCommand> {
    private static final Logger log = LoggerFactory.getLogger(NettyEncoder.class);


    @Override
    public void encode(ChannelHandlerContext ctx, RemotingCommand remotingCommand, ByteBuf out)
            throws Exception {
        try {
            ByteBuffer header = remotingCommand.encodeHeader();
            out.writeBytes(header);
            byte[] body = remotingCommand.getBody();
            if (body != null) {
                out.writeBytes(body);
            }
        }
        catch (Exception e) {
            log.error(String.format("exception happens when encode remote command %s on channel %s",
                    remotingCommand,RemotingHelper.parseChannelRemoteAddress(ctx.channel())), e);
            throw new RemotingCommandException("remote command can't encode.",e);
        }
    }
}