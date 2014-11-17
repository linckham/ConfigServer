package com.cmbc.configserver.core.heartbeat;

import com.cmbc.configserver.core.service.ConfigHeartBeatService;
import io.netty.channel.Channel;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.cmbc.configserver.core.dao.ConfigHeartBeatDao;
import com.cmbc.configserver.core.service.ConfigServerService;
import com.cmbc.configserver.core.subscriber.SubscriberService;
import com.cmbc.configserver.domain.ConfigHeartBeat;
import com.cmbc.configserver.remoting.common.RemotingHelper;
import com.cmbc.configserver.remoting.common.RemotingUtil;
import com.cmbc.configserver.utils.ConfigServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("heartbeatService")
public class HeartbeatService {
	private final Map<String/* clientId */, HeartbeatInfo> heartbeatInfoTable;
    @Autowired
    private ConfigHeartBeatService configHeartBeatService;
    @Autowired
    private SubscriberService subscriberService;
    @Autowired
    private ConfigServerService configServerService;

	private ScheduledExecutorService scheduledExecutorService = Executors
			.newSingleThreadScheduledExecutor(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r, "heartbeat_timeout_scan_thread");
				}
			});

    public void setSubscriberService(SubscriberService subscriberService) {
		this.subscriberService = subscriberService;
	}

    public void setConfigServerService(ConfigServerService configServerService) {
        this.configServerService = configServerService;
    }

    public void setConfigHeartBeatService(ConfigHeartBeatService configHeartBeatService) {
        this.configHeartBeatService = configHeartBeatService;
    }
    
	public void start() {
        //scan timeout channel
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                	HeartbeatService.this.scanTimeoutChannel();
                	HeartbeatService.this.scanDBTimeoutClient();
                }
                catch (Exception e) {
                	ConfigServerLogger.warn("timeout scan error", e);
                }
            }
        }, 1000 * 10, 1000 * 10, TimeUnit.MILLISECONDS);
    }
	
	public void shutdown(){
		this.scheduledExecutorService.shutdown();
	}
	
	public HeartbeatService(){
		this.heartbeatInfoTable = new ConcurrentHashMap<String, HeartbeatInfo>(256);
	}
	
	public void channelCreated(Channel channel){
		String clientId = RemotingHelper.getChannelId(channel);
		ConfigHeartBeat configHeartBeat = null;
		try {
			configHeartBeat = configHeartBeatService.get(clientId);
		} catch (Exception e) {
			ConfigServerLogger.error("get client heartbeat error", e);
		}
		
		if(configHeartBeat !=null){
			this.clearChannel(channel);
		}else{
			if(heartbeatInfoTable.get(clientId) != null){
				this.clearChannel(channel);
			}else{
				HeartbeatInfo heartbeatInfo = new HeartbeatInfo(clientId,channel,System.currentTimeMillis());
				heartbeatInfoTable.put(clientId, heartbeatInfo);
				
				//save db
				configHeartBeat = new ConfigHeartBeat(heartbeatInfo.getClientId(),heartbeatInfo.getLastUpdateMillis());
				try {
					heartbeatInfo.setLastDBSyncMillis(configHeartBeat.getLastModifiedTime());
                    configHeartBeatService.save(configHeartBeat);
				} catch (Exception e) {
					ConfigServerLogger.error("save client heartbeat error", e);
				}
			}
			
		}
	}
	
	public void updateHeartbeat(Channel channel){
		String clientId = RemotingHelper.getChannelId(channel);
		HeartbeatInfo heartbeatInfo = heartbeatInfoTable.get(clientId);
		if(heartbeatInfo == null){
			ConfigServerLogger.warn("heartbeatInfo is null in updateHeartbeat!");
		}else{
			heartbeatInfo.setLastUpdateMillis(System.currentTimeMillis());
			if(heartbeatInfo.getLastDBSyncMillis() <= 0){
				// save heartbeat to db error or in saving to db in channelCreated.
				ConfigServerLogger.warn("heartbeatInfo lastDBSyncMillis <= 0");
				try {
					ConfigHeartBeat configHeartBeat = configHeartBeatService.get(clientId);
					if(configHeartBeat == null){
						configHeartBeat = new ConfigHeartBeat(heartbeatInfo.getClientId(),heartbeatInfo.getLastUpdateMillis());
						heartbeatInfo.setLastDBSyncMillis(configHeartBeat.getLastModifiedTime());
                        configHeartBeatService.save(configHeartBeat);
					}
				} catch (Exception e) {
					ConfigServerLogger.error("save client heartbeat error", e);
				}
			}else{
				if(heartbeatInfo.getLastUpdateMillis() - heartbeatInfo.getLastDBSyncMillis() >= HeartbeatInfo.SYNC_DB_INTERVAL){
					try {
						ConfigHeartBeat configHeartBeat = new ConfigHeartBeat(heartbeatInfo.getClientId(),heartbeatInfo.getLastUpdateMillis());
                        configHeartBeatService.update(configHeartBeat);
						heartbeatInfo.setLastDBSyncMillis(configHeartBeat.getLastModifiedTime());
					} catch (Exception e) {
						ConfigServerLogger.error("update client heartbeat error", e);
					}
				}
			}
		}
	}
	
	public void scanTimeoutChannel(){
		Iterator<Entry<String, HeartbeatInfo>> i = heartbeatInfoTable.entrySet().iterator();
		while(i.hasNext()){
			HeartbeatInfo heartbeatInfo = i.next().getValue();
			if(System.currentTimeMillis() - heartbeatInfo.getLastUpdateMillis() > HeartbeatInfo.TIMEOUT){
				i.remove();
				this.clearChannel(heartbeatInfo.getChannel());
			}
		}
	}
	
	/**
	 * when config server is down,there will left the client configuration of this server,
	 * so use this method to scan the left configuration.
	 */
	public void scanDBTimeoutClient(){
		try {
			List<ConfigHeartBeat> configHeartbeats = configHeartBeatService.getTimeout();
			if(configHeartbeats != null){
				for(ConfigHeartBeat configHeartBeat : configHeartbeats){
					//delete configuration
					configServerService.deleteConfigurationByClientId(configHeartBeat.getClientId());
                    configHeartBeatService.delete(configHeartBeat.getClientId());
				}
			}
		} catch (Exception e) {
			ConfigServerLogger.error("scanDBTimeoutClient error", e);
		}
	}
	
	public void clearChannel(Channel channel){
		ConfigServerLogger.info("server clear channel start:" + channel);
		
		String clientId = RemotingHelper.getChannelId(channel);
		heartbeatInfoTable.remove(clientId);
		RemotingUtil.closeChannel(channel);
		//delete subscribe
		subscriberService.clearChannel(channel);
		
		try {
			//delete configuration
            configServerService.deleteConfigurationByClientId(clientId);
            configHeartBeatService.delete(clientId);
		} catch (Exception e) {
			ConfigServerLogger.error("delete client heartbeat error", e);
		}
		
		ConfigServerLogger.info("server clear channel end:" + channel);
	}
}
