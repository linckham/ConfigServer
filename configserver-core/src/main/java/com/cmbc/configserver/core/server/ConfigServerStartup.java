package com.cmbc.configserver.core.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cmbc.configserver.core.storage.ConfigStorage;
import com.cmbc.configserver.core.storage.impl.LocalMemoryConfigStorageImpl;
import com.cmbc.configserver.remoting.netty.NettyServerConfig;
import com.cmbc.configserver.core.service.ConfigServerService;
import com.cmbc.configserver.core.service.impl.ConfigServerServiceImpl;

import java.util.concurrent.atomic.AtomicInteger;

public class ConfigServerStartup {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigServerStartup.class);
    public static void main(String[] args) {
        try {
            final NettyServerConfig nettyServerConfig = new NettyServerConfig();
            nettyServerConfig.setListenPort(19999);
            final ConfigStorage configStorage = new LocalMemoryConfigStorageImpl();
            final ConfigServerService configServerService = new ConfigServerServiceImpl(configStorage);
            final ConfigServerController controller = new ConfigServerController(nettyServerConfig,configServerService);
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
                                    LOGGER.info(tips);
                                    System.out.println(tips);
                                }
                            }
                        }
                    }, "ShutdownHook")
            );
            //start the config server controller
            controller.start();

            String successInfo = "The Config Server boot success.";
            LOGGER.info(successInfo);
            System.out.println(successInfo);
        } catch (Exception e) {
            LOGGER.error("ConfigServer startup failed",e);
            e.printStackTrace();
        }
    }
}