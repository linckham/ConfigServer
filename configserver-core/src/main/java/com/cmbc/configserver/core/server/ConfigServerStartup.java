package com.cmbc.configserver.core.server;

import com.cmbc.configserver.core.service.ConfigServerService;
import com.cmbc.configserver.core.service.impl.ConfigServerServiceImpl;
import com.cmbc.configserver.core.storage.ConfigStorage;
import com.cmbc.configserver.core.storage.impl.LocalMemoryConfigStorageImpl;
import com.cmbc.configserver.core.notify.NotifyService;
import com.cmbc.configserver.remoting.netty.NettyServerConfig;
import com.cmbc.configserver.utils.ConfigServerLogger;

public class ConfigServerStartup {
    public static void main(String[] args) {
        try {
            final NettyServerConfig nettyServerConfig = new NettyServerConfig();
            //TODO: get the listen port from the configuration file or the command option
            nettyServerConfig.setListenPort(19999);
            ConfigNettyServer configNettyServer = new ConfigNettyServer(nettyServerConfig);

            final ConfigStorage configStorage = new LocalMemoryConfigStorageImpl();
            final NotifyService notifyService = new NotifyService(configStorage,configNettyServer);
            final ConfigServerService configServerService = new ConfigServerServiceImpl(configStorage,notifyService);
            final ConfigServerController controller = new ConfigServerController(configNettyServer,configServerService);

            boolean initialized = controller.intialize();
            if (!initialized) {
                controller.shutdown();
                System.exit(-3);
            }
            //add the shutdown hook for ConfigServer
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                        private volatile boolean hasShutdown = false;
                        @Override
                        public void run() {
                            synchronized (this) {
                                if (!hasShutdown) {
                                    this.hasShutdown = true;
                                    long beginTime = System.currentTimeMillis();
                                    controller.shutdown();
                                    long consumingTime = System.currentTimeMillis() - beginTime;
                                    String tips = String.format("the shutdown action of ConfigServer cost %s ms",consumingTime);
                                    ConfigServerLogger.info(tips);
                                    System.out.println(tips);
                                }
                            }
                        }
                    }, "ShutdownHook")
            );

            //start the config server controller
            controller.start();

            String successInfo = "The Config Server boot success.";
            ConfigServerLogger.info(successInfo);
            System.out.println(successInfo);
        } catch (Throwable t) {
            t.printStackTrace();
            ConfigServerLogger.error("configServer startup failed.", t);
        }
    }
}