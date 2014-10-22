package com.cmbc.configserver.core.server;

import com.cmbc.configserver.remoting.netty.NettyServerConfig;
import com.cmbc.configserver.core.service.ConfigServerService;
import com.cmbc.configserver.core.service.impl.ConfigServerServiceImpl;

import java.util.concurrent.atomic.AtomicInteger;

public class ConfigServerStartup {
    public static void main(String[] args) {
        try {
            // TODO: use spring to manage the property and injection of Java Object
            final NettyServerConfig nettyServerConfig = new NettyServerConfig();
            nettyServerConfig.setListenPort(19999);

            final ConfigServerService configServerService = new ConfigServerServiceImpl();
            final ConfigServerController controller = new ConfigServerController(nettyServerConfig,configServerService);
            boolean initialized = controller.intialize();
            if (!initialized) {
                controller.shutdown();
                System.exit(-3);
            }
            //add the shutdown hook for ConfigServer
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                        private volatile boolean hasShutdown = false;
                        private AtomicInteger shutdownTimes = new AtomicInteger(0);

                        @Override
                        public void run() {
                            synchronized (this) {
                                if (!hasShutdown) {
                                    this.hasShutdown = true;
                                    long beginTime = System.currentTimeMillis();
                                    controller.shutdown();
                                    long cosumingTime = System.currentTimeMillis() - beginTime;

                                }
                            }
                        }
                    }, "ShutdownHook")
            );
            //start the config server controller
            controller.start();
        } catch (Exception e) {
            //log the exception
            e.printStackTrace();
        }
    }
}