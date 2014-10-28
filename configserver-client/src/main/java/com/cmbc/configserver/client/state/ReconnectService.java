package com.cmbc.configserver.client.state;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ReconnectService extends TimerTask{
	private static final Logger logger = LoggerFactory.getLogger(ReconnectService.class);
	
	@Override
	public void run() {
		try {
			//TODO
			
			
			
			Thread.sleep( 30* 1000);
		} catch (InterruptedException e) {
			logger.info(e.toString());
		}
		
		
		
		
	}
	
	/*
	    //TODO use separate reconnct thread
//		reconnectExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryImpl("client-reconnect-thread"));
//		reconnectExecutor.execute(new ReconnectService(this));
		
		if(currentState == null){
			clientImpl.getCurrentState().set(ConnectionState.CONNECTED);
		}else{
			clientImpl.getCurrentState().set(ConnectionState.RECONNECTED);
			if(clientImpl.getStateListener() != null){
				clientImpl.getStateListener().reconnected();
			}
		}
	 */
}
