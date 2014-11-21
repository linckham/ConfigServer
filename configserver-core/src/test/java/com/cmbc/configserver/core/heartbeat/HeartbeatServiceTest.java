package com.cmbc.configserver.core.heartbeat;

import com.cmbc.configserver.core.MockUtils;
import com.cmbc.configserver.core.subscriber.SubscriberService;
import io.netty.channel.Channel;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/11/13
 * @Time 15:44
 */
public class HeartbeatServiceTest {
    private HeartbeatService heartbeatService;

    @Before
    public void setUp() throws Exception{
        heartbeatService = new HeartbeatService();
        heartbeatService.setConfigServerService(MockUtils.mockConfigServerService());
        heartbeatService.setSubscriberService(new SubscriberService());
    }

    @Test
    public void testUpdateHeartbeat() throws  Exception{
        Channel channel = MockUtils.mockChannel();
        heartbeatService.updateHeartbeat(channel);

        heartbeatService.channelCreated(channel);

        try {
            TimeUnit.MILLISECONDS.sleep(30*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
