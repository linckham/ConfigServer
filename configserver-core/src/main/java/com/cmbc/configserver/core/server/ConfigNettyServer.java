package com.cmbc.configserver.core.server;

import com.cmbc.configserver.remoting.RemotingServer;
import com.cmbc.configserver.remoting.netty.NettyRemotingServer;
import com.cmbc.configserver.remoting.netty.NettyServerConfig;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/31
 * @Time 11:12
 */
public class ConfigNettyServer {
    private final NettyServerConfig nettyServerConfig;
    private RemotingServer remotingServer;

    public ConfigNettyServer(NettyServerConfig nettyServerConfig){
        this.nettyServerConfig = nettyServerConfig;
    }

    public RemotingServer getRemotingServer() {
        return this.remotingServer;
    }

    public NettyServerConfig getNettyServerConfig(){
        return this.nettyServerConfig;
    }

    public boolean initialize() {
        this.remotingServer = new NettyRemotingServer(this.nettyServerConfig);
        return true;
    }

    public void start() throws Exception{
        this.remotingServer.start();
    }
}
