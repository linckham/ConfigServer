package com.cmbc.configserver.core.server;

import com.cmbc.configserver.remoting.netty.NettyServerConfig;

import java.util.concurrent.atomic.AtomicInteger;

public class ConfigServerStartup {
    public static void main(String[] args) {
        //start the config server controller
        try {
            final NettyServerConfig nettyServerConfig = new NettyServerConfig();
            nettyServerConfig.setListenPort(19999);
            final ConfigServerController controller = new ConfigServerController(nettyServerConfig);
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
            controller.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}