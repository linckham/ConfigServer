package com.cmbc.configserver.core.server;

import com.cmbc.configserver.common.ThreadFactoryImpl;
import com.cmbc.configserver.remoting.netty.NettyServerConfig;
import com.cmbc.configserver.remoting.netty.NettyRemotingServer;
import com.cmbc.configserver.remoting.RemotingServer;
import com.cmbc.configserver.core.processor.DefaultRequestProcessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * the controller of config server.It is the core class of the configserver-core project
 *
 * @author tongchuan.lin<linckham@gmail.com>
 */
public class ConfigServerController {
    private final NettyServerConfig nettyServerConfig;
    private RemotingServer remotingServer;
    private ExecutorService remotingExecutor;

    public ConfigServerController(NettyServerConfig nettyServerConfig) {
        this.nettyServerConfig = nettyServerConfig;
    }

    /**
     * initialize the ConfigServer Controller
     * @return true if the controller initialize successfully,else false.
     */
    public boolean intialize() {
        this.remotingServer = new NettyRemotingServer(this.nettyServerConfig);
        this.remotingExecutor = Executors.newFixedThreadPool(nettyServerConfig.getServerWorkerThreads(), new ThreadFactoryImpl("ConfigServerExecutorThread_"));
        this.registerProcessor();
        return true;
    }

    private void registerProcessor() {
        this.remotingServer.registerDefaultProcessor(new DefaultRequestProcessor(this), this.remotingExecutor);
    }

    public NettyServerConfig getNettyServerConfig(){
        return this.nettyServerConfig;
    }

    public void start() throws Exception{
        this.remotingServer.start();
    }

    public void shutdown(){
        this.remotingServer.shutdown();
        this.remotingExecutor.shutdown();
    }
}