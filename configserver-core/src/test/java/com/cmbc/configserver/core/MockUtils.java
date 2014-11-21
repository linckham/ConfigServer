package com.cmbc.configserver.core;

import com.cmbc.configserver.core.dao.ConfigHeartBeatDao;
import com.cmbc.configserver.core.service.ConfigServerService;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.remoting.common.RemotingHelper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.easymock.EasyMock;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/13
 * @Time 15:47
 */
public class MockUtils {

    public static Configuration mockConfiguration() {
        Configuration configuration = new Configuration();
        configuration.setCell("test-dubbo");
        configuration.setResource(MockUtils.class.getName());
        configuration.setType("providers");
        configuration.setContent("dubbo://127.0.0.1:20881?interface=xxxx");
        return configuration;
    }

    public static ConfigHeartBeatDao mockConfigHeartBeatDao() {
        return EasyMock.createMock(ConfigHeartBeatDao.class);
    }

    public static ConfigServerService mockConfigServerService() throws Exception {
        Channel channel = mockChannel();
        Configuration configuration = mockConfiguration();
        //mock config server service
        ConfigServerService configServerService = EasyMock.createMock(ConfigServerService.class);
        EasyMock.expect(configServerService.publish(configuration)).andReturn(true).anyTimes();
        EasyMock.expect(configServerService.unPublish(configuration)).andReturn(true).anyTimes();
        EasyMock.expect(configServerService.subscribe(configuration, channel)).andReturn(true).anyTimes();
        EasyMock.expect(configServerService.unSubscribe(configuration, channel)).andReturn(true);
        EasyMock.expect(configServerService.deleteConfigurationByClientId(RemotingHelper.getChannelId(channel))).andReturn(true);
        //replay
        EasyMock.replay(configServerService);
        return configServerService;
    }

    public static Channel mockChannel() {
        final Channel channel = EasyMock.createMock(Channel.class);
        EasyMock.expect(channel.localAddress()).andReturn(new InetSocketAddress("127.0.0.1", 19999)).anyTimes();
        EasyMock.expect(channel.remoteAddress()).andReturn(new InetSocketAddress("127.0.0.1", 20034)).anyTimes();
        EasyMock.expect(channel.close()).andReturn(new ChannelFuture() {
            @Override
            public Channel channel() {
                return channel;
            }

            @Override
            public ChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
                return null;
            }

            @Override
            public ChannelFuture addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
                return null;
            }

            @Override
            public ChannelFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
                return null;
            }

            @Override
            public ChannelFuture removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
                return null;
            }

            @Override
            public ChannelFuture sync() throws InterruptedException {
                return null;
            }

            @Override
            public ChannelFuture syncUninterruptibly() {
                return null;
            }

            @Override
            public ChannelFuture await() throws InterruptedException {
                return null;
            }

            @Override
            public ChannelFuture awaitUninterruptibly() {
                return null;
            }

            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public boolean isCancellable() {
                return false;
            }

            @Override
            public Throwable cause() {
                return null;
            }

            @Override
            public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
                return false;
            }

            @Override
            public boolean await(long timeoutMillis) throws InterruptedException {
                return false;
            }

            @Override
            public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
                return false;
            }

            @Override
            public boolean awaitUninterruptibly(long timeoutMillis) {
                return false;
            }

            @Override
            public Void getNow() {
                return null;
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public Void get() throws InterruptedException, ExecutionException {
                return null;
            }

            @Override
            public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        });
        EasyMock.replay(channel);
        return channel;
    }
}
