package com.cmbc.configserver.remoting.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cmbc.configserver.remoting.ChannelEventListener;
import com.cmbc.configserver.remoting.ConnectionStateListener;
import com.cmbc.configserver.remoting.RPCHook;
import com.cmbc.configserver.remoting.common.Pair;
import com.cmbc.configserver.remoting.common.RemotingHelper;
import com.cmbc.configserver.remoting.common.RemotingUtil;
import com.cmbc.configserver.remoting.common.RequestProcessor;
import com.cmbc.configserver.remoting.exception.RemotingConnectException;
import com.cmbc.configserver.remoting.exception.RemotingSendRequestException;
import com.cmbc.configserver.remoting.exception.RemotingTimeoutException;
import com.cmbc.configserver.remoting.protocol.RemotingCommand;

public class NettyRemotingClient extends NettyRemotingAbstract {
	private static final Logger log = LoggerFactory.getLogger(NettyRemotingClient.class);

	private static final long LockTimeoutMillis = 3000;

	private final NettyClientConfig nettyClientConfig;
	private final Bootstrap bootstrap = new Bootstrap();
	private final EventLoopGroup eventLoopGroupWorker;
	private DefaultEventExecutorGroup defaultEventExecutorGroup;

	private final Lock lockChannel = new ReentrantLock();
	private volatile ChannelWrapper clientChannel;

	private final Timer timer = new Timer("ClientHouseKeepingService", true);
	private final Timer reconnectTimer = new Timer("ClientReconnectService", true);

	private final AtomicReference<List<String>> namesrvAddrList = new AtomicReference<List<String>>();
	private final AtomicReference<String> namesrvAddrChoosed = new AtomicReference<String>();
	private final AtomicInteger namesrvIndex = new AtomicInteger(initValueIndex());
	private final Lock lockNamesrvChannel = new ReentrantLock();

	private final ExecutorService publicExecutor;

	private final ChannelEventListener channelEventListener;

	private RPCHook rpcHook;
	
	private ConnectionStateListener connectionStateListener;

	class ChannelWrapper {
		private final ChannelFuture channelFuture;

		public ChannelWrapper(ChannelFuture channelFuture) {
			this.channelFuture = channelFuture;
		}

		public boolean isOK() {
			return (this.channelFuture.channel() != null && this.channelFuture
					.channel().isActive());
		}

		private Channel getChannel() {
			return this.channelFuture.channel();
		}

		public ChannelFuture getChannelFuture() {
			return channelFuture;
		}
	}

	class NettyClientHandler extends
			SimpleChannelInboundHandler<RemotingCommand> {

		@Override
		protected void channelRead0(ChannelHandlerContext ctx,
				RemotingCommand msg) throws Exception {
			processMessageReceived(ctx, msg);

		}
	}

	class NettyConnetManageHandler extends ChannelDuplexHandler {
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			super.channelActive(ctx);
			if (NettyRemotingClient.this.channelEventListener != null) {
				NettyRemotingClient.this.putNettyEvent(new NettyEvent(
						NettyEventType.ACTIVE, null, ctx.channel()));
			}
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			super.channelInactive(ctx);
			if (NettyRemotingClient.this.channelEventListener != null) {
				NettyRemotingClient.this.putNettyEvent(new NettyEvent(
						NettyEventType.CLOSE, null, ctx.channel()));
			}
		}


		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
				throws Exception {
			final String remoteAddress = RemotingHelper
					.parseChannelRemoteAddr(ctx.channel());
			log.warn("NETTY CLIENT PIPELINE: exceptionCaught {}", remoteAddress);
			log.warn("NETTY CLIENT PIPELINE: exceptionCaught exception.", cause);
			if (NettyRemotingClient.this.channelEventListener != null) {
				NettyRemotingClient.this.putNettyEvent(new NettyEvent(
						NettyEventType.EXCEPTION, remoteAddress.toString(), ctx
								.channel()));
			}
		}

		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
				throws Exception {
			if (evt instanceof IdleStateEvent) {
				IdleStateEvent evnet = (IdleStateEvent) evt;
				if (evnet.state().equals(IdleState.READER_IDLE)) {
					final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
					
					if (NettyRemotingClient.this.channelEventListener != null) {
						NettyRemotingClient.this.putNettyEvent(new NettyEvent(
								NettyEventType.IDLE, remoteAddress.toString(),
								ctx.channel()));
					}
				}
			}
			
			ctx.fireUserEventTriggered(evt);
		}
	}

	private static int initValueIndex() {
		Random r = new Random();

		return Math.abs(r.nextInt() % 999) % 999;
	}

	public NettyRemotingClient(final NettyClientConfig nettyClientConfig) {
		this(nettyClientConfig, null);
	}

	public NettyRemotingClient(final NettyClientConfig nettyClientConfig,
			final ChannelEventListener channelEventListener) {
		super(nettyClientConfig.getClientOnewaySemaphoreValue(),
				nettyClientConfig.getClientAsyncSemaphoreValue());
		this.nettyClientConfig = nettyClientConfig;
		this.channelEventListener = channelEventListener;
		
		int publicThreadNums = nettyClientConfig.getClientCallbackExecutorThreads();
		if (publicThreadNums <= 0) {
			publicThreadNums = 4;
		}

		this.publicExecutor = Executors.newFixedThreadPool(publicThreadNums,
				new ThreadFactory() {
					private AtomicInteger threadIndex = new AtomicInteger(0);

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "NettyClientPublicExecutor_"
								+ this.threadIndex.incrementAndGet());
					}
				});

		this.eventLoopGroupWorker = new NioEventLoopGroup(1,
				new ThreadFactory() {
					private AtomicInteger threadIndex = new AtomicInteger(0);

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, String.format(
								"NettyClientSelector_%d",
								this.threadIndex.incrementAndGet()));
					}
				});
	}

	public void start() throws InterruptedException {
		this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(//
				nettyClientConfig.getClientWorkerThreads(), //
				new ThreadFactory() {

					private AtomicInteger threadIndex = new AtomicInteger(0);

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "NettyClientWorkerThread_"
								+ this.threadIndex.incrementAndGet());
					}
				});

		this.bootstrap.group(this.eventLoopGroupWorker)
				.channel(NioSocketChannel.class)//
				.option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.SO_SNDBUF,NettySystemConfig.SocketSndbufSize)
				.option(ChannelOption.SO_RCVBUF,NettySystemConfig.SocketRcvbufSize)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(
								defaultEventExecutorGroup,
								new NettyEncoder(),
								new NettyDecoder(), 
								new IdleStateHandler(nettyClientConfig.getClientChannelMaxIdleTimeSeconds(),0, 0),
								new NettyConnetManageHandler(),
								new NettyClientHandler());
					}
				});

        // schedule the timeout of async call per second
		this.timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				try {
					NettyRemotingClient.this.scanResponseTable();
				} catch (Exception e) {
					log.error("scanResponseTable exception", e);
				}
			}
		}, 1000 * 3, 1000);
		
		//connect to server
		Channel channel = this.getAndCreateNameserverChannel();
		if(channel == null){
			log.error("can't connect to server,please check server config");
		}
		this.reconnectTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					NettyRemotingClient.this.getAndCreateNameserverChannel();
				} catch (InterruptedException e) {
					log.error("reconnect to server failed", e);
				}
			}
		}, 1000 * 10, 1000 * 10);

		if (this.channelEventListener != null) {
			this.nettyEventExecuter.start();
		}
	}

	public void shutdown() {
		try {
			this.timer.cancel();

			if (this.clientChannel != null) {
				this.closeChannel(clientChannel.getChannel());
			}

			this.eventLoopGroupWorker.shutdownGracefully();

			if (this.nettyEventExecuter != null) {
				this.nettyEventExecuter.shutdown();
			}

			if (this.defaultEventExecutorGroup != null) {
				this.defaultEventExecutorGroup.shutdownGracefully();
			}
		} catch (Exception e) {
			log.error("NettyRemotingClient shutdown exception, ", e);
		}

		if (this.publicExecutor != null) {
			try {
				this.publicExecutor.shutdown();
			} catch (Exception e) {
				log.error("NettyRemotingServer shutdown exception, ", e);
			}
		}
	}

	

	public Channel getAndCreateNameserverChannel() throws InterruptedException {
		if (clientChannel != null && clientChannel.isOK()) {
			return clientChannel.getChannel();
		}

		final List<String> addrList = this.namesrvAddrList.get();
		// add lock and try to create new connection
		if (this.lockNamesrvChannel.tryLock(LockTimeoutMillis,
				TimeUnit.MILLISECONDS)) {
			try {
				String addr = this.namesrvAddrChoosed.get();
				if (addr != null) {
					if (clientChannel != null && clientChannel.isOK()) {
						return clientChannel.getChannel();
					}
				}

				if (addrList != null && !addrList.isEmpty()) {
					for (int i = 0; i < addrList.size(); i++) {
						int index = this.namesrvIndex.incrementAndGet();
						index = Math.abs(index);
						index = index % addrList.size();
						String newAddr = addrList.get(index);

						this.namesrvAddrChoosed.set(newAddr);
						Channel channelNew = this.createChannel();
						if (channelNew != null){
							
							//reconnected event,not exactly
							if(this.connectionStateListener != null){
								this.publicExecutor.execute(new Runnable(){
									@Override
									public void run() {
										connectionStateListener.reconnected();
									}
								});
							}
							return channelNew;
						}
					}
				}
			} catch (Exception e) {
				log.error("getAndCreateNameserverChannel: create name server channel exception",e);
			} finally {
				this.lockNamesrvChannel.unlock();
			}
		} else {
			log.warn("getAndCreateNameserverChannel: try to lock name server, but timeout, {}ms",LockTimeoutMillis);
		}

		return null;
	}

	private Channel createChannel() throws InterruptedException {
		if (clientChannel != null && clientChannel.isOK()) {
			return clientChannel.getChannel();
		}

		if (this.lockChannel.tryLock(LockTimeoutMillis, TimeUnit.MILLISECONDS)) {
			try {
				boolean createNewConnection = false;
				
				if (clientChannel != null) {
					if (clientChannel.isOK()) {
						return clientChannel.getChannel();
					}
					// the connection is doing
					else if (!clientChannel.getChannelFuture().isDone()) {
						createNewConnection = false;
					}
					// the connection is not successful
					else {
						createNewConnection = true;
					}
				}
				else {
					createNewConnection = true;
				}

				if (createNewConnection) {
					ChannelFuture channelFuture = this.bootstrap.connect(RemotingHelper.string2SocketAddress(namesrvAddrChoosed.get()));
					log.info("createChannel: begin to connect remote host[{}] asynchronously",namesrvAddrChoosed.get());
					clientChannel = new ChannelWrapper(channelFuture);
				}
			} catch (Exception e) {
				log.error("createChannel: create channel exception", e);
			} finally {
				this.lockChannel.unlock();
			}
		} else {
			log.warn(
					"createChannel: try to lock channel table, but timeout, {}ms",
					LockTimeoutMillis);
		}

		if (clientChannel != null) {
			ChannelFuture channelFuture = clientChannel.getChannelFuture();
			if (channelFuture.awaitUninterruptibly(this.nettyClientConfig.getConnectTimeoutMillis())) {
				if (clientChannel.isOK()) {
					log.info(
							"createChannel: connect remote host[{}] success, {}",
							namesrvAddrChoosed.get(), channelFuture.toString());
					return clientChannel.getChannel();
				} else {
					log.warn("createChannel: connect remote host[" + namesrvAddrChoosed.get()
							+ "] failed, " + channelFuture.toString(),
							channelFuture.cause());
				}
			} else {
				log.warn(
						"createChannel: connect remote host[{}] timeout {}ms, {}",
						namesrvAddrChoosed.get(), this.nettyClientConfig.getConnectTimeoutMillis(),
						channelFuture.toString());
			}
		}

		return null;
	}

	public void closeChannel(final Channel channel) {
		if (null == channel)
			return;

		try {
			if (this.lockChannel.tryLock(LockTimeoutMillis,
					TimeUnit.MILLISECONDS)) {
				try {
					RemotingUtil.closeChannel(channel);
				} catch (Exception e) {
					log.error("closeChannel: close the channel exception", e);
				} finally {
					this.lockChannel.unlock();
				}
			} else {
				log.warn("closeChannel: try to lock channel table, but timeout, {}ms",LockTimeoutMillis);
			}
		} catch (InterruptedException e) {
			log.error("closeChannel exception", e);
		}
	}

	public void registerProcessor(int requestCode, RequestProcessor processor,
			ExecutorService executor) {
		ExecutorService executorThis = executor;
		if (null == executor) {
			executorThis = this.publicExecutor;
		}

		Pair<RequestProcessor, ExecutorService> pair = new Pair<RequestProcessor, ExecutorService>(
				processor, executorThis);
		this.processorTable.put(requestCode, pair);
	}

	public RemotingCommand invokeSync(final RemotingCommand request, long timeoutMillis)
			throws InterruptedException, RemotingConnectException,RemotingSendRequestException, RemotingTimeoutException {
		final Channel channel = this.getAndCreateNameserverChannel();
		if (channel != null && channel.isActive()) {
			try {
				if (this.rpcHook != null) {
					this.rpcHook.doBeforeRequest(namesrvAddrChoosed.get(), request);
				}
				RemotingCommand response = this.invokeSyncImpl(channel,
						request, timeoutMillis);
				if (this.rpcHook != null) {
					this.rpcHook.doAfterResponse(request, response);
				}
				return response;
			} catch (RemotingSendRequestException e) {
				log.warn("invokeSync: send request exception, so close the channel[{}]",namesrvAddrChoosed.get());
				this.closeChannel(channel);
				throw e;
			} catch (RemotingTimeoutException e) {
				log.warn("invokeSync: wait response timeout exception, the channel[{}]",namesrvAddrChoosed.get());
				this.closeChannel(channel);
				throw e;
			}
		} else {
			this.closeChannel(channel);
			throw new RemotingConnectException(namesrvAddrChoosed.get());
		}
	}

	
	
	@Override
	public ExecutorService getCallbackExecutor() {
		return this.publicExecutor;
	}

	@Override
	public ChannelEventListener getChannelEventListener() {
		return channelEventListener;
	}

	public List<String> getNamesrvAddrList() {
		return namesrvAddrList.get();
	}
	
    public void updateNameServerAddressList(List<String> addrs) {
        List<String> old = this.namesrvAddrList.get();
        boolean update = false;

        if (!addrs.isEmpty()) {
            if (null == old) {
                update = true;
            }
            else if (addrs.size() != old.size()) {
                update = true;
            }
            else {
                for (int i = 0; i < addrs.size() && !update; i++) {
                    if (!old.contains(addrs.get(i))) {
                        update = true;
                    }
                }
            }

            if (update) {
                Collections.shuffle(addrs);
                this.namesrvAddrList.set(addrs);
            }
        }
    }

	public RPCHook getRpcHook() {
		return rpcHook;
	}

	public void registerRPCHook(RPCHook rpcHook) {
		this.rpcHook = rpcHook;
	}

	@Override
	public RPCHook getRPCHook() {
		return this.rpcHook;
	}
	
	public void setConnectionStateListener(
			ConnectionStateListener connectionStateListener) {
		this.connectionStateListener = connectionStateListener;
	}
}
