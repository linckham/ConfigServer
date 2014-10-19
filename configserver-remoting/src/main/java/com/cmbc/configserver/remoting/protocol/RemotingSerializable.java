package com.cmbc.configserver.remoting.protocol;

import java.nio.charset.Charset;



/**
 * the serialization of the Object.
 * @author tongchuan.lin<linckham@gmail.com>
 * @since 2014年10月17日 下午3:54:11
 */
public abstract class RemotingSerializable {
    public String toJson() {
        return toJson(false);
    }


    public String toJson(final boolean prettyFormat) {
        return toJson(this, prettyFormat);
    }


    public static String toJson(final Object obj, boolean prettyFormat) {
        return null;
    	//return JSON.toJSONString(obj, prettyFormat);
    }


    public static <T> T fromJson(String json, Class<T> classOfT) {
        return null;
    	//return JSON.parseObject(json, classOfT);
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


    public static <T> T decode(final byte[] data, Class<T> classOfT) {
        final String json = new String(data, Charset.forName("UTF-8"));
        return fromJson(json, classOfT);
    }
}
