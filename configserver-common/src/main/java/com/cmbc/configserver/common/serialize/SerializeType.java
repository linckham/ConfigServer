package com.cmbc.configserver.common.serialize;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/6
 * @Time 14:19
 */
public enum SerializeType {
    FAST_JSON(0),
    PROTOCOL_BUFFER(1),
    HESSIAN(2),
    THRIFT(3);
    private int type;

    private SerializeType(int type) {
        this.type = type;
    }

    public static SerializeType getSerializeType(int type) {
        switch (type) {
            case 0:
                return FAST_JSON;
            case 1:
                return PROTOCOL_BUFFER;
            case 2:
                return HESSIAN;
            case 3:
                return THRIFT;
            default:
                return FAST_JSON;
        }
    }

    public int getType() {
        return this.type;
    }
}
