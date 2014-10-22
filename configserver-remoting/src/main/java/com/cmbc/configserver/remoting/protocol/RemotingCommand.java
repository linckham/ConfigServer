package com.cmbc.configserver.remoting.protocol;

import com.alibaba.fastjson.annotation.JSONField;
import com.cmbc.configserver.utils.Constants;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * the base communicating unit between client and server
 *
 * @author tongchuan.lin<linckham@gmail.com>
 * @since 2014年10月17日 下午3:01:22
 */
public class RemotingCommand {
    private static AtomicInteger requestId = new AtomicInteger(0);
    private RemotingHeader header;
    /**
     * the body of the remote command
     */
    private transient byte[] body;

    protected RemotingCommand() {
    }

    private byte[] buildHeader() {
        return null;
    }

    public ByteBuffer encode() {
        // 1> header length size
        int length = 4;

        // 2> header data length
        byte[] headerData = this.buildHeader();
        length += headerData.length;

        // 3> body data length
        if (this.body != null) {
            length += body.length;
        }

        ByteBuffer result = ByteBuffer.allocate(4 + length);

        // length
        result.putInt(length);

        // header length
        result.putInt(headerData.length);

        // header data
        result.put(headerData);

        // body data;
        if (this.body != null) {
            result.put(this.body);
        }

        result.flip();

        return result;
    }

    public ByteBuffer encodeHeader() {
        return encodeHeader(this.body != null ? this.body.length : 0);
    }

    /**
     * 只打包Header，body部分独立传输
     */
    public ByteBuffer encodeHeader(final int bodyLength) {
        // 1> header length size
        int length = 4;

        // 2> header data length
        byte[] headerData = this.buildHeader();
        length += headerData.length;

        // 3> body data length
        length += bodyLength;

        ByteBuffer result = ByteBuffer.allocate(4 + length - bodyLength);

        // length
        result.putInt(length);

        // header length
        result.putInt(headerData.length);

        // header data
        result.put(headerData);

        result.flip();

        return result;
    }

    public static RemotingCommand decode(final byte[] array) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(array);
        return decode(byteBuffer);
    }

    public static RemotingCommand decode(final ByteBuffer byteBuffer) {
        int length = byteBuffer.limit();
        int magicCode = byteBuffer.getShort();//magic code
        int packetTotalLength= byteBuffer.getInt();// the total length of the packet
        int headerLength = byteBuffer.getShort();//the header length

        //valid the magic code
        if(magicCode != Constants.MAGIC_CODE){
            //TODO: throw an exception to the endpoint
        }

        //valid the packet's length
        if(packetTotalLength < Constants.MIN_PACKET_LENGTH || packetTotalLength > Constants.MAX_PACKET_LENGTH){
            //TODO: throw an exception to the endpoint
        }

        //valid the header's length
        if(headerLength <= 0 || headerLength > Constants.MAX_PACKET_HEADER_LENGTH ){
            //TODO:throw an exception to the endpoint
        }

        byte[] headerData = new byte[headerLength];
        byteBuffer.get(headerData);

        int bodyLength = length - Constants.HEADER_LENGTH_BYTE_COUNT - headerLength;
        byte[] bodyData = null;
        if (bodyLength > 0) {
            bodyData = new byte[bodyLength];
            byteBuffer.get(bodyData);
        }

        RemotingCommand cmd = RemotingSerializable.decode(headerData,RemotingCommand.class);
        cmd.body = bodyData;
        return cmd;
    }

    public void markResponseType() {
        this.header.setRemotingType(RemotingCommandType.RESPONSE_COMMAND.getType());
    }

    /*@JSONField(serialize = false)*/
    public boolean isResponseType() {
        return this.header.getRemotingType() == RemotingCommandType.RESPONSE_COMMAND.getType();
    }

    public void markOnewayRPC() {
        /*int bits = 1 << RPC_ONEWAY;
        this.flag |= bits;*/
    }

    /*@JSONField(serialize = false)*/
    public boolean isOnewayRPC() {
        /*int bits = 1 << RPC_ONEWAY;
        return (this.flag & bits) == bits;*/
        return false;
    }

    public int getCode() {
        return this.header.getCode();
    }

    @JSONField(serialize = false)
    public RemotingCommandType getType() {
        if (this.isResponseType()) {
            return RemotingCommandType.RESPONSE_COMMAND;
        }
        return RemotingCommandType.REQUEST_COMMAND;
    }
}