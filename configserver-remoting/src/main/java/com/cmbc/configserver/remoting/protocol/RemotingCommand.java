package com.cmbc.configserver.remoting.protocol;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.fastjson.annotation.JSONField;
import com.cmbc.configserver.common.RemotingSerializable;
import com.cmbc.configserver.utils.Constants;

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

    public RemotingCommand() {
    }
    
    public static RemotingCommand createRequestCommand(int code){
    	
    	RemotingHeader hdr = new RemotingHeader();
    	hdr.setCode(code);
    	hdr.setRemotingType(RemotingCommandType.REQUEST_COMMAND.getType());
    	hdr.setRequestId(requestId.getAndIncrement());
    	
    	RemotingCommand cmd = new RemotingCommand();
    	cmd.header = hdr;
    	return cmd;
    }
    
    private byte[] buildHeader() {
    	//assert header not null 
        return RemotingSerializable.encode(header);
    }

    public ByteBuffer encodeHeader() {
        return encodeHeader(this.body != null ? this.body.length : 0);
    }

    /**
     * 只打包Header，body部分独立传输
     */
    public ByteBuffer encodeHeader(final int bodyLength) {
    	
    	int magicCodeSize = 2;
        int totalLengthSize = 4;
        int headerLengthSize = Constants.HEADER_LENGTH_BYTE_COUNT;
        
        byte[] headerData = this.buildHeader();
        //TODO validate header length

        ByteBuffer result = ByteBuffer.allocate(magicCodeSize + totalLengthSize + headerLengthSize + headerData.length);

        //put magic code
        result.putShort(Constants.MAGIC_CODE);
        // put total length
        int totalLength = headerLengthSize + headerData.length + bodyLength;
        //TODO validate total length
        result.putInt(totalLength);

        // put header length
        result.putShort((short)headerData.length);

        // put header data
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
        short magicCode = byteBuffer.getShort();//magic code
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

        int bodyLength = packetTotalLength - Constants.HEADER_LENGTH_BYTE_COUNT - headerLength;
        byte[] bodyData = null;
        if (bodyLength > 0) {
            bodyData = new byte[bodyLength];
            byteBuffer.get(bodyData);
        }

        RemotingCommand cmd = new RemotingCommand();
        cmd.header=RemotingSerializable.decode(headerData,RemotingHeader.class);
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

    public void setCode(int code){
        this.header.setCode(code);
    }

    @JSONField(serialize = false)
    public RemotingCommandType getType() {
        if (this.isResponseType()) {
            return RemotingCommandType.RESPONSE_COMMAND;
        }
        return RemotingCommandType.REQUEST_COMMAND;
    }

    public byte[] getBody(){
        return this.body;
    }

    public void setBody(byte[] body){
        this.body = body;
    }

    public static RemotingCommand createResponseCommand(int code,int requestId){

        RemotingHeader hdr = new RemotingHeader();
        hdr.setCode(code);
        hdr.setRemotingType(RemotingCommandType.RESPONSE_COMMAND.getType());
        hdr.setRequestId(requestId);
        hdr.setLanguageCode(LanguageCode.JAVA.getCode());

        RemotingCommand cmd = new RemotingCommand();
        cmd.header = hdr;
        return cmd;
    }

    public RemotingHeader getHeader(){
        return this.header;
    }

    public void setHeader(RemotingHeader header){
        this.header = header;
    }

    public int getRequestId(){
        return this.header == null?0:this.header.getRequestId();
    }

    public void setRequestId(int requestId){
        if(null !=  this.header){
            this.header.setRequestId(requestId);
        }
    }
}