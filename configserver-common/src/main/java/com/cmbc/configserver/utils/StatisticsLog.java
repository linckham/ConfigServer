package com.cmbc.configserver.utils;

import org.apache.log4j.Logger;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;


public class StatisticsLog implements Runnable {
    private static Logger log = Logger.getLogger("debug_stat");
    private static AtomicBoolean outOfMemory = new AtomicBoolean(false);
    private static AtomicBoolean pausePrint = new AtomicBoolean(false);
    private static Map<String, ThreadPoolExecutor> executors = new ConcurrentHashMap<String, ThreadPoolExecutor>();

    static {
        printStat(5000);
    }

    private static long startTime;
    private static long interval;

    public StatisticsLog(long startTime2, long interval2) {
        startTime = startTime2;
        interval = interval2;
    }

    public static void setPausePrint(boolean print) {
        pausePrint.set(print);
    }

    public static void registerExecutor(String name, ThreadPoolExecutor executor) {
        executors.put(name, executor);
    }

    /**
     * print stat info on the screen, this method will block until total is reached,
     *
     * @param interval how long (second) to print a stat log
     */
    public static StatisticsLog printStat(long interval) {
        log.info("Start statistics log thread.");
        StatisticsLog t = new StatisticsLog(System.currentTimeMillis(), interval);
        Thread thread = new Thread(t, "Statistics-Log-Thread-");
        thread.setDaemon(true);
        thread.start();
        return t;
    }

    public void run() {
        while (true) {
            try {
                synchronized (this) {
                    wait(interval);
                }
                if (pausePrint.get())
                    continue;

                long time2 = System.currentTimeMillis();
                if (time2 == startTime)
                    continue;
                log.info("---------------------------");
                log.info("JAVA HEAP: " + memoryReport() + ", UP TIME: " + ((time2 - startTime) / 1000) + ", min: " + ((time2 - startTime) / 60000));
                StringBuilder sb = new StringBuilder("thread-pool:[");
                StringBuilder jsonLog = new StringBuilder();
                jsonLog.append("[");
                int i = 0;
                for (Map.Entry<String, ThreadPoolExecutor> entry : executors.entrySet()) {
                    sb.append(statExecutor(entry.getKey(), entry.getValue())).append(", ");
                    if (i++ > 0) {
                        jsonLog.append(",");
                    }
                    jsonLog.append(statJsonExecutor(entry.getKey(), entry.getValue()));
                }
                jsonLog.append("]");
                sb.append(jsonLog);
                log.info(sb.append(" ]"));
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        log.info("Stat log stop");
        log.info("---------------------------");
    }

    public static String memoryReport() {
        Runtime runtime = Runtime.getRuntime();
        double freeMemory = (double) runtime.freeMemory() / (1024 * 1024);
        double maxMemory = (double) runtime.maxMemory() / (1024 * 1024);
        double totalMemory = (double) runtime.totalMemory() / (1024 * 1024);
        double usedMemory = totalMemory - freeMemory;
        double percentFree = ((maxMemory - usedMemory) / maxMemory) * 100.0;
        if (percentFree < 10) {
            outOfMemory.set(true);
            log.error("Detected OutOfMemory potential memory > 90%, stop broadcast presence !!!!!!");
        } else if (outOfMemory.get() && percentFree > 20) {
            outOfMemory.set(false);
            log.error("Detected memory return to normal, memory < 80%, resume broadcast presence.");
        }

        double percentUsed = 100 - percentFree;
        DecimalFormat mbFormat = new DecimalFormat("#0.00");
        DecimalFormat percentFormat = new DecimalFormat("#0.0");

        StringBuilder sb = new StringBuilder(" ");
        sb.append(mbFormat.format(usedMemory)).append("MB of ").append(mbFormat.format(maxMemory))
                .append(" MB (").append(percentFormat.format(percentUsed)).append("%) used");
        return sb.toString();
    }

    public static boolean isOutOfMemory() {
        return outOfMemory.get();
    }

    private String statExecutor(String name, ThreadPoolExecutor executor) {
        StringBuilder strBuf = new StringBuilder(32);
        strBuf.append(name).append("{").append(executor.getQueue().size()).append(",")
                .append(executor.getCompletedTaskCount()).append(",")
                .append(executor.getTaskCount()).append(",")
                .append(executor.getActiveCount()).append(",")
                .append(executor.getCorePoolSize()).append("}\n");
        return strBuf.toString();
    }

    private String statJsonExecutor(String name, ThreadPoolExecutor executor) {
        StringBuilder jsonBuilder = new StringBuilder(128);
        jsonBuilder.append("{\"name\":").append(name).append(",");
        jsonBuilder.append("\"act\":").append(executor.getActiveCount()).append(",");
        jsonBuilder.append("\"max\":").append(executor.getCorePoolSize());
        return jsonBuilder.append("}").toString();
    }
}