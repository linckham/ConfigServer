package com.cmbc.configserver.core.subscriber;

import com.cmbc.configserver.utils.ConcurrentHashSet;
import com.cmbc.configserver.utils.ConfigServerLogger;

import java.util.Collections;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;

import com.cmbc.configserver.utils.Constants;

import io.netty.channel.Channel;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * the class manages the subscriber channel between config-client and config-server.<br/>
 * meanwhile,it also manages the mapping relation between path and channel.<br/>
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/31
 * @Time 11:12
 */
@Service("subscriberService")
public class SubscriberService {
    private Map</*path*/String, Set<Channel>> path2ChannelMap = new ConcurrentHashMap<String, Set<Channel>>(Constants.DEFAULT_INITIAL_CAPACITY);
    private Map</*channel*/Channel, Set<String>> channel2PathMap = new ConcurrentHashMap<Channel, Set<String>>(32);
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    /**
     * add the subscriber's channel to the path
     *
     * @param path    the path that subscriber will being subscribed.
     * @param channel the subscriber's channel
     * @return true if subscribe success,else false
     */
    public boolean subscribe(String path, Channel channel) {
        try {
            //valid the path and the channel whether is active
            if(StringUtils.isBlank(path) || (null == channel || !channel.isActive())){
                return false;
            }

            writeLock.tryLock(Constants.DEFAULT_READ_WRITE_LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
            Set<Channel> channelSet = path2ChannelMap.get(path);
            if (null == channelSet) {
                channelSet = new ConcurrentHashSet<Channel>(16);
                this.path2ChannelMap.put(path, channelSet);
            }
            channelSet.add(channel);

            //add the path to the subscriber channel
            Set<String> pathSet = this.channel2PathMap.get(channel);
            if (null == pathSet) {
                pathSet = new ConcurrentHashSet<String>(32);
                this.channel2PathMap.put(channel, pathSet);
            }
            pathSet.add(path);

            return true;
        } catch (InterruptedException ex) {
            ConfigServerLogger.warn(String.format("add the subscribe channel %s to the path %s failed.", channel, path), ex);
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * unregister the subscriber channel from  the path
     *
     * @param path    the path that subscriber will being un subscribe.
     * @param channel the un subscriber's channel
     * @return true if un subscribe success,else false
     */
    public boolean unSubcribe(String path, Channel channel) {
        try {
            //valid the path
            if(StringUtils.isBlank(path)){
                return false;
            }
            writeLock.tryLock(Constants.DEFAULT_READ_WRITE_LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
            Set<Channel> channelSet = path2ChannelMap.get(path);
            if (null == channelSet) {
                return false;
            }
            channelSet.remove(channel);
            if (channelSet.isEmpty()) {
                this.path2ChannelMap.remove(path);
            }
            //remove the path from the channel set
            Set<String> pathSet = this.channel2PathMap.get(channel);
            if (null != pathSet) {
                pathSet.remove(path);
                if (pathSet.isEmpty()) {
                    this.channel2PathMap.remove(channel);
                }
            }
            return true;
        } catch (InterruptedException ex) {
            ConfigServerLogger.warn(String.format("un subscribe channel %s from the path %s failed.", channel, path), ex);
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * get the subscriber's channel list of the specified path
     *
     * @param path the specified path
     * @return subscriber's channel list
     */
    @SuppressWarnings({"unchecked"})
    public Set<Channel> getSubscriberChannels(String path) {
        try {
            //valid the path
            if(StringUtils.isBlank(path)){
                return Collections.EMPTY_SET;
            }

            readLock.tryLock(Constants.DEFAULT_READ_WRITE_LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
            return this.path2ChannelMap.get(path);
        } catch (InterruptedException ex) {
            ConfigServerLogger.warn(String.format("get subscribe channel of the path %s failed.", path), ex);
            return null;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * get the subscriber's path list of the specified channel
     *
     * @param channel the specified channel
     * @return subscriber's path list
     */
    @SuppressWarnings({"unchecked"})
    public Set<String> getPathByChannel(Channel channel) {
        try {

            if(null == channel){
                return Collections.EMPTY_SET;
            }

            readLock.tryLock(Constants.DEFAULT_READ_WRITE_LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
            return this.channel2PathMap.get(channel);
        } catch (InterruptedException ex) {
            ConfigServerLogger.warn(String.format("get subscribe path list of the channel %s failed.", channel), ex);
            return null;
        } finally {
            readLock.unlock();
        }
    }
    
    /**
     * clear channel's subscribe info 
     * @param channel
     */
    public boolean clearChannel(Channel channel){
    	
    	try{
    		writeLock.tryLock(Constants.DEFAULT_READ_WRITE_LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
    		Set<String> paths = this.channel2PathMap.get(channel);
    		if(paths != null){
    			for(String path : paths){
    				Set<Channel> channelSet = path2ChannelMap.get(path);
    				if(channelSet != null){
    					channelSet.remove(channel);
    					if (channelSet.isEmpty()) {
    		                this.path2ChannelMap.remove(path);
    		            }
    				}
    			}
    		}
    		
    		channel2PathMap.remove(channel);
    		return true;
    	}catch (InterruptedException ex) {
            ConfigServerLogger.warn(String.format("clear channel %s failed.", channel), ex);
            return false;
        } finally {
            writeLock.unlock();
        }
    }
}
