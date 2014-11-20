package com.cmbc.configserver.remoting.netty;

import com.cmbc.configserver.remoting.ChannelEventListener;
import com.cmbc.configserver.remoting.InvokeCallback;
import com.cmbc.configserver.remoting.RPCHook;
import com.cmbc.configserver.remoting.common.*;
import com.cmbc.configserver.remoting.exception.RemotingSendRequestException;
import com.cmbc.configserver.remoting.exception.RemotingTimeoutException;
import com.cmbc.configserver.remoting.exception.RemotingTooMuchRequestException;
import com.cmbc.configserver.remoting.protocol.RemotingCommand;
import com.cmbc.configserver.remoting.protocol.RemotingSysResponseCode;
import com.cmbc.configserver.utils.Constants;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.*;

public abstract class NettyRemotingAbstract {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyRemotingAbstract.class);
    protected final Semaphore semaphoreOneway;
    protected final Semaphore semaphoreAsync;
    protected final byte[] SYSTEM_BUSY_BYTES = "too many requests and system thread pool busy, please try another server".getBytes();

    // cache all the remote request that is outing
    protected final ConcurrentHashMap<Integer /* requestId */, ResponseFuture> responseTable =  new ConcurrentHashMap<Integer, ResponseFuture>(256);
    // the default request processor
    protected Pair<RequestProcessor, ExecutorService> defaultRequestProcessor;
    // the request processors
    protected final HashMap<Integer/* request code */, Pair<RequestProcessor, ExecutorService>> processorTable =  new HashMap<Integer, Pair<RequestProcessor, ExecutorService>>(64);
    protected final NettyEventExecutor nettyEventExecutor = new NettyEventExecutor();
    public abstract ChannelEventListener getChannelEventListener();

    public abstract RPCHook getRPCHook();

    public void putNettyEvent(final NettyEvent event) {
        this.nettyEventExecutor.putNettyEvent(event);
    }

    class NettyEventExecutor extends ServiceThread {
        private final LinkedBlockingQueue<NettyEvent> eventQueue = new LinkedBlockingQueue<NettyEvent>();
        public void putNettyEvent(final NettyEvent event) {
            if (this.eventQueue.size() <= Constants.DEFAULT_MAX_QUEUE_ITEM) {
                this.eventQueue.add(event);
            }
            else {
                LOGGER.warn("event queue size {} enough, so drop this event {}", this.eventQueue.size(),event.toString());
            }
        }

        @Override
        public void run() {
            LOGGER.info(this.getServiceName() + " service started");
            final ChannelEventListener listener = NettyRemotingAbstract.this.getChannelEventListener();
            while (!this.isStopped()) {
                try {
                    NettyEvent event = this.eventQueue.poll(Constants.DEFAULT_QUEUE_TIMEOUT, TimeUnit.MILLISECONDS);
                    if (event != null) {
                        switch (event.getType()) {
                        case IDLE:
                            listener.onChannelIdle(event.getRemoteAddr(), event.getChannel());
                            break;
                        case CLOSE:
                            listener.onChannelClose(event.getRemoteAddr(), event.getChannel());
                            break;
                        case CONNECT:
                            listener.onChannelConnect(event.getRemoteAddr(), event.getChannel());
                            break;
                        case EXCEPTION:
                            listener.onChannelException(event.getRemoteAddr(), event.getChannel());
                            break;
                        case ACTIVE:
                        	listener.onChannelActive(event.getChannel());
                        	break;
                        default:
                            break;
                        }
                    }
                }
                catch (Exception e) {
                    LOGGER.warn(this.getServiceName() + " service has exception. ", e);
                }
            }
            LOGGER.info(this.getServiceName() + " service end");
        }

        @Override
        public String getServiceName() {
            return NettyEventExecutor.class.getSimpleName();
        }
    }


    public NettyRemotingAbstract(final int permitsOneway, final int permitsAsync) {
        this.semaphoreOneway = new Semaphore(permitsOneway, true);
        this.semaphoreAsync = new Semaphore(permitsAsync, true);
    }


    public void processRequestCommand(final ChannelHandlerContext ctx, final RemotingCommand cmd) {
        final Pair<RequestProcessor, ExecutorService> matched = this.processorTable.get(cmd.getCode());
        final Pair<RequestProcessor, ExecutorService> pair = null== matched ? this.defaultRequestProcessor : matched;
        if (pair != null) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        RPCHook rpcHook = NettyRemotingAbstract.this.getRPCHook();
                        if (rpcHook != null) {
                            rpcHook.doBeforeRequest(RemotingHelper.parseChannelRemoteAddress(ctx.channel()), cmd);
                        }

                        final RemotingCommand response = pair.getObject1().processRequest(ctx, cmd);
                        if (rpcHook != null) {
                            rpcHook.doAfterResponse(cmd, response);
                        }

                        // ignore the result in one way
                        if (!cmd.isOnewayRPC()) {
                            if (response != null) {
                                //set the request id for response
                                response.setRequestId(cmd.getRequestId());
                                response.markResponseType();
                                try {
                                    ctx.writeAndFlush(response);
                                } catch (Throwable e) {
                                    LOGGER.error(String.format("process request %s over, but response %s failed", cmd.toString(), response.toString()), e);
                                }
                            }
                        }
                    }
                    catch (Throwable e) {
                        LOGGER.error(String.format("process request %s error,exception is ", cmd.toString()), e);
                        if (!cmd.isOnewayRPC()) {
                            final RemotingCommand response = RemotingCommand.createResponseCommand(RemotingSysResponseCode.SYSTEM_ERROR, cmd.getRequestId());
                            response.setBody(e.getMessage() == null ? "system error".getBytes() : e.getMessage().getBytes());
                            response.setRequestId(cmd.getRequestId());
                            ctx.writeAndFlush(response);
                        }
                    }
                }
            };

            try {
                pair.getObject2().submit(run);
            }
            catch (RejectedExecutionException e) {
                LOGGER.warn(String.format("%s too many requests and system thread pool busy, RejectedExecutionException %s of request code %s",
                        RemotingHelper.parseChannelRemoteAddress(ctx.channel()), pair.getObject2().toString(), cmd.getCode()));
                if (!cmd.isOnewayRPC()) {
                    final RemotingCommand response = RemotingCommand.createResponseCommand(RemotingSysResponseCode.SYSTEM_BUSY, cmd.getRequestId());
                    response.setBody(SYSTEM_BUSY_BYTES);
                    response.setRequestId(cmd.getRequestId());
                    ctx.writeAndFlush(response);
                }
            }
        }
        else {
            //request is not supported
            String error = "request type " + cmd.getCode() + " not supported";
            final RemotingCommand response = RemotingCommand.createResponseCommand(RemotingSysResponseCode.REQUEST_CODE_NOT_SUPPORTED, cmd.getRequestId());
            response.setBody(error.getBytes());
            response.setRequestId(cmd.getRequestId());
            ctx.writeAndFlush(response);
            LOGGER.error(RemotingHelper.parseChannelRemoteAddress(ctx.channel()) + error);
        }
    }


    public void processResponseCommand(ChannelHandlerContext ctx, RemotingCommand cmd) {
        final ResponseFuture responseFuture = responseTable.get(cmd.getRequestId());
        if (responseFuture != null) {
            responseFuture.setResponseCommand(cmd);
            responseFuture.release();

            // async remote call
            if (responseFuture.getInvokeCallback() != null) {
                boolean runInThisThread = false;
                ExecutorService executor = this.getCallbackExecutor();
                if (executor != null) {
                    try {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    responseFuture.executeInvokeCallback();
                                }
                                catch (Throwable e) {
                                    LOGGER.warn("execute callback in executor exception, and callback throw", e);
                                }
                            }
                        });
                    }
                    catch (Exception e) {
                        runInThisThread = true;
                        LOGGER.warn("execute callback in executor exception, maybe executor busy", e);
                    }
                }
                else {
                    runInThisThread = true;
                }

                if (runInThisThread) {
                    try {
                        responseFuture.executeInvokeCallback();
                    }
                    catch (Throwable e) {
                        LOGGER.warn("executeInvokeCallback Exception", e);
                    }
                }
            }
            // sync remote call
            else {
                responseFuture.putResponse(cmd);
            }
        }
        else {
            //case
            //1.the request is not sending by the client
            //2.the request is sending by the client,but it is timeout,and after the timeout period,the server response this request.
            LOGGER.warn(String.format("receive response %s, but not matched any request,channel id %s", cmd.toString(), RemotingHelper.getChannelId(ctx.channel())));
        }
        responseTable.remove(cmd.getRequestId());
    }


    public void processMessageReceived(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {
        final RemotingCommand cmd = msg;
        if (cmd != null) {
            switch (cmd.getType()) {
            case REQUEST_COMMAND:
                processRequestCommand(ctx, cmd);
                break;
            case RESPONSE_COMMAND:
                processResponseCommand(ctx, cmd);
                break;
            default:
                break;
            }
        }
    }

    abstract public ExecutorService getCallbackExecutor();

    public void scanResponseTable() {
        Iterator<Entry<Integer, ResponseFuture>> it = this.responseTable.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, ResponseFuture> next = it.next();
            ResponseFuture rep = next.getValue();

            if ((rep.getBeginTimestamp() + rep.getTimeoutMillis() + 1000) <= System.currentTimeMillis()) {
                it.remove();
                try {
                    rep.executeInvokeCallback();
                }
                catch (Throwable e) {
                    LOGGER.warn("scanResponseTable, operationComplete Exception", e);
                }
                finally {
                    rep.release();
                }

                LOGGER.warn("remove timeout request, " + rep);
            }
        }
    }

    public RemotingCommand invokeSyncImpl(final Channel channel, final RemotingCommand request,
            final long timeoutMillis) throws InterruptedException, RemotingSendRequestException,
            RemotingTimeoutException {
        try {
            final ResponseFuture responseFuture =
                    new ResponseFuture(request.getRequestId(), timeoutMillis, null, null);
            this.responseTable.put(request.getRequestId(), responseFuture);
            channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture f) throws Exception {
                    if (f.isSuccess()) {
                        responseFuture.setSendRequestOK(true);
                        return;
                    }
                    else {
                        responseFuture.setSendRequestOK(false);
                    }
                    responseTable.remove(request.getRequestId());
                    responseFuture.setCause(f.cause());
                    responseFuture.putResponse(null);
                    LOGGER.warn(String.format("send request %s to channel <%s> failed.", request.toString(), RemotingHelper.getChannelId(channel)));
                }
            });
            RemotingCommand responseCommand = responseFuture.waitResponse(timeoutMillis);
            if (null == responseCommand) {
                // successfully sending the remote request,timeout happens when reading the ACK
                if (responseFuture.isSendRequestOK()) {
                    LOGGER.warn(String.format("timeout when waiting for response from remote channel %s. request is %s",
                            RemotingHelper.parseChannelRemoteAddress(channel),
                            request.toString()));
                    throw new RemotingTimeoutException(RemotingHelper.getChannelId(channel),
                        timeoutMillis, responseFuture.getCause());
                }
                // failed in sending the remote request
                else {
                    LOGGER.warn(String.format("failed when sending request to remote channel %s. request is %s",
                            RemotingHelper.parseChannelRemoteAddress(channel),
                            request.toString()));
                    throw new RemotingSendRequestException(RemotingHelper.getChannelId(channel),
                        responseFuture.getCause());
                }
            }
            return responseCommand;
        }
        finally {
            this.responseTable.remove(request.getRequestId());
        }
    }

    public void invokeAsyncImpl(final Channel channel, final RemotingCommand request,
            final long timeoutMillis, final InvokeCallback invokeCallback) throws InterruptedException,
            RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException {
        boolean acquired = this.semaphoreAsync.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        if (acquired) {
            final SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(this.semaphoreAsync);

            final ResponseFuture responseFuture =
                    new ResponseFuture(request.getRequestId(), timeoutMillis, invokeCallback, once);
            this.responseTable.put(request.getRequestId(), responseFuture);
            try {
                channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture f) throws Exception {
                        if (f.isSuccess()) {
                            responseFuture.setSendRequestOK(true);
                            return;
                        }
                        else {
                            responseFuture.setSendRequestOK(false);
                        }

                        responseFuture.putResponse(null);
                        responseTable.remove(request.getRequestId());
                        try {
                            responseFuture.executeInvokeCallback();
                        }
                        catch (Throwable e) {
                            LOGGER.warn("execute callback in writeAndFlush addListener, and callback throw", e);
                        }
                        finally {
                            responseFuture.release();
                        }

                        LOGGER.warn("send a request command to channel <{}> failed.",
                                RemotingHelper.parseChannelRemoteAddress(channel));
                        LOGGER.warn(request.toString());
                    }
                });
            }
            catch (Exception e) {
                responseFuture.release();
                LOGGER.warn(
                        "send a request command to channel <" + RemotingHelper.parseChannelRemoteAddress(channel)
                                + "> Exception", e);
                throw new RemotingSendRequestException(RemotingHelper.getChannelId(channel), e);
            }
        }
        else {
            if (timeoutMillis <= 0) {
                throw new RemotingTooMuchRequestException("invokeAsyncImpl invoke too fast");
            }
            else {
                String info = String.format("invokeAsyncImpl tryAcquire semaphore timeout, %dms, waiting thread numbers: %d semaphoreAsyncValue: %d", //
                        timeoutMillis, this.semaphoreAsync.getQueueLength(), this.semaphoreAsync.availablePermits());
                LOGGER.warn(info);
                LOGGER.warn("async request is " + request.toString());
                throw new RemotingTimeoutException(info);
            }
        }
    }

    public void invokeOnewayImpl(final Channel channel, final RemotingCommand request,
            final long timeoutMillis) throws InterruptedException, RemotingTooMuchRequestException,
            RemotingTimeoutException, RemotingSendRequestException {
        request.markOnewayRPC();
        boolean acquired = this.semaphoreOneway.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        if (acquired) {
            final SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(this.semaphoreOneway);
            try {
                channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture f) throws Exception {
                        once.release();
                        if (!f.isSuccess()) {
                            LOGGER.warn("send a request command to channel <" + channel.remoteAddress() + "> failed.");
                            LOGGER.warn("one way request is" + request.toString());
                        }
                    }
                });
            }
            catch (Exception e) {
                once.release();
                LOGGER.warn("write send a request command to channel <" + channel.remoteAddress() + "> failed.");
                throw new RemotingSendRequestException(RemotingHelper.getChannelId(channel), e);
            }
        }
        else {
            if (timeoutMillis <= 0) {
                throw new RemotingTooMuchRequestException("invokeOnewayImpl invoke too fast");
            }
            else {
                String info = String.format("invokeOnewayImpl tryAcquire semaphore timeout  %dms, waiting thread numbers: %d semaphoreAsyncValue: %d",
                        timeoutMillis, this.semaphoreAsync.getQueueLength(), this.semaphoreAsync.availablePermits());
                LOGGER.warn(info);
                LOGGER.warn(request.toString());
                throw new RemotingTimeoutException(info);
            }
        }
    }
}