package com.cmbc.configserver.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * because of System.currentTimeMillis need switch content between user and kernel,so we cache the system time.
 * Usage Case: the accurate of the time is low than 10 ms, can use this class to get system time
 */
public class SystemTimer {
    private final static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private static final long tickUnit = Long.parseLong(System.getProperty("commons.systimer.tick", "10"));
    private static volatile long time = System.currentTimeMillis();

    private static class TimerTicker implements Runnable {
        public void run() {
            time = System.currentTimeMillis();
        }
    }

    public static long currentTimeMillis() {
        return time;
    }

    static {
        executor.scheduleAtFixedRate(new TimerTicker(), tickUnit, tickUnit, TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                executor.shutdown();
            }
        });
    }
}