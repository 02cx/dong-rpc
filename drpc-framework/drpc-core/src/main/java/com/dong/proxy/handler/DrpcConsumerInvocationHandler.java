package com.dong.proxy.handler;

import com.dong.DrpcBootstrap;
import com.dong.NettyBootstrapInitializer;
import com.dong.discovery.Register;
import com.dong.enumeration.RequestType;
import com.dong.exceptions.NetworkException;
import com.dong.transport.message.DrpcRequest;
import com.dong.transport.message.RequestPayload;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DrpcConsumerInvocationHandler<T> implements InvocationHandler {

    private Register register;

    private Class<T> interfaceRef;


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 拉取服务  服务名   返回ip+端口
        List<InetSocketAddress> lookup = register.lookup(interfaceRef.getName(), "");
        if (log.isDebugEnabled()) {
            log.debug("服务调用方从注册中心拉取了服务【{}】", lookup);
        }
        //WYD TODO 2024-02-23:当前只有一个服务
        InetSocketAddress inetSocketAddress = lookup.get(0);
        // 2.用netty连接服务器，发送调用的  服务名+方法名+参数列表，得到结果
        //获取通道
        Channel channel = getAvailableChannel(inetSocketAddress);

        // 封装报文
        RequestPayload payload = RequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parametersType(method.getParameterTypes())
                .parametersValue(args).build();

        DrpcRequest drpcRequest = DrpcRequest.builder()
                .requestId(1L)
                .compressType((byte) 1)
                .serializeType((byte) 1)
                .requestType((RequestType.REQUEST.getId()))
                .requestPayload(payload).build();

        // 发送消息，异步监听
        CompletableFuture<Object> objectCompletableFuture = new CompletableFuture<>();
        DrpcBootstrap.PENDING_REQUEST.put(1L,objectCompletableFuture);
        ChannelFuture channelFuture = channel.writeAndFlush(drpcRequest).addListener(
                (ChannelFutureListener) promise -> {
                    if(!promise.isSuccess()){
                        objectCompletableFuture.completeExceptionally(promise.cause());
                    }
                }
        );

        // 获取响应结果
        return objectCompletableFuture.get(60,TimeUnit.SECONDS);
    }

    /**
     *  获取通道
     * @param inetSocketAddress
     * @return
     */
    private Channel getAvailableChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = DrpcBootstrap.CHANNEL_CACHE.get(inetSocketAddress);
        if (channel == null) {

            // 尝试连接服务器，等待连接
            // channel = NettyBootstrapInitializer.getBootstrap().connect(inetSocketAddress).sync().channel();

            // 使用addListener执行的异步操作
            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            NettyBootstrapInitializer.getBootstrap().connect(inetSocketAddress).addListener(
                    (ChannelFutureListener) promise -> {
                        if(promise.isDone()){
                            // 异步的
                            log.debug("已经和【{}】建立连接",inetSocketAddress);
                            channelFuture.complete(promise.channel());
                        }else if(!promise.isSuccess()){
                            channelFuture.completeExceptionally(promise.cause());
                        }
                    }
            );
            // 阻塞获取channel
            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new NetworkException("获取通道失败");
            }
            // 缓存channel
            DrpcBootstrap.CHANNEL_CACHE.put(inetSocketAddress, channel);

        }

        return channel;
    }
}
