package com.cmbc.configserver.core.server;

import com.cmbc.configserver.common.ThreadFactoryImpl;
import com.cmbc.configserver.remoting.common.RequestProcessor;
import com.cmbc.configserver.utils.Constants;
import com.cmbc.configserver.utils.StatisticsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

/**
 * the controller of config server.It is the core class of the configserver-core project
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/31
 * @Time 11:12
 */
@Service("configServerController")
public class ConfigServerController {
    @Autowired
    private ConfigNettyServer configNettyServer;
    @Autowired
    private RequestProcessor defaultRequestProcessor;
    private ExecutorService remoteExecutor;

    /**
     * initialize the ConfigServer Controller
     * @return true if the controller initialize successfully,else false.
     */
    public boolean initialize() {
        //limit the max queue size of the thread pool
        this.remoteExecutor = new ThreadPoolExecutor(this.configNettyServer.getNettyServerConfig().getServerWorkerThreads(),
                this.configNettyServer.getNettyServerConfig().getServerWorkerThreads(), 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(Constants.DEFAULT_MAX_QUEUE_ITEM), new ThreadFactoryImpl("ConfigServerExecutorThread-"));
        StatisticsLog.registerExecutor("remote-request-pool",(ThreadPoolExecutor)this.remoteExecutor);
        this.configNettyServer.initialize(this);
        this.registerProcessor();
        return true;
    }

    private void registerProcessor() {
        this.configNettyServer.getRemotingServer().registerDefaultProcessor(this.defaultRequestProcessor, this.remoteExecutor);
    }

    public void start() {
        this.configNettyServer.start();
    }

    public void shutdown(){
        this.configNettyServer.getRemotingServer().shutdown();
        this.remoteExecutor.shutdown();
    }
}