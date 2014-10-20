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
}