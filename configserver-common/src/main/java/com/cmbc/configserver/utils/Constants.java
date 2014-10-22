package com.cmbc.configserver.utils;

/**
 * the constants of the configuration server
 */
public class Constants {
    /**
     * the magic code of the remoting header
     */
    public static final int MAGIC_CODE=0xCFEE;
    /**
     * the max length of the packet
     */
    public static final int MAX_PACKET_LENGTH=1*1024*1024;
    /**
     * the max length of the packet's header
     */
    public static final int MAX_PACKET_HEADER_LENGTH=60;
    /**
     * the total count byte that the field of header length occupy.
     */
    public static final int HEADER_LENGTH_BYTE_COUNT=2;
    /**
     * the min length of the packet
     */
    public static final int MIN_PACKET_LENGTH = 4/*byte count of length*/ + 2/*byte count of header length*/ + 1/*byte count of version*/ + 1/*byte count of language code*/ + 4/*byte count of request id*/ + 1/*byte count of remote command type*/ + 1/*byte count of flag*/;
}