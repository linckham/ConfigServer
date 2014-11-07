package com.cmbc.configserver.common.compress;

/**
 * the compress/un-compress algorithm type
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/6
 * @Time 14:56
 */
public enum CompressType {
    GZIP(0),
    QUICK_LZ(1),
    ZLIB(2);
    private int type;

    private CompressType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public static CompressType getCompressType(int type) {
        switch (type) {
            case 0:
                return GZIP;
            case 1:
                return QUICK_LZ;
            case 2:
                return ZLIB;
            default:
                return GZIP;
        }
    }
}
