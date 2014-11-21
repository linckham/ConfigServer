package com.cmbc.configserver.core.notify;

import com.cmbc.configserver.common.ThreadFactoryImpl;
import com.cmbc.configserver.core.event.Event;
import com.cmbc.configserver.core.event.EventService;
import com.cmbc.configserver.core.event.EventType;
import com.cmbc.configserver.core.service.ConfigChangeLogService;
import com.cmbc.configserver.domain.ConfigChangeLog;
import com.cmbc.configserver.utils.ConcurrentHashSet;
import com.cmbc.configserver.utils.ConfigServerLogger;
import com.cmbc.configserver.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * the notify service uses to manage the change of config_change_log.<br/>
 * 1 when the config_change_log has been changed,this service will produce a event and publish it to NotifyService.<br/>
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/31
 * @Time 16:10
 */
@Service("configChangedNotifyService")
public class ConfigChangedNotifyService {
    private Map</*path*/String,/*md5*/Long> pathMd5Cache = new ConcurrentHashMap<String, Long>(Constants.DEFAULT_INITIAL_CAPACITY);
    private volatile boolean stop = true;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1,new ThreadFactoryImpl("change-log-notify-"));
    @Autowired
    private ConfigChangeLogService configChangeLogService;
    @Autowired
    private EventService<Event> eventService;

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
     */
    public void updatePathMd5Cache(String path,Long last_modified_time){
        this.pathMd5Cache.put(path,last_modified_time);
    }

    private List<ConfigChangeLog> getAllConfigChangeLogs() throws Exception {
        return this.configChangeLogService.getAllConfigChangeLogs();
    }

    class ChangedWorker implements Runnable {
        private AtomicLong loadingTimes = new AtomicLong(0);
        @Override
        public void run() {
            while (!stop && !Thread.interrupted()) {
                try {
                    List<ConfigChangeLog> changeLogList = getAllConfigChangeLogs();
                    long times = loadingTimes.incrementAndGet();
                    //reduce the statistics log
                    if (times % 64 == 1) {
                        ConfigServerLogger.info(String.format("ChangedWorker getAllConfigChangeLogs from database. size = %s, changeLogs = %s",
                                changeLogList == null ? 0 : changeLogList.size(), changeLogList));
                    }
                    if (times >= Long.MAX_VALUE) {
                        loadingTimes.set(0);
                    }

                    if (null != changeLogList && !changeLogList.isEmpty()) {
                        for (ConfigChangeLog changeLog : changeLogList) {
                            //the last modify time in local cache is not equals the value in database
                            //fix the bug: change log's modify time is long,and the value of path in Cache is Long,when the value is null,compare the long with
                            //null,It may be thrown the JNP exception
                            if ((pathMd5Cache.get(changeLog.getPath()) == null)||(changeLog.getLastModifiedTime() != pathMd5Cache.get(changeLog.getPath()))) {
                                ConfigServerLogger.warn(String.format("the path %s has changed,last_modified_time is %s",
                                        changeLog.getPath(), changeLog.getLastModifiedTime()));
                                Event event = new Event();
                                event.setEventType(EventType.PATH_DATA_CHANGED);
                                event.setEventSource(changeLog.getPath());
                                event.setEventCreatedTime(System.currentTimeMillis());
                                eventService.publish(event);
                            }
                            pathMd5Cache.put(changeLog.getPath(), changeLog.getLastModifiedTime());
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
