package com.cmbc.configserver.core.heartbeat;

import com.cmbc.configserver.core.MockUtils;
import com.cmbc.configserver.core.dao.ConfigHeartBeatDao;
import com.cmbc.configserver.core.subscriber.SubscriberService;
import com.cmbc.configserver.domain.ConfigHeartBeat;
import com.cmbc.configserver.remoting.common.RemotingHelper;
import io.netty.channel.Channel;

import org.easymock.EasyMock;
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
    private String clientId;
    private ConfigHeartBeatDao configHeartBeatDao;
    @Before
    public void setUp() throws Exception{
        heartbeatService = new HeartbeatService();
        clientId = RemotingHelper.getChannelId(MockUtils.mockChannel());
        heartbeatService.setConfigServerService(MockUtils.mockConfigServerService());
        heartbeatService.setSubscriberService(new SubscriberService());
        configHeartBeatDao = MockUtils.mockConfigHeartBeatDao();
        heartbeatService.setHeartBeatDao(configHeartBeatDao);
        heartbeatService.start();
    }

    @Test
    public void testUpdateHeartbeat() throws  Exception{
        Channel channel = MockUtils.mockChannel();
        heartbeatService.updateHeartbeat(channel);
        EasyMock.expect(configHeartBeatDao.get(RemotingHelper.getChannelId(channel))).andReturn(null).once();
        EasyMock.replay(configHeartBeatDao);

        heartbeatService.channelCreated(channel);

        try {
            TimeUnit.MILLISECONDS.sleep(30*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
