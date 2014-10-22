package com.cmbc.configserver.remoting.protocol;

import java.io.Serializable;

/**
 * the remoting header of communication between server and client
 * Created by tongchuan.lin<linckham@gmail.com> on 2014/10/20.
 */
public class RemotingHeader implements Serializable {
    /**
     * the header length
     */
    private int headerLength;
    /**
     * the identifier of the request.
     * When the server response this request,the server should use the requestId to fill up this field.
     */
    private long requestId;

    private int version;
    /**
     * the flag = serializable type(4bit)+compress flag(1bit)+compress type(3bit).
     */
    private int flag;

    private int languageCode;
    /**
     * the remote communication type.It is a request or a response.
     */
    private int remotingType;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getRemotingType() {
        return remotingType;
    }

    public void setRemotingType(int remotingType) {
        this.remotingType = remotingType;
    }

    /**
     * the request/response code.
     */
    private int code;

    //TODO:the extend attribute

    public int getHeaderLength() {
        return headerLength;
    }

    public void setHeaderLength(int headerLength) {
        this.headerLength = headerLength;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(int languageCode) {
        this.languageCode = languageCode;
    }

}