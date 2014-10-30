package com.cmbc.configserver.domain;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/30
 * @Time 18:31
 */
public class ConfigClientMapping {
    private int configId;
    private String clientId;

    public int getConfigId() {
        return configId;
    }

    public void setConfigId(int configId) {
        this.configId = configId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
