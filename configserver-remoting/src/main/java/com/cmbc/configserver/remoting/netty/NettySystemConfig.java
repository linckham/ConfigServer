package com.cmbc.configserver.remoting.netty;

public class NettySystemConfig {
    public static final String SystemPropertyNettyPooledByteBufAllocatorEnable = "configserver.nettyPooledByteBufAllocatorEnable";
    public static boolean NettyPooledByteBufAllocatorEnable = Boolean.parseBoolean(System.getProperty(SystemPropertyNettyPooledByteBufAllocatorEnable, "false"));
    public static final String SystemPropertySocketSndbufSize = "configserver.socket.sndbuf.size";
    //default buffer size : 128K
    public static int SocketSndbufSize = Integer.parseInt(System.getProperty(SystemPropertySocketSndbufSize, "131072"));
    public static final String SystemPropertySocketRcvbufSize = "configserver.socket.rcvbuf.size";
    //default buffer size : 128K
    public static int SocketRcvbufSize = Integer.parseInt(System.getProperty(SystemPropertySocketRcvbufSize, "131072"));
}
