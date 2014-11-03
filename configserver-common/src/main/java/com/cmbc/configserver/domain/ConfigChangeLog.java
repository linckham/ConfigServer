package com.cmbc.configserver.domain;

/**
 * the domain of configuration changed log.<br/>
 * It uses md5 signature to mark the configuration of the specified path has been changed.<br/>
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/30
 * @Time 19:41
 */
public class ConfigChangeLog {
    public final static ConfigChangeLog EMPTY_MESSAGE = new ConfigChangeLog();
    private String path;
    private String md5;

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
