package com.cmbc.configserver.common;

import com.alibaba.fastjson.JSON;

import java.nio.charset.Charset;


/**
 * the serialization of the Object.<br/>
 * @author tongchuan.lin<linckham@gmail.com>
 * @since 2014/10/17 3:01:22PM
 */
public abstract class RemotingSerializable {
    public String toJson() {
        return toJson(false);
    }


    public String toJson(final boolean prettyFormat) {
        return toJson(this, prettyFormat);
    }


    public static String toJson(final Object obj, boolean prettyFormat) {
    	return JSON.toJSONString(obj, prettyFormat);
    }


    public static <T> T fromJson(String json, Class<T> classOfT) {
    	return JSON.parseObject(json, classOfT);
    }


    public byte[] encode() {
        final String json = this.toJson();
        if (json != null) {
            return json.getBytes();
        }
        return null;
    }


    public static byte[] encode(final Object obj) {
        final String json = toJson(obj, false);
        if (json != null) {
            return json.getBytes(Charset.forName("UTF-8"));
        }
        return null;
    }

    /**
     * decode the binary data to JAVA Object
     * @param data the binary data that will being decoded
     * @param classOfT the JAVA Object that will being convert to.
     * @return the specified JAVA Object that after decoded
     */
    public static <T> T decode(final byte[] data, Class<T> classOfT) {
        final String json = new String(data, Charset.forName("UTF-8"));
        return fromJson(json, classOfT);
    }
}