package com.cmbc.configserver.core.heartbeat;

import io.netty.channel.Channel;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.cmbc.configserver.core.dao.ConfigHeartBeatDao;
import com.cmbc.configserver.domain.ConfigHeartBeat;
import com.cmbc.configserver.remoting.common.RemotingHelper;
import com.cmbc.configserver.remoting.common.RemotingUtil;
import com.cmbc.configserver.utils.ConfigServerLogger;

public class HeartbeatService {
	private final Map<String/* clientId */, HeartbeatInfo> heartbeatInfoTable;
	private ConfigHeartBeatDao heartbeatDao;
	private ScheduledExecutorService scheduledExecutorService = Executors
			.newSingleThreadScheduledExecutor(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r, "heartbeat_timeout_scan_thread");
				}
			});
	
	public void start() {
        //scan timeout channel
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                	HeartbeatService.this.scanTimeoutChannel();
                }
                catch (Exception e) {
                	ConfigServerLogger.warn("timeout scan error", e);
                }
            }
        }, 1000 * 10, 1000 * 10, TimeUnit.MILLISECONDS);
    }
	
	public HeartbeatService(){
		this.heartbeatInfoTable = new ConcurrentHashMap<String, HeartbeatInfo>(256);
	}
	
	public void channelCreated(Channel channel){
		String clientId = RemotingHelper.parseChannelRemoteAddr(channel);
		ConfigHeartBeat configHeartBeat = null;
		try {
			configHeartBeat = heartbeatDao.get(clientId);
		} catch (Exception e) {
			ConfigServerLogger.error("get client heartbeat error", e);
		}
		
		if(configHeartBeat != null){
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
					heartbeatDao.save(configHeartBeat);
					heartbeatInfo.setLastDBSyncMillis(configHeartBeat.getLastModifiedTime());
				} catch (Exception e) {
					ConfigServerLogger.error("save client heartbeat error", e);
				}
			}
			
		}
	}
	
	public void updateHeartbeat(Channel channel){
		String clientId = RemotingHelper.parseChannelRemoteAddr(channel);
		HeartbeatInfo heartbeatInfo = heartbeatInfoTable.get(clientId);
		if(heartbeatInfo == null){
			ConfigServerLogger.error("heartbeatInfo is null in updateHeartbeat!");
			this.clearChannel(channel);
		}else{
			heartbeatInfo.setLastUpdateMillis(System.currentTimeMillis());
			if(heartbeatInfo.getLastDBSyncMillis() <= 0){
				try {
					ConfigHeartBeat configHeartBeat = new ConfigHeartBeat(heartbeatInfo.getClientId(),heartbeatInfo.getLastUpdateMillis());
					heartbeatDao.save(configHeartBeat);
					heartbeatInfo.setLastDBSyncMillis(configHeartBeat.getLastModifiedTime());
				} catch (Exception e) {
					ConfigServerLogger.error("save client heartbeat error", e);
				}
			}else{
				if(heartbeatInfo.getLastUpdateMillis() - heartbeatInfo.getLastDBSyncMillis() >= HeartbeatInfo.SYNC_DB_INTERVAL){
					try {
						ConfigHeartBeat configHeartBeat = new ConfigHeartBeat(heartbeatInfo.getClientId(),heartbeatInfo.getLastUpdateMillis());
						heartbeatDao.update(configHeartBeat);
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
	
	public void clearChannel(Channel channel){
		String clientId = RemotingHelper.parseChannelRemoteAddr(channel);
		heartbeatInfoTable.remove(clientId);
		try {
			heartbeatDao.delete(clientId);
		} catch (Exception e) {
			ConfigServerLogger.error("save client heartbeat error", e);
		}
		
		RemotingUtil.closeChannel(channel);
		//TODO delete subscribe
		
		//TODO delete configuration
	}
}
