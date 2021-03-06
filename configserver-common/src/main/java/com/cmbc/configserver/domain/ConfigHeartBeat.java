package com.cmbc.configserver.domain;

/**
 * the domain uses to manage the configuration heart beat.<br/>
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/30
 * @Time 20:32
 */
public class ConfigHeartBeat {
    public final static ConfigHeartBeat EMPTY_MESSAGE = new ConfigHeartBeat();
	public static long DB_TIMEOUT = 60 * 1000;
	
    private String clientId;
    private long lastModifiedTime;
    
    public ConfigHeartBeat(){
    }

    public ConfigHeartBeat(String clientId,long lastModifiedTime){
    	this.clientId = clientId;
    	this.lastModifiedTime = lastModifiedTime;
    }
    
    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfigHeartBeat that = (ConfigHeartBeat) o;

        return lastModifiedTime == that.lastModifiedTime && !(clientId != null ? !clientId.equals(that.clientId) : that.clientId != null);

    }

    @Override
    public int hashCode() {
        return clientId != null ? clientId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ConfigHeartBeat{" +
                "clientId='" + clientId + '\'' +
                ", lastModifiedTime=" + lastModifiedTime +
                '}';
    }
}