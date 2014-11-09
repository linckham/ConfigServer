package com.cmbc.configserver.remoting.protocol;

import com.cmbc.configserver.common.Version;
import com.cmbc.configserver.common.compress.Compress;
import com.cmbc.configserver.common.compress.CompressFactory;
import com.cmbc.configserver.common.compress.CompressType;
import com.cmbc.configserver.common.serialize.SerializeType;
import com.cmbc.configserver.utils.Constants;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * the base communicating unit between client and server
 *
 * @author tongchuan.lin<linckham@gmail.com>
 * @since 2014/10/17 3:01:22PM
 */
public class RemotingCommand {
    private static AtomicInteger requestId = new AtomicInteger(0);
    private RemotingHeader header;
    private int packetLength;
    private int headerLength;
    /**
     * the body of the remote command
     */
    private byte[] body;

    public RemotingCommand() {
    }
    
    public static RemotingCommand createRequestCommand(int code){
        RemotingHeader hdr = RemotingCommand.createRemotingHeader(code, requestId.incrementAndGet(), RemotingCommandType.REQUEST_COMMAND.getType());
        RemotingCommand cmd = new RemotingCommand();
        cmd.header = hdr;
        cmd.setCode(code);
        return cmd;
    }
    
    private byte[] buildHeader() {
        //doesn't use the FAST JSON to serialize the header. FAST JSON cost too many bytes
        //the detail of header structure,please see protocol.txt
        ByteBuffer headerBuffer = ByteBuffer.allocate(Constants.HEADER_FIX_DATA_LENGTH);
        headerBuffer.put((byte)header.getVersion());//version
        headerBuffer.put((byte)header.getLanguageCode());//language code
        headerBuffer.put((byte)header.getRemotingType());// type request/response
        headerBuffer.putShort((short)header.getCode());//request/response code
        headerBuffer.putInt(header.getRequestId());//request id
        headerBuffer.put((byte)header.getFlag());//flag
        headerBuffer.flip();
        return headerBuffer.array();
    }

    private static RemotingHeader decodeBuilder(byte[] header) throws Exception{
        if(null != header && header.length == Constants.HEADER_FIX_DATA_LENGTH){
            RemotingHeader remoteHeader = new RemotingHeader();
            ByteBuffer buffer = ByteBuffer.wrap(header);
            remoteHeader.setVersion(buffer.get());
            remoteHeader.setLanguageCode(buffer.get());
            remoteHeader.setRemotingType(buffer.get());
            remoteHeader.setCode(buffer.getShort());
            remoteHeader.setRequestId(buffer.getInt());
            remoteHeader.setFlag(buffer.get());
            return  remoteHeader;
        }
        else{
            throw new Exception("invalid header format");
        }
    }

    public ByteBuffer encodeHeader() throws Exception {
        return encodeHeader(this.body != null ? this.body.length : 0);
    }

    /**
     * only encode the header,the body transport independence
     */
    public ByteBuffer encodeHeader(final int bodyLength) throws Exception {
    	
    	int magicCodeSize = 2;
        int totalLengthSize = 4;
        int headerLengthSize = Constants.HEADER_LENGTH_BYTE_COUNT;
        
        byte[] headerData = this.buildHeader();
        int headerLength = 0;
        if(null != headerData){
            headerLength = headerData.length;
        }
        //valid the header's length
        if(headerLength < Constants.HEADER_FIX_DATA_LENGTH || headerLength > Constants.MAX_PACKET_HEADER_LENGTH ){
            throw new Exception(String.format("request/response packet's header length is invalid. %s is not in [%s,%s]",headerLength,Constants.HEADER_FIX_DATA_LENGTH,Constants.MAX_PACKET_HEADER_LENGTH));
        }

        ByteBuffer result = ByteBuffer.allocate(magicCodeSize + totalLengthSize + headerLengthSize + headerData.length);

        //put magic code
        result.putShort(Constants.MAGIC_CODE);
        // put total length
        int totalLength = headerData.length + bodyLength;
        this.setHeaderLength(headerData.length);
        this.setPacketLength(totalLength);
        //valid the body's length
        if (bodyLength > Constants.MAX_PACKET_LENGTH) {
            throw new Exception(String.format("request/response packet's body length is invalid.%s is not in[0,%s]", bodyLength, Constants.MAX_PACKET_LENGTH));
        }
        result.putInt(totalLength);

        // put header length
        result.putShort((short)headerData.length);

        // put header data
        result.put(headerData);

        result.flip();

        return result;
    }

