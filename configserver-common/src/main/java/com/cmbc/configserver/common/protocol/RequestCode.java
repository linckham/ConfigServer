package com.cmbc.configserver.common.protocol;

/**
 * the request code
 */
public class RequestCode {
    public static final int PUBLISH_CONFIG = 1;
    public static final int UNPUBLISH_CONFIG = 2;
    public static final int SUBSCRIBE_CONFIG = 3;
    public static final int UNSUBSCRIBE_CONFIG=4;
    public static final int NOTIFY_CONFIG = 5;
    public static final int HEARTBEAT = 6;
}
