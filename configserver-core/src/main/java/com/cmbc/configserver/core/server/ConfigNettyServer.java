package com.cmbc.configserver.core.server;

import com.cmbc.configserver.core.heartbeat.ClientConnectionListener;
import com.cmbc.configserver.remoting.RemotingServer;
import com.cmbc.configserver.remoting.netty.NettyRemotingServer;
import com.cmbc.configserver.remoting.netty.NettyServerConfig;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 * *
 * @Date 2014/10/31
 * @Time 11:12
 */
public class ConfigNettyServer {
    private NettyServerConfig nettyServerConfig;
    private RemotingServer remotingServer;
    private ClientConnectionListener clientConnectionListener;

    public void setClientConnectionListener(ClientConnectionListener clientConnectionListener) {
        this.clientConnectionListener = clientConnectionListener;
    }

    public void setNettyServerConfig(NettyServerConfig nettyServerConfig) {
        this.nettyServerConfig = nettyServerConfig;
    }

    public NettyServerConfig getNettyServerConfig(){
        return this.nettyServerConfig;
    }
    public RemotingServer getRemotingServer() {
        return this.remotingServer;
    }

    public boolean initialize(ConfigServerController configServerController) {
        this.remotingServer = new NettyRemotingServer(this.nettyServerConfig,this.clientConnectionListener);
        return true;
    }

    public void start() throws Exception{
        this.remotingServer.start();
    }
}
