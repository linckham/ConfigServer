package com.cmbc.configserver.remoting.netty;

import com.cmbc.configserver.remoting.common.RemotingHelper;
import com.cmbc.configserver.remoting.exception.RemotingCommandException;
import com.cmbc.configserver.remoting.protocol.RemotingCommand;
import com.cmbc.configserver.utils.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * the decoder of communication protocol between server and client.<br/>
 *
 * @author tongchuan.lin<linckham@gmail.com>
 * @since 2014-10-22
 */
public class NettyDecoder extends LengthFieldBasedFrameDecoder {
    private static final Logger log = LoggerFactory.getLogger(NettyDecoder.class);

    public NettyDecoder() {
        super(Constants.MAX_PACKET_LENGTH, 2, 4, 2, 0);
    }


    @Override
    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = null;
        try {
            frame = (ByteBuf) super.decode(ctx, in);
            if (null == frame) {
                return null;
            }

            ByteBuffer byteBuffer = frame.nioBuffer();
            return RemotingCommand.decode(byteBuffer);
        } catch (Exception e) {
            log.error("decode exception on channel " + RemotingHelper.parseChannelRemoteAddress(ctx.channel()), e);
            throw new RemotingCommandException("remote command can't decode",e);
        } finally {
            if (null != frame) {
                frame.release();
            }
        }
    }
}
