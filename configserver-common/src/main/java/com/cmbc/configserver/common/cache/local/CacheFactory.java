/**
 * $RCSfile$
 * $Revision: 3144 $
 * $Date: 2005-12-01 14:20:11 -0300 (Thu, 01 Dec 2005) $
 *
 * Copyright (C) 2004-2008 Jive Software. All rights reserved.
 *
 * This software is published under the terms of the GNU Public License (GPL),
 * a copy of which is included in this distribution, or a commercial license
 * agreement with Jive.
 */
package com.cmbc.configserver.common.cache.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

/**
 * Creates Cache objects. The returned caches will either be local or clustered
 * depending on the clustering enabled setting and a user's license.
 * <p/>
 * <p/>
 * When clustered caching is turned on, cache usage statistics for all caches
 * that have been created are periodically published to the clustered cache
 * named "opt-$cacheStats".
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class CacheFactory {
    private static long SECOND = 1000L;
    private static long MINUTE = 60 * SECOND;
    private static long HOUR = 60 * MINUTE;
    private static Map<String, Cache> caches = new ConcurrentHashMap<String, Cache>();
    private static CacheFactoryStrategy cacheFactoryStrategy;
    public static final long DEFAULT_MAX_CACHE_SIZE = 1024L * 1024 * 128;
    public static final long DEFAULT_MAX_CACHE_LIFETIME = 24 * HOUR;
    /**
     * Default properties to use for local caches. Default properties can be
     * overridden by setting the corresponding system properties.
     */
    private static final Map<String, Long> cacheProps = new HashMap<String, Long>();

    static {
        initialize();
    }

    private CacheFactory() {
    }

    /**
     * If a local property is found for the supplied name which specifies a
     * value for cache size, it is returned. Otherwise, the defaultSize argument
     * is returned.
     *
     * @param cacheName the name of the cache to look up a corresponding property for.
     * @return either the property value or the default value.
     */
    public static long getMaxCacheSize(String cacheName) {
        return getCacheProperty(cacheName, ".size", DEFAULT_MAX_CACHE_SIZE);
    }

    /**
     * Sets a local property which overrides the maximum cache size as
     * configured in coherence-cache-config.xml for the supplied cache name.
     *
     * @param cacheName the name of the cache to store a value for.
     * @param size      the maximum cache size.
     */
    public static void setMaxSizeProperty(String cacheName, long size) {
        cacheName = cacheName.replaceAll(" ", "");
        cacheProps.put("cache." + cacheName + ".size", size);
    }

    public static boolean hasMaxSizeFromProperty(String cacheName) {
        return hasCacheProperty(cacheName, ".size");
    }

    /**
     * If a local property is found for the supplied name which specifies a
     * value for cache entry lifetime, it is returned. Otherwise, the
     * defaultLifetime argument is returned.
     *
     * @param cacheName the name of the cache to look up a corresponding property for.
     * @return either the property value or the default value.
     */
    public static long getMaxCacheLifetime(String cacheName) {
        return getCacheProperty(cacheName, ".maxLifetime",
                DEFAULT_MAX_CACHE_LIFETIME);
    }

    /**
     * Sets a local property which overrides the maximum cache entry lifetime as
     * configured in coherence-cache-config.xml for the supplied cache name.
     *
     * @param cacheName the name of the cache to store a value for.
     * @param lifetime  the maximum cache entry lifetime.
     */
    public static void setMaxLifetimeProperty(String cacheName, long lifetime) {
        cacheName = cacheName.replaceAll(" ", "");
        cacheProps.put("cache." + cacheName + ".maxLifetime", lifetime);
    }

    public static boolean hasMaxLifetimeFromProperty(String cacheName) {
        return hasCacheProperty(cacheName, ".maxLifetime");
    }

    public static long getMinCacheSize(String cacheName) {
        return getCacheProperty(cacheName, ".min", 0);
    }

    private static long getCacheProperty(String cacheName, String suffix,
                                         long defaultValue) {
        // First check if user is overwriting default value using a system
        // property for the cache name
        String propName = "cache." + cacheName.replaceAll(" ", "") + suffix;
        // Check if there is a default size value for this cache
        Long defaultSize = cacheProps.get(propName);
        return defaultSize == null ? defaultValue : defaultSize;
    }

    private static boolean hasCacheProperty(String cacheName, String suffix) {
        return false;
    }

    /**
     * Returns an array of all caches in the system.
     *
     * @return an array of all caches in the system.
     */
    public static Cache[] getAllCaches() {
        List<Cache> values = new ArrayList<Cache>();
        for (Cache cache : caches.values()) {
            values.add(cache);
        }
        return values.toArray(new Cache[values.size()]);
    }

    /**
     * Returns the named cache, creating it as necessary.
     *
     * @param name the name of the cache to create.
     * @return the named cache, creating it as necessary.
     */
    public static synchronized <T extends Cache> T createCache(String name) {
        T cache = (T) caches.get(name);
        if (cache != null) {
            return cache;
        }

        cache = (T) cacheFactoryStrategy.createCache(name);

        return wrapCache(cache, name);
    }


    public static <T extends Cache> T createCache(String name, long lifeTime) {

        T cacheWrapper = CacheFactory.createCache(name);

        cacheWrapper.setMaxLifetime(lifeTime);//million second

        return cacheWrapper;
    }

    /**
     * Destroys the cache for the cache name specified.
     *
     * @param name the name of the cache to destroy.
     */
    public static void destroyCache(String name) {
        Cache cache = caches.remove(name);
        if (cache != null) {
            cacheFactoryStrategy.destroyCache(cache);
        }
    }

    /**
     * Returns an existing {@link java.util.concurrent.locks.Lock} on the
     * specified key or creates a new one if none was found. This operation is
     * thread safe. Successive calls with the same key may or may not return the
     * same {@link java.util.concurrent.locks.Lock}. However, different threads
     * asking for the same Lock at the same time will get the same Lock object.
     * <p/>
     * <p/>
     * The supplied cache may or may not be used depending whether the server is
     * running on cluster mode or not. When not running as part of a cluster
     * then the lock will be unrelated to the cache and will only be visible in
     * this JVM.
     *
     * @param key   the object that defines the visibility or scope of the lock.
     * @param cache the cache used for holding the lock.
     * @return an existing lock on the specified key or creates a new one if
     * none was found.
     */
    public static Lock getLock(Object key, Cache cache) {
        return cacheFactoryStrategy.getLock(key, cache);
    }

    private static <T extends Cache> T wrapCache(T cache, String name) {
        cache = (T) new CacheWrapper(cache);
        cache.setName(name);

        caches.put(name, cache);
        return cache;
    }

    public static Cache getCache(String name) {
        return caches.get(name);
    }

    /**
     * Returns a byte[] that uniquely identifies this member within the cluster
     * or <tt>null</tt> when not in a cluster.
     *
     * @return a byte[] that uniquely identifies this member within the cluster
     * or null when not in a cluster.
     */
    public static byte[] getClusterMemberID() {
        return cacheFactoryStrategy.getClusterMemberID();
    }

    public synchronized static void clearCaches() {
        for (String cacheName : caches.keySet()) {
            Cache cache = caches.get(cacheName);
            cache.clear();
        }
    }

    /**
     * Returns a byte[] that uniquely identifies this senior cluster member or
     * <tt>null</tt> when not in a cluster.
     *
     * @return a byte[] that uniquely identifies this senior cluster member or
     * null when not in a cluster.
     */
    public static byte[] getSeniorClusterMemberID() {
        return cacheFactoryStrategy.getSeniorClusterMemberID();
    }

    /**
     * Returns true if this member is the senior member in the cluster. If
     * clustering is not enabled, this method will also return true. This test
     * is useful for tasks that should only be run on a single member in a
     * cluster.
     *
     * @return true if this cluster member is the senior or if clustering is not
     * enabled.
     */
    public static boolean isSeniorClusterMember() {
        return cacheFactoryStrategy.isSeniorClusterMember();
    }

    /**
     * Returns basic information about the current members of the cluster or an
     * empty collection if not running in a cluster.
     *
     * collection if not running in a cluster.
     * <p/>
     * public static Collection<ClusterNodeInfo> getClusterNodesInfo() {
     * return cacheFactoryStrategy.getClusterNodesInfo(); }
     */

    public static synchronized void initialize() {
        try {
            cacheFactoryStrategy = new DefaultLocalCacheStrategy();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Cannot init cache factory for communicate module !");
        }

    }

    public static Map<String, Long> getCacheProps() {
        return cacheProps;
    }


}
