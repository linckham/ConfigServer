package com.cmbc.configserver.domain;

import com.cmbc.configserver.common.RemotingSerializable;

import java.util.List;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @author tongchuan.lin<linckham@gmail.com>.
 *         Date: 2014/10/27
 *         Time: 10:37
 */
public class Notify extends RemotingSerializable {
    private String path;
    private List<Configuration> configLists;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Configuration> getConfigLists() {
        return configLists;
    }

    public void setConfigLists(List<Configuration> configLists) {
        this.configLists = configLists;
    }

    @Override
    public String toString() {
        return "Nofity{" +
                "path='" + path + '\'' +
                ", configLists=" + configLists +
                '}';
    }
}
