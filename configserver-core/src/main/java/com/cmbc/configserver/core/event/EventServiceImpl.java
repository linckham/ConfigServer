package com.cmbc.configserver.core.event;

import com.cmbc.configserver.utils.ConfigServerLogger;
import com.cmbc.configserver.utils.Constants;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/21
 * @Time 9:39
 */
@Service("eventService")
public class EventServiceImpl implements EventService<Event> {
    private LinkedBlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>(Constants.DEFAULT_MAX_QUEUE_ITEM);

    @Override
    /**
     * publish the event to queue
     *
     */
    public void publish(Event event) {
        if (this.eventQueue.offer(event)) {
            ConfigServerLogger.info(String.format("new event %s is enqueue,the size of queue is %d", event, eventQueue.size()));
        } else {
            ConfigServerLogger.warn(String.format("new event %s can not enqueue due to the queue is full", event));
        }
    }

    @Override
    public BlockingQueue<Event> getQueue() {
        return this.eventQueue;
    }
}
