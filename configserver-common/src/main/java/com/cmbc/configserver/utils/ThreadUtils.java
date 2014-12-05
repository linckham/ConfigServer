package com.cmbc.configserver.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * the helper class for thread
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/24
 * @Time 10:36
 */
public class ThreadUtils {
    /**
     * gracefully shutdown the ExecutorService
     * @param pool the executor service that will being shutdown
     */
    public static void shutdownAndAwaitTermination(ExecutorService pool) {
        if (null != pool) {
            // Disable new tasks from being submitted
            pool.shutdown();
            try {
                // Wait a while for existing tasks to terminate
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    pool.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                        ConfigServerLogger.warn(String.format("Pool %s did not terminate", pool));
                    }
                }
            } catch (InterruptedException ex) {
                // (Re-)Cancel if current thread also interrupted
                pool.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void safeSleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            //ignore it
        }
    }
}