    public static RemotingCommand decode(final ByteBuffer byteBuffer) throws Exception{
        short magicCode = byteBuffer.getShort();//magic code
        int packetTotalLength= byteBuffer.getInt();// the total length of the packet
        int headerLength = byteBuffer.getShort();//the header length

        //valid the magic code
        if (magicCode != Constants.MAGIC_CODE) {
            throw new Exception(String.format("request/response command magic code is invalid. %s is not match the correct code %s", magicCode, Constants.MAGIC_CODE));
        }

        byte[] headerData = new byte[headerLength];
        byteBuffer.get(headerData);

        int bodyLength = packetTotalLength - headerLength;
        byte[] bodyData = null;
        if (bodyLength > 0) {
            bodyData = new byte[bodyLength];
            byteBuffer.get(bodyData);
        }

        RemotingCommand cmd = new RemotingCommand();
        cmd.header=decodeBuilder(headerData);
        cmd.setHeaderLength(headerLength);
        cmd.setPacketLength(packetTotalLength);

        boolean isCompressed = CommandFlagUtils.isCompressed(cmd.header);
        if(isCompressed){
            CompressType type = CommandFlagUtils.getCompressType(cmd.header);
            Compress compress = CompressFactory.createCompress(type);
            if( null != compress){
                cmd.body = compress.decompress(bodyData);
            }
        }
        else{
            cmd.body = bodyData;
        }
        return cmd;
    }

    public void markResponseType() {
        this.header.setRemotingType(RemotingCommandType.RESPONSE_COMMAND.getType());
    }

    public boolean isResponseType() {
        return this.header.getRemotingType() == RemotingCommandType.RESPONSE_COMMAND.getType();
    }

    public void markOnewayRPC() {
    }

    public boolean isOnewayRPC() {
        return false;
    }

    public int getCode() {
        return this.header.getCode();
    }

    public void setCode(int code){
        this.header.setCode(code);
    }

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
        if(null != body && body.length > Constants.DEFAULT_COMPRESS_LENGTH){
            CommandFlagUtils.markCompressFlag(this.header);
            Compress compress = CompressFactory.createCompress(CommandFlagUtils.getCompressType(this.header));
            if(null != compress){
                this.body = compress.compress(body);
            }
        }
        else
        {
            this.body = body;
        }
    }

    public static RemotingCommand createResponseCommand(int code,int requestId){
        RemotingHeader hdr = RemotingCommand.createRemotingHeader(code, requestId, RemotingCommandType.RESPONSE_COMMAND.getType());
        RemotingCommand cmd = new RemotingCommand();
        cmd.header = hdr;
        cmd.markResponseType();
        cmd.setCode(code);
        return cmd;
    }

    public int getRequestId(){
        return this.header == null?0:this.header.getRequestId();
    }

    public void setRequestId(int requestId){
        if(null !=  this.header){
            this.header.setRequestId(requestId);
        }
    }

    /**
     * create the remote header
     */
    private static RemotingHeader createRemotingHeader(int code, int requestId, int commandType) {
        RemotingHeader hdr = new RemotingHeader();
        hdr.setCode(code);
        hdr.setRemotingType(commandType);
        hdr.setRequestId(requestId);
        hdr.setLanguageCode(LanguageCode.JAVA.getCode());
        hdr.setVersion(Version.V1.getVersion());
        CommandFlagUtils.setCompressType(hdr, CompressType.QUICK_LZ);
        CommandFlagUtils.setSerializeType(hdr, SerializeType.FAST_JSON);
        return hdr;
    }

    /**
     * create the response command
     * @param code the response code
     * @param requestId the request id of this response
     * @return the response command
     */
    public static RemotingCommand createResponseCommand(int code,byte[] body,int requestId){
        RemotingCommand command = RemotingCommand.createResponseCommand(code,requestId);
        command.body = body;
        return command;
    }

    public int getPacketLength() {
        return packetLength;
    }

    public void setPacketLength(int packetLength) {
        this.packetLength = packetLength;
    }

    public int getHeaderLength() {
        return headerLength;
    }

    public void setHeaderLength(int headerLength) {
        this.headerLength = headerLength;
    }

    @Override
    public String toString() {
        return "Command{" +
                "requestId='" + this.getRequestId() + "" +
                ",headerLength=" + this.getHeaderLength() + "" +
                ",packetLength=" + this.getPacketLength() + "'" +
                ",type"+this.getType()+"}";
    }
}