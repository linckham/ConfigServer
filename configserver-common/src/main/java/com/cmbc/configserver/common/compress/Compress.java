package com.cmbc.configserver.common.compress;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/6
 * @Time 21:47
 */
public interface Compress {
    public byte[] compress(byte[] source);
    public byte[] decompress(byte[] source);
}
