package com.cmbc.configserver.utils;

import com.cmbc.configserver.common.ThreadFactoryImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/21
 * @Time 13:25
 */
public class StatisticsLogTest {
    private ThreadPoolExecutor threadPool;

    @Before
    public void setUp() {
        threadPool = new ThreadPoolExecutor(1, 4, 0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(20),
                new ThreadFactoryImpl("statistics-log-test-"));
    }

    @Test
    public void testStatLog() {
        StatisticsLog.setPausePrint(true);
        StatisticsLog.registerExecutor("test-pool", threadPool);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        System.out.println(StatisticsLog.isOutOfMemory());
                        TimeUnit.MILLISECONDS.sleep(6 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }, "test-outMemory");
        t.setDaemon(true);
        t.start();

        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        StatisticsLog.setPausePrint(false);
        threadPool.shutdown();
    }
}