package com.cmbc.configserver.remoting.netty;

import com.cmbc.configserver.common.ThreadFactoryImpl;
import com.cmbc.configserver.remoting.ChannelEventListener;
import com.cmbc.configserver.remoting.ConnectionStateListener;
import com.cmbc.configserver.remoting.RPCHook;
import com.cmbc.configserver.remoting.RemotingClient;
import com.cmbc.configserver.remoting.common.Pair;
import com.cmbc.configserver.remoting.common.RemotingHelper;
import com.cmbc.configserver.remoting.common.RemotingUtil;
import com.cmbc.configserver.remoting.common.RequestProcessor;
import com.cmbc.configserver.remoting.exception.RemotingConnectException;
import com.cmbc.configserver.remoting.exception.RemotingSendRequestException;
import com.cmbc.configserver.remoting.exception.RemotingTimeoutException;
import com.cmbc.configserver.remoting.protocol.RemotingCommand;
import com.cmbc.configserver.utils.Constants;
import com.cmbc.configserver.utils.StatisticsLog;
import com.cmbc.configserver.utils.ThreadUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NettyRemotingClient extends NettyRemotingAbstract implements RemotingClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(NettyRemotingClient.class);

	private final NettyClientConfig nettyClientConfig;
	private final Bootstrap bootstrap = new Bootstrap();
	private final EventLoopGroup eventLoopGroupWorker;
	private DefaultEventExecutorGroup defaultEventExecutorGroup;

	private final Lock lockChannel = new ReentrantLock();
	private volatile ChannelWrapper clientChannel;

	private final Timer timer = new Timer("ClientHouseKeepingService", true);
	private final Timer reconnectTimer = new Timer("ClientReconnectService", true);

	private final AtomicReference<List<String>> serverAddressList = new AtomicReference<List<String>>();
	private final AtomicReference<String> serverAddressSelected = new AtomicReference<String>();
	private final AtomicInteger serverAddressIndex = new AtomicInteger(initValueIndex());
	private final Lock lockServerChannel = new ReentrantLock();

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
			return (this.channelFuture.channel() != null && this.channelFuture.channel().isActive());
		}

		private Channel getChannel() {
			return this.channelFuture.channel();
		}

		public ChannelFuture getChannelFuture() {
			return channelFuture;
		}
	}

	class NettyClientHandler extends SimpleChannelInboundHandler<RemotingCommand> {
		@Override
		protected void channelRead0(ChannelHandlerContext ctx,RemotingCommand msg) throws Exception {
			processMessageReceived(ctx, msg);
		}
	}

	class NettyConnectManageHandler extends ChannelDuplexHandler {
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			super.channelActive(ctx);
			if (NettyRemotingClient.this.channelEventListener != null) {
				NettyRemotingClient.this.putNettyEvent(new NettyEvent(NettyEventType.ACTIVE, null, ctx.channel()));
			}
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			super.channelInactive(ctx);
			if (NettyRemotingClient.this.channelEventListener != null) {
				NettyRemotingClient.this.putNettyEvent(new NettyEvent(NettyEventType.CLOSE, null, ctx.channel()));
			}
		}


		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
				throws Exception {
			final String remoteAddress = RemotingHelper.parseChannelRemoteAddress(ctx.channel());
			LOGGER.warn("NettyConnectManageHandler exceptionCaught remote address {} exception {}.", remoteAddress, cause);
			if (NettyRemotingClient.this.channelEventListener != null) {
				NettyRemotingClient.this.putNettyEvent(new NettyEvent(NettyEventType.EXCEPTION, remoteAddress, ctx.channel()));
			}
		}

		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
				throws Exception {
			if (evt instanceof IdleStateEvent) {
				IdleStateEvent event = (IdleStateEvent) evt;
				if (event.state().equals(IdleState.READER_IDLE)) {
					final String remoteAddress = RemotingHelper.parseChannelRemoteAddress(ctx.channel());
					if (NettyRemotingClient.this.channelEventListener != null) {
						NettyRemotingClient.this.putNettyEvent(new NettyEvent(NettyEventType.IDLE, remoteAddress,ctx.channel()));
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

	public NettyRemotingClient(final NettyClientConfig nettyClientConfig,final ChannelEventListener channelEventListener) {
        super(nettyClientConfig.getClientOnewaySemaphoreValue(), nettyClientConfig.getClientAsyncSemaphoreValue());
        this.nettyClientConfig = nettyClientConfig;
        this.channelEventListener = channelEventListener;

        int publicThreadNumbers = nettyClientConfig.getClientCallbackExecutorThreads();
        if (publicThreadNumbers <= 0) {
            publicThreadNumbers = 4;
        }

        this.publicExecutor = new ThreadPoolExecutor(publicThreadNumbers,publicThreadNumbers, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(Constants.DEFAULT_MAX_QUEUE_ITEM), new ThreadFactoryImpl("NettyClientPublicExecutor-"));
        this.eventLoopGroupWorker = new NioEventLoopGroup(1,new ThreadFactoryImpl("NettyClientSelector-"));
    }

	public void start()  {
        StatisticsLog.registerExecutor("public-pool",(ThreadPoolExecutor)publicExecutor);
		this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
				nettyClientConfig.getClientWorkerThreads(),new ThreadFactoryImpl("NettyClientWorkerThread-"));

		this.bootstrap.group(this.eventLoopGroupWorker)
				.channel(NioSocketChannel.class)
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
								new NettyConnectManageHandler(),
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
					LOGGER.error("scanResponseTable exception", e);
				}
			}
		}, 1000 * 3, 1000);
		
		//connect to server
		Channel channel = this.getAndCreateServerChannel();
		if(channel == null){
			LOGGER.warn("can't connect to server,please check server address list {}",this.serverAddressList.get());
		}

		this.reconnectTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					NettyRemotingClient.this.getAndCreateServerChannel();
				} catch (Throwable e) {
					LOGGER.error("reconnect to config server failed", e);
				}
			}
		}, 1000 * 10, 1000 * 10);

		if (this.channelEventListener != null) {
			this.nettyEventExecutor.start();
		}
	}

	public void shutdown() {
		try {
			this.timer.cancel();

			if (this.clientChannel != null) {
				this.closeChannel(clientChannel.getChannel());
			}

			this.eventLoopGroupWorker.shutdownGracefully();

            this.nettyEventExecutor.shutdown();

			if (this.defaultEventExecutorGroup != null) {
				this.defaultEventExecutorGroup.shutdownGracefully();
			}
		} catch (Exception e) {
			LOGGER.error("NettyRemotingClient shutdown exception, ", e);
		}

		if (this.publicExecutor != null) {
			try {
                ThreadUtils.shutdownAndAwaitTermination(this.publicExecutor);
			} catch (Exception e) {
				LOGGER.error("NettyRemotingClient shutdown exception, ", e);
			}
		}
	}

	

	private Channel getAndCreateServerChannel() {
		if (clientChannel != null && clientChannel.isOK()) {
			return clientChannel.getChannel();
		}

        final List<String> addressList = this.serverAddressList.get();
        try {
            this.lockServerChannel.tryLock(Constants.DEFAULT_READ_WRITE_LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
            String address = this.serverAddressSelected.get();
            if (address != null) {
                if (clientChannel != null && clientChannel.isOK()) {
                    return clientChannel.getChannel();
                }
            }

            if (addressList != null && !addressList.isEmpty()) {
                for (int i = 0; i < addressList.size(); i++) {
                    int index = this.serverAddressIndex.incrementAndGet();
                    index = Math.abs(index);
                    index = index % addressList.size();
                    String newAddress = addressList.get(index);

                    this.serverAddressSelected.set(newAddress);
                    Channel channelNew = this.createChannel();
                    if (channelNew != null) {
                        //reconnected event,not exactly
                        if (this.connectionStateListener != null) {
                            this.publicExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        connectionStateListener.reconnected();
                                    } catch (Throwable e) {
                                        LOGGER.error("connectionStateListener recover failed.", e);
                                    }
                                }
                            });
                        }
                        return channelNew;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            LOGGER.error("getAndCreateServerChannel: create name server channel exception", e);
            return null;
        } finally {
            this.lockServerChannel.unlock();
        }
    }

	private Channel createChannel() throws InterruptedException {
        if (clientChannel != null && clientChannel.isOK()) {
            return clientChannel.getChannel();
        }

        if (this.lockChannel.tryLock(Constants.DEFAULT_READ_WRITE_LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
            try {
                boolean createNewConnection;

                if (clientChannel != null) {
                    if (clientChannel.isOK()) {
                        return clientChannel.getChannel();
                    }
                    else {
                        createNewConnection = clientChannel.getChannelFuture().isDone();
                    }
                } else {
                    createNewConnection = true;
                }

                if (createNewConnection) {
                    ChannelFuture channelFuture = this.bootstrap.connect(RemotingHelper.string2SocketAddress(serverAddressSelected.get()));
                    LOGGER.info("createChannel: begin to connect remote host[{}] asynchronously", serverAddressSelected.get());
                    clientChannel = new ChannelWrapper(channelFuture);
                }
            } catch (Exception e) {
                LOGGER.error("createChannel: create channel exception", e);
            } finally {
                this.lockChannel.unlock();
            }
        } else {
            LOGGER.warn("createChannel: try to lock channel table, but timeout{} ms", Constants.DEFAULT_READ_WRITE_LOCK_TIMEOUT);
        }

        if (clientChannel != null) {
            ChannelFuture channelFuture = clientChannel.getChannelFuture();
            if (channelFuture.awaitUninterruptibly(this.nettyClientConfig.getConnectTimeoutMillis())) {
                if (clientChannel.isOK()) {
                    LOGGER.info("createChannel: connect remote host {} success,local address {}",
                            serverAddressSelected.get(), RemotingHelper.parseChannelLocalAddress(clientChannel.getChannel()));
                    return clientChannel.getChannel();
                } else {
                    LOGGER.warn("createChannel: connect remote host " + serverAddressSelected.get() + " failed, " + channelFuture.toString(), channelFuture.cause());
                }
            } else {
                LOGGER.warn("createChannel: connect remote host{} timeout {}ms, {}", serverAddressSelected.get(), this.nettyClientConfig.getConnectTimeoutMillis(), channelFuture.toString());
            }
        }
        return null;
    }

	public void closeChannel(final Channel channel) {
		if (null == channel)
			return;

		try {
			if (this.lockChannel.tryLock(Constants.DEFAULT_READ_WRITE_LOCK_TIMEOUT,
					TimeUnit.MILLISECONDS)) {
				try {
					RemotingUtil.closeChannel(channel);
				} catch (Exception e) {
					LOGGER.error("closeChannel: close the channel exception", e);
				} finally {
					this.lockChannel.unlock();
				}
			} else {
				LOGGER.warn("closeChannel: try to lock channel table, but timeout {}ms", Constants.DEFAULT_READ_WRITE_LOCK_TIMEOUT);
			}
		} catch (InterruptedException e) {
			LOGGER.error("closeChannel exception", e);
		}
	}

    @Override
	public void registerProcessor(int requestCode, RequestProcessor processor,
			ExecutorService executor) {
		ExecutorService executorThis = executor;
		if (null == executor) {
			executorThis = this.publicExecutor;
		}

		Pair<RequestProcessor, ExecutorService> pair = new Pair<RequestProcessor, ExecutorService>(processor, executorThis);
		this.processorTable.put(requestCode, pair);
	}

    @Override
	public RemotingCommand invokeSync(final RemotingCommand request, long timeoutMillis)
			throws InterruptedException, RemotingConnectException,RemotingSendRequestException, RemotingTimeoutException {
		final Channel channel = this.getAndCreateServerChannel();
		if (channel != null && channel.isActive()) {
			try {
				if (this.rpcHook != null) {
					this.rpcHook.doBeforeRequest(serverAddressSelected.get(), request);
				}
				RemotingCommand response = this.invokeSyncImpl(channel,request, timeoutMillis);
				if (this.rpcHook != null) {
					this.rpcHook.doAfterResponse(request, response);
				}
				return response;
			} catch (RemotingSendRequestException e) {
				LOGGER.warn("invokeSync: send request exception,channel {}", serverAddressSelected.get());
				throw e;
			} catch (RemotingTimeoutException e) {
                LOGGER.warn("invokeSync: request exception,channel {}", serverAddressSelected.get());
				throw e;
			}
		} else {
			this.closeChannel(channel);
			throw new RemotingConnectException(serverAddressSelected.get());
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

	public List<String> getServerAddressList() {
		return serverAddressList.get();
	}
	
    public void updateNameServerAddressList(List<String> address) {
        List<String> old = this.serverAddressList.get();
        boolean update = false;

        if (!address.isEmpty()) {
            if (null == old) {
                update = true;
            }
            else if (address.size() != old.size()) {
                update = true;
            }
            else {
                for (int i = 0; i < address.size() && !update; i++) {
                    if (!old.contains(address.get(i))) {
                        update = true;
                    }
                }
            }

            if (update) {
                Collections.shuffle(address);
                this.serverAddressList.set(address);
            }
        }
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

    public boolean isAvailable(){
        return clientChannel != null && clientChannel.isOK();
    }
}