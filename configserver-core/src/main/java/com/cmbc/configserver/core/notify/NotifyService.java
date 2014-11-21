package com.cmbc.configserver.core.notify;

import com.cmbc.configserver.common.RemotingSerializable;
import com.cmbc.configserver.common.ThreadFactoryImpl;
import com.cmbc.configserver.common.protocol.RequestCode;
import com.cmbc.configserver.core.event.Event;
import com.cmbc.configserver.core.event.EventService;
import com.cmbc.configserver.core.event.EventType;
import com.cmbc.configserver.core.heartbeat.HeartbeatService;
import com.cmbc.configserver.core.server.ConfigNettyServer;
import com.cmbc.configserver.core.service.CategoryService;
import com.cmbc.configserver.core.service.ConfigServerService;
import com.cmbc.configserver.core.subscriber.SubscriberService;
import com.cmbc.configserver.domain.Category;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.domain.Notify;
import com.cmbc.configserver.remoting.protocol.RemotingCommand;
import com.cmbc.configserver.utils.ConfigServerLogger;
import com.cmbc.configserver.utils.Constants;
import com.cmbc.configserver.utils.PathUtils;
import com.cmbc.configserver.utils.StatisticsLog;
import io.netty.channel.Channel;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * the notify service uses to manage change event of the configuration.<br/>
 * It receive the change event and get the latest configuration from database, eventually push the configuration to subscriber channels.<br/>
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/31
 * @Time 11:12
 */
