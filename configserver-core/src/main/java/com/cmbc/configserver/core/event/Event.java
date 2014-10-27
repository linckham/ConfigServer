package com.cmbc.configserver.core.event;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @author tongchuan.lin<linckham@gmail.com>.
 *         Date: 2014/10/24
 *         Time: 15:12
 */
public class Event {
    private EventType eventType;
    private Object eventSource;

    /**
     * the time that event has been created.
     * from this field,we can the delay between created and processed.
     */
    private long eventCreatedTime;

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Object getEventSource() {
        return eventSource;
    }

    public void setEventSource(Object eventSource) {
        this.eventSource = eventSource;
    }

    public long getEventCreatedTime() {
        return eventCreatedTime;
    }

    public void setEventCreatedTime(long eventCreatedTime) {
        this.eventCreatedTime = eventCreatedTime;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventType=" + eventType +
                ", eventSource=" + eventSource +
                ", eventCreatedTime=" + eventCreatedTime +
                '}';
    }
}