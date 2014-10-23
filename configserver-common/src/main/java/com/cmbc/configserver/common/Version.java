package com.cmbc.configserver.common;

/**
 * the class use to maintain the ConfigServer's communication protocol version
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @author tongchuan.lin<linckham@gmail.com>.
 *         Date: 2014/10/23
 *         Time: 15:03
 */
public enum Version {
    V1(1, "the first version");
    private int version;
    private String description;

    Version(int version, String description) {
        this.version = version;
        this.description = description;
    }

    public int getVersion() {
        return this.version;
    }

    @Override
    public String toString() {
        return "Version{" +
                "version=" + version +
                ", description='" + description + '\'' +
                '}';
    }
}