@Service("notifyService")
public class NotifyService implements InitializingBean,DisposableBean {
    private static final int MAX_DELAY_TIME = 3 * 60 * 1000;
    private volatile boolean stop = true;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1,new ThreadFactoryImpl("event-dispatcher-"));
    /**
     * the executor uses to load the specified path's all configuration items.
     */
    private ThreadPoolExecutor configLoadExecutor;
    /**
     * the executor uses to notify all the channels that subscribe the specified path
     */
    private ThreadPoolExecutor subscriberNotifyExecutor;
    @Autowired
    private ConfigServerService configServerService;
    @Autowired
    private ConfigNettyServer configNettyServer;
    @Autowired
    private HeartbeatService heartbeatService;
    @Autowired
    private SubscriberService subscriberService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private EventService<Event> eventService;

    public ConfigNettyServer getConfigNettyServer() {
        return configNettyServer;
    }

    private void initialize() {
        this.scheduler.execute(new EventDispatcher());
        this.configLoadExecutor = new ThreadPoolExecutor(2, 8, 60 * 1000,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(Constants.DEFAULT_MAX_QUEUE_ITEM),
                new ThreadFactoryImpl("config-load-thread-"));

        this.subscriberNotifyExecutor = new ThreadPoolExecutor(8,32,60*1000,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(Constants.DEFAULT_MAX_QUEUE_ITEM),
                new ThreadFactoryImpl("subscriber-notify-thread-"));
        StatisticsLog.registerExecutor("config-load-pool",this.configLoadExecutor);
        StatisticsLog.registerExecutor("subscriber-notify-pool",this.subscriberNotifyExecutor);
    }

    public boolean start() {
        this.stop = false;
        initialize();
        return true;
    }

    public void stop() {
        this.stop = true;
        this.scheduler.shutdown();
        ConfigServerLogger.info("NotifyService has been stopped!");
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    private void onConfigChanged(Event event) {
        this.configLoadExecutor.execute(new ConfigurationLoadWorker(event));
    }

    private void onPathDataChanged(Event event) {
        this.configLoadExecutor.execute(new ConfigurationLoadWorker(event));
    }

    private void onCategoryChanged(Event event){
        this.configLoadExecutor.execute(new ConfigurationLoadWorker(event));
    }

    /**
     * the worker that using to load specified path's configuration from database when the specified event happened
     */
    class ConfigurationLoadWorker implements Runnable {
        private Event event;
        public ConfigurationLoadWorker(Event event) {
            this.event = event;
        }

        @Override
        public void run() {
            if (EventType.PUBLISH == event.getEventType() || EventType.UN_PUBLISH == event.getEventType()) {
                Configuration config = (Configuration) event.getEventSource();
                this.notify(config);
            } else if (EventType.PATH_DATA_CHANGED == event.getEventType()) {
                Configuration config = PathUtils.path2Configuration((String) event.getEventSource());
                this.notify(config);
            } else if (EventType.CATEGORY_CHANGED == event.getEventType()) {
                //the resources of the cell has happened
                this.notify((Category) event.getEventSource());
            }
        }

        private void notify(Configuration config) {
            if (null != config) {
                try {
                    // get the configuration list which is the latest version in the server.
                    List<Configuration> configList = NotifyService.this.configServerService.getConfigurationList(config);
                    String subscriberPath = PathUtils.getSubscriberPath(config);
                    doNotify(subscriberPath, configList);
                } catch (Exception e) {
                    ConfigServerLogger.error("ConfigurationLoadWorker notify failed, details is ", e);
                }
            }
        }

        /**
         * notify all the resources of the specified cell
         */
        private void notify(Category category) {
            if (null != category) {
                try {
                    List<String> resourcesList = NotifyService.this.categoryService.getResources(category.getCell());
                    if (resourcesList != null && !resourcesList.isEmpty()) {
                        // build the configuration list
                        List<Configuration> configurationList = new ArrayList<Configuration>(resourcesList.size());
                        for (String resource : resourcesList) {
                            Configuration config = new Configuration();
                            config.setResource(resource);
                            configurationList.add(config);
                        }
                        String path = Constants.PATH_SEPARATOR + category.getCell();
                        doNotify(path, configurationList);
                    }
                } catch (Exception ex) {
                    ConfigServerLogger.error("ConfigurationLoadWorker process failed, details is ", ex);
                }
            }
        }

        private void doNotify(String path, List<Configuration> configurations) {
            Notify notify = new Notify();
            notify.setPath(path);
            notify.setConfigLists(configurations);
            byte[] body = RemotingSerializable.encode(notify);
            //get the subscriber's channels that will being to notify
            Set<Channel> subscriberChannels = NotifyService.this.subscriberService.getSubscriberChannels(path);
            if (null != subscriberChannels && !subscriberChannels.isEmpty()) {
                ConfigServerLogger.info(String.format("subscriber channel of path %s has %d items,channel details is %s",path,subscriberChannels.size(),subscriberChannels));
                for (Channel channel : subscriberChannels) {
                    if (null != channel && channel.isActive()) {
                        subscriberNotifyExecutor.execute(new SubscriberNotifyWorker(channel, body));
                    }
                }
            }
        }
    }

    /**
     * this worker uses to notify the specified command to the subscriber
     */
    class SubscriberNotifyWorker implements Runnable {
        private Channel channel;
        private byte[] body;
        public  SubscriberNotifyWorker(Channel channel,byte[] body){
            this.channel = channel;
            this.body = body;
        }
        @Override
        public void run() {
            notifySubscriber(channel,body);
        }
    }

    private void notifySubscriber(Channel channel, byte[] body) {
        if(null != channel && channel.isActive()){
            try {
                //per request per subscriber channel
                RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.NOTIFY_CONFIG);
                if(null !=body){
                    request.setBody(body);
                }
                this.getConfigNettyServer().getRemotingServer().invokeSync(channel, request, Constants.DEFAULT_SOCKET_READING_TIMEOUT);
                //avoiding to kill this channel,update the subscriber channel's heart beat time when push the notify message on the channel.
                heartbeatService.updateHeartbeat(channel);
            } catch (Exception ex) {
                ConfigServerLogger.warn(String.format("notify the configuration to subscriber %s failed.", channel), ex);
            }
        }
    }

    /**
     * the event dispatcher
     */
    class EventDispatcher implements Runnable {
        @Override
        public void run() {
            while (!stop && !Thread.interrupted()) {
                try {
                    Event event = NotifyService.this.eventService.getQueue().poll(Constants.DEFAULT_QUEUE_TIMEOUT, TimeUnit.MILLISECONDS);
                    if (null != event) {
                        long delayTime = System.currentTimeMillis() - event.getEventCreatedTime();
                        if (delayTime <= MAX_DELAY_TIME) {
                            EventType eventType = event.getEventType();
                            if (EventType.PUBLISH == eventType || EventType.UN_PUBLISH == eventType) {
                                onConfigChanged(event);
                            } else if (EventType.PATH_DATA_CHANGED == eventType) {
                                onPathDataChanged(event);
                            }
                            else if(EventType.CATEGORY_CHANGED == eventType){
                                onCategoryChanged(event);
                            }
                        } else {
                            ConfigServerLogger.warn(String.format("dispatcher event %s after the current time too much, so ignore it!", event));
                        }
                    }
                } catch (Throwable t) {
                    ConfigServerLogger.error("EventDispatcher process event failed, details is ", t);
                }
            }
        }
    }
}