package com.cmbc.configserver.utils;

import java.io.File;

/**
 * the constants of the configuration server
 */
public class Constants {
    /**
     * the magic code of the remoting header
     */
    public static final short MAGIC_CODE= (short)0xCFEE;
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
     * the fix header data's length
     */
    public static final int HEADER_FIX_DATA_LENGTH=1/*byte count of version*/ + 1/*byte count of language code*/ + 2/* byte count of request code*/+4/*byte count of request id*/ + 1/*byte count of remote command type*/ + 1/*byte count of flag*/;

    /**
     * the min length of the packet
     */
    public static final int MIN_PACKET_LENGTH = 4/*byte count of length*/ + 2/*byte count of header length*/ + HEADER_FIX_DATA_LENGTH;

    /**
     * the path separator
     */
    public static final String PATH_SEPARATOR="/";

    /**
     * the default initial capacity of the collection
     */
    public static final int DEFAULT_INITIAL_CAPACITY = 32;

    /**
     * the default timeout of queue
     */
    public static final int DEFAULT_QUEUE_TIMEOUT = 3*1000;

    /**
     * the default timeout of read-write lock
     */
    public static final int DEFAULT_READ_WRITE_LOCK_TIMEOUT = 3*1000;

    /**
     * the default timeout of socket reading timeout.
     */
    public static final int DEFAULT_SOCKET_READING_TIMEOUT = 5*1000;

    /**
     * the default max queue item
     */
    public static final int DEFAULT_MAX_QUEUE_ITEM = 1024*1024;

    /**
     * default sleep time of Thread
     */
    public static final int DEFAULT_THREAD_SLEEP_TIME = 2*1000;
    /**
     * default compress length
     */
    public static final int DEFAULT_COMPRESS_LENGTH = 2*1024;
    /**
     * default config server address file name
     */
    public static final String DEFAULT_CONFIG_SERVER_ADDRESS_FILE_NAME=System.getProperty("user.home")+ File.separator+"config_server_address.properties";
    public static final String CONFIG_SERVER_ADDRESS_FILE_NAME_KEY="configserver.address.file";
    public static final String CONFIG_SERVER_ADDRESS_KEY="configserver.address";
    public static final String  COMMA_SEPARATOR = ",";
}