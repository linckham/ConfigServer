package com.cmbc.configserver.core.notify;

import com.cmbc.configserver.common.RemotingSerializable;
import com.cmbc.configserver.common.ThreadFactoryImpl;
import com.cmbc.configserver.common.protocol.RequestCode;
import com.cmbc.configserver.core.event.Event;
import com.cmbc.configserver.core.event.EventType;
import com.cmbc.configserver.core.server.ConfigNettyServer;
import com.cmbc.configserver.core.storage.ConfigStorage;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.domain.Notify;
import com.cmbc.configserver.remoting.protocol.RemotingCommand;
import com.cmbc.configserver.utils.ConfigServerLogger;
import com.cmbc.configserver.utils.Constants;
import com.cmbc.configserver.utils.PathUtils;
import io.netty.channel.Channel;
import org.omg.CORBA.Request;

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
public class NotifyService {
    private static final int MAX_DELAY_TIME = 3 * 60 * 1000;
    private LinkedBlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>(Constants.DEFAULT_MAX_QUEUE_ITEM);
    private volatile boolean stop = true;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    /**
     * the executor uses to load the specified path's all configuration items.
     */
    private ThreadPoolExecutor configLoadExecutor;
    /**
     * the executor uses to notify all the channels that subscribe the specified path
     */
    private ThreadPoolExecutor subscriberNotifyExecutor;
    private ConfigStorage configStorage;
    private ConfigNettyServer configNettyServer;

    public void setConfigStorage(ConfigStorage configStorage) {
        this.configStorage = configStorage;
    }

    public ConfigStorage getConfigStorage() {
        return this.configStorage;
    }

    public ConfigNettyServer getConfigNettyServer() {
        return configNettyServer;
    }

    public void setConfigNettyServer(ConfigNettyServer configNettyServer) {
        this.configNettyServer = configNettyServer;
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

    /**
     * publish the event to queue
     *
     * @param event
     */
    public void publish(Event event) {
        this.eventQueue.offer(event);
    }

    private void onConfigChanged(Event event) {
        this.configLoadExecutor.execute(new ConfigurationLoadWorker(event));
    }

    private void onPathDataChanged(Event event) {
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
                this.doNotify(config);
            } else if (EventType.PATH_DATA_CHANGED == event.getEventType()) {
                Configuration config = PathUtils.path2Configuration((String) event.getEventSource());
                this.doNotify(config);
            }
        }

        private void doNotify(Configuration config) {
            if (null != config) {
                try {
                    // get the configuration list which is the latest version in the server.
                    List<Configuration> configList = NotifyService.this.configStorage.getConfigurationList(config);
                    String subscriberPath = PathUtils.getSubscriberPath(config);
                    Notify notify = new Notify();
                    notify.setPath(subscriberPath);
                    notify.setConfigLists(configList);

                    byte[] body = RemotingSerializable.encode(notify);
                    //get the subscriber's channels that will being to notify
                    Set<Channel> subscriberChannels = NotifyService.this.configStorage.getSubscribeChannel(subscriberPath);
                    if (null != subscriberChannels && !subscriberChannels.isEmpty()) {
                        for (Channel channel : subscriberChannels) {
                            if(null !=channel && channel.isActive()){
                                //notifySubscriber(channel,body);
                                subscriberNotifyExecutor.execute(new SubscriberNotifyWorker(channel, body));
                            }
                        }
                    }
                } catch (Exception e) {
                    ConfigServerLogger.error("ConfigurationLoadWorker process failed, details is ", e);
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

                this.getConfigNettyServer().getRemotingServer()
                        .invokeSync(channel, request, Constants.DEFAULT_SOCKET_READING_TIMEOUT);
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
                    Event event = eventQueue.poll(Constants.DEFAULT_QUEUE_TIMEOUT, TimeUnit.MILLISECONDS);
                    if (null != event) {
                        long delayTime = System.currentTimeMillis() - event.getEventCreatedTime();
                        if (delayTime <= MAX_DELAY_TIME) {
                            EventType eventType = event.getEventType();
                            if (EventType.PUBLISH == eventType || EventType.UN_PUBLISH == eventType) {
                                onConfigChanged(event);
                            } else if (EventType.PATH_DATA_CHANGED == eventType) {
                                onPathDataChanged(event);
                            }
                        } else {
                            //log this event and ignore
                            ConfigServerLogger.warn(String.format("%s after the current time too much,so ignore it!", event));
                        }
                    }
                } catch (Throwable t) {
                    ConfigServerLogger.error("EventDispatcher process event failed, details is ", t);
                }
            }
        }
    }
}