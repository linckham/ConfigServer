package com.cmbc.configserver.utils;

import com.cmbc.configserver.domain.Configuration;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * the algorithm of the specified path's configuration list<br/>
 */
public class ClusterSignAlgorithm {
    public static final String DEFAULT_SIGN = MD5Utils.md5("default");

    public static String calculate(List<Configuration> configList) {
        if (configList != null) {
            SortedSet<String> sortedList = new TreeSet<String>();
            for (Configuration config : configList) {
                if (null != config.getNode()) {
                    //TODO: consider which property will being contained in MD5 calculate
                    if (StringUtils.isNotBlank(config.getNode().getData())) {
                        sortedList.add(config.getCell() + config.getResource() + config.getType() + config.getNode().getData());
                    }
                } else {
                    sortedList.add(config.getCell() + config.getResource() + config.getType());
                }
            }
            if (sortedList.size() > 0) {
                return MD5Utils.md5(sortedList.toString());
            } else {
                return DEFAULT_SIGN;
            }
        } else {
            return DEFAULT_SIGN;
        }
    }

    public static final boolean equals(String sign, String anotherSign) {
        if (StringUtils.equals(sign, anotherSign)) {
            return true;
        } else if (StringUtils.isBlank(sign) && ClusterSignAlgorithm.DEFAULT_SIGN.equals(anotherSign)) {
            return true;
        } else if (StringUtils.isBlank(anotherSign) && ClusterSignAlgorithm.DEFAULT_SIGN.equals(sign)) {
            return true;
        } else {
            return false;
        }
    }
}