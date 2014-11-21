package com.cmbc.configserver.remoting.protocol;

import com.cmbc.configserver.common.compress.CompressType;
import com.cmbc.configserver.common.serialize.SerializeType;

/**
 * the util class that uses to manage the Request/Response command flag
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/6
 * @Time 14:25
 */
public class CommandFlagUtils {
    public static void markCompressFlag(RemotingHeader header) {
        if (null != header) {
            int flag = header.getFlag();
            header.setFlag(flag | 1 << 3);
        }
    }

    public static boolean isCompressed(RemotingHeader header) {
        if (null != header) {
            int flag = header.getFlag();
            return (flag & (1 << 3)) == 1 << 3;
        }
        return false;
    }

    public static SerializeType getSerializeType(RemotingHeader header) {
        int type = 0;
        if (null != header) {
            int flag = header.getFlag();
            type = (flag & 0xF0) >> 4;
        }
        return SerializeType.getSerializeType(type);
    }

    public static void setSerializeType(RemotingHeader header, SerializeType type) {
        if (null != header) {
            int flag = header.getFlag();
            // clear the old serialize type value
            int tmp = flag & 0xFFFFFF0F;
            //set the new value to serialize type
            tmp = tmp | (type.getType() << 4);
            flag = tmp;
            header.setFlag(flag);
        }
    }

    public static CompressType getCompressType(RemotingHeader header) {
        int type = 0;
        if (null != header) {
            int flag = header.getFlag();
            type = flag & 0x07;
        }
        return CompressType.getCompressType(type);
    }

    public static void setCompressType(RemotingHeader header, CompressType type) {
        if (null != header) {
            int flag = header.getFlag();
            int tmp = flag & 0xFFFFFFF8;
            tmp = tmp | type.getType();
            flag = tmp;
            header.setFlag(flag);
        }
    }
}
