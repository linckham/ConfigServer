package com.cmbc.configserver.core.event;

import java.util.concurrent.BlockingQueue;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/21
 * @Time 9:38
 */
public interface EventService<T> {
    public void publish(T event);

    public BlockingQueue<T> getQueue();
}
