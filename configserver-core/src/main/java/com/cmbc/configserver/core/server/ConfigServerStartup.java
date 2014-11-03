package com.cmbc.configserver.core.server;

import com.cmbc.configserver.utils.ConfigServerLogger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * the bootstrap class of the config-server process.<br/>
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/31
 * @Time 11:12
 */
public class ConfigServerStartup {
    public static void main(String[] args) {
        try {
            ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/config-server.xml");
            //get the configServerController from the sprint context
            final ConfigServerController controller = (ConfigServerController) context.getBean("configServerController");

            boolean initialized = controller.initialize();
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
                                    String tips = String.format("the shutdown action of ConfigServer cost %s ms", consumingTime);
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
        } catch (Throwable t) {
            t.printStackTrace();
            ConfigServerLogger.error("configServer startup failed.", t);
        }
    }
}