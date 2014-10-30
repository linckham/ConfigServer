package com.cmbc.configserver.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the unified logger for ConfigServer
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @author tongchuan.lin<linckham@gmail.com>.
 *         Date: 2014/10/27
 *         Time: 13:39
 */
public class ConfigServerLogger {
    private static final Logger debug = LoggerFactory.getLogger("debug");
    private static final Logger infoLog = LoggerFactory.getLogger("info");
    private static final Logger warnLog = LoggerFactory.getLogger("warn");
    private static final Logger errorLog = LoggerFactory.getLogger("error");

    public static boolean isDebugEnabled() {
        return debug.isDebugEnabled();
    }

    public static void debug(StringBuilder msg) {
        debug(msg.toString());
    }

    public static void debug(String msg) {
        debug.debug(msg);
    }

    public static void info(StringBuilder msg) {
        info(msg.toString());
    }

    public static void info(String msg) {
        infoLog.info(msg);
    }

    public static void warn(StringBuilder msg) {
        warn(msg.toString());
    }

    public static void warn(String msg) {
        warnLog.warn(msg);
    }

    public static void warn(StringBuilder msg, Throwable e) {
        warn(msg.toString(), e);
    }

    public static void warn(String msg, Throwable e) {
        warnLog.warn(msg, e);
    }

    public static void error(StringBuilder msg) {
        error(msg.toString());
    }

    public static void error(String msg) {
        errorLog.error(msg);
    }

    public static void error(StringBuilder msg, Throwable e) {
        error(msg.toString(), e);
    }

    public static void error(String msg, Throwable e) {
        errorLog.error(msg, e);
    }

    public static boolean isTraceEnabled() {
        return infoLog.isTraceEnabled();
    }
    public static void trace(String msg) {
        infoLog.trace(msg);
    }
}
