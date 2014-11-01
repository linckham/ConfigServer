package com.cmbc.configserver.core.server;

import com.cmbc.configserver.common.ThreadFactoryImpl;
import com.cmbc.configserver.core.processor.DefaultRequestProcessor;
import com.cmbc.configserver.core.service.ConfigServerService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * the controller of config server.It is the core class of the configserver-core project
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/31
 * @Time 11:12
 */
public class ConfigServerController {
    private ConfigNettyServer configNettyServer;
    private ExecutorService remoteExecutor;
    private ConfigServerService configServerService;

    public ConfigServerController(ConfigNettyServer configNettyServer,ConfigServerService configServerService) {
        this.configNettyServer = configNettyServer;
        this.configServerService =configServerService;
    }

    public ConfigServerService getConfigServerService(){
        return this.configServerService;
    }
    /**
     * initialize the ConfigServer Controller
     * @return true if the controller initialize successfully,else false.
     */
    public boolean initialize() {
        this.remoteExecutor = Executors.newFixedThreadPool(this.configNettyServer.getNettyServerConfig().getServerWorkerThreads(), new ThreadFactoryImpl("ConfigServerExecutorThread_"));
        this.configNettyServer.initialize();
        this.registerProcessor();
        return true;
    }

    private void registerProcessor() {
        this.configNettyServer.getRemotingServer().registerDefaultProcessor(new DefaultRequestProcessor(this), this.remoteExecutor);
    }

    public void start() throws Exception{
        this.configNettyServer.start();
        this.configServerService.start();
    }

    public void shutdown(){
        this.configNettyServer.getRemotingServer().shutdown();
        this.remoteExecutor.shutdown();
        this.configServerService.shutdown();
    }
}