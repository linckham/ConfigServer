package com.cmbc.configserver.common.compress;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * the compress factory which can provide many different compress algorithm.
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/6
 * @Time 21:48
 */
public class CompressFactory {
    private final static Map</**/Integer,Compress> compressMap = new ConcurrentHashMap<Integer,Compress>(8);

    public static Compress createCompress(CompressType type){
        Compress compress = compressMap.get(type.getType());
        if(null == compress){
            //double check and lock the map
            synchronized (compressMap){
                compress = compressMap.get(type.getType());
                if(null == compress){
                    switch (type.getType()){
                        case 0:
                        case 2:
                        case 1:
                            compress = new QuickLZ();
                            break;
                        default:
                            compress = new QuickLZ();
                            break;
                    }
                    compressMap.put(type.getType(),compress);
                }
            }
        }
        return compress;
    }
}
