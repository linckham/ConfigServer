package com.cmbc.configserver.core.notify;

import com.cmbc.configserver.core.dao.ConfigChangeLogDao;
import com.cmbc.configserver.core.event.Event;
import com.cmbc.configserver.core.event.EventType;
import com.cmbc.configserver.domain.ConfigChangeLog;
import com.cmbc.configserver.utils.ConfigServerLogger;
import com.cmbc.configserver.utils.Constants;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

/**
 * the notify service uses to manage the change of config_change_log.<br/>
 * when the config_change_log has been changed,this service will produce a event and publish it to NotifyService.<br/>
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/31
 * @Time 16:10
 */
public class ConfigChangedNotifyService {
    private Map</*path*/String,/*md5*/Long> pathMd5Cache = new ConcurrentHashMap<String, Long>(Constants.DEFAULT_INITIAL_CAPACITY);
    private volatile boolean stop = true;
    private ExecutorService scheduler = Executors.newFixedThreadPool(1);
    private ConfigChangeLogDao configChangeLogDao;
    private NotifyService notifyService;

    public void setNotifyService(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    public void setConfigChangeLogDao(ConfigChangeLogDao configChangeLogDao) {
        this.configChangeLogDao = configChangeLogDao;
    }

    public boolean start() throws Exception {
        this.stop = false;
        initialize();
        return true;
    }

    private void initialize() {
        this.scheduler.execute(new ChangedWorker());
    }

    public void stop() {
        this.stop = true;
        this.scheduler.shutdown();
    }

    /**
     * update the path's md5
     * @param path
     * @param last_modified_time
     */
    public void updatePathMd5Cache(String path,Long last_modified_time){
        this.pathMd5Cache.put(path,last_modified_time);
    }

    private List<ConfigChangeLog> getAllConfigChangeLogs() throws Exception {
        //TODO: if the config_change_log has too many record, this way may be not better. consider an better resolution to fix this problem
        return this.configChangeLogDao.getAllConfigChangeLogs();
    }

    class ChangedWorker implements Runnable {
        @Override
        public void run() {
            while (!stop && !Thread.interrupted()) {
                try {
                    List<ConfigChangeLog> changeLogList = getAllConfigChangeLogs();
                    if (null != changeLogList && !changeLogList.isEmpty()) {
                        for (ConfigChangeLog changeLog : changeLogList) {
                            //the last modify time in local cache is not equals the value in database
                            if (changeLog.getLastModifiedTime() != pathMd5Cache.get(changeLog.getPath())) {
                                if (null != pathMd5Cache.get(changeLog.getPath())) {
                                    //doesn't send notify event when the JVM is just starting
                                    //send a event to NotifyService
                                    Event event = new Event();
                                    event.setEventType(EventType.PATH_DATA_CHANGED);
                                    event.setEventSource(changeLog.getPath());
                                    event.setEventCreatedTime(System.currentTimeMillis());
                                    ConfigChangedNotifyService.this.notifyService.publish(event);
                                }
                                pathMd5Cache.put(changeLog.getPath(), changeLog.getLastModifiedTime());
                            }
                        }
                    }
                    TimeUnit.MILLISECONDS.sleep(Constants.DEFAULT_THREAD_SLEEP_TIME);
                } catch (Throwable t) {
                    ConfigServerLogger.warn("error happens when change worker running.", t);
                }
            }
        }
    }
}
