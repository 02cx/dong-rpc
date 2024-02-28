package com.dong.core;

import com.dong.DrpcBootstrap;
import com.dong.NettyBootstrapInitializer;
import com.dong.compress.CompressorFactory;
import com.dong.discovery.Register;
import com.dong.enumeration.RequestType;
import com.dong.serialize.SerializerFactory;
import com.dong.transport.message.DrpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 *  心跳检测
 */
@Slf4j
public class HeartbeatDetection {

    public static void detectHeartbeat(String serviceName){
        // 1.从注册中心拉取服务并建立连接
        Register register = DrpcBootstrap.getInstance().getRegister();
        List<InetSocketAddress> address = register.lookup(serviceName, "");
        // 2.将连接进行缓存
        for (InetSocketAddress inetSocketAddress : address) {
            try {
                 if(!DrpcBootstrap.CHANNEL_CACHE.containsKey(inetSocketAddress)){
                     Channel channel = NettyBootstrapInitializer.getBootstrap().connect(inetSocketAddress).sync().channel();
                     DrpcBootstrap.CHANNEL_CACHE.put(inetSocketAddress,channel);
                 }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        // 3。定时发送任务
        Thread thread = new Thread(() -> {
            new Timer().scheduleAtFixedRate(new MyTimerTask(), 0, 2000);
        },"Heartbeat Thread");
        // 设置为守护线程
        thread.setDaemon(true);
        thread.start();

    }

    private static class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            // 清空心跳响应时间缓存
            DrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.clear();
            // 遍历所有的channel
            Map<InetSocketAddress, Channel> channelCache = DrpcBootstrap.CHANNEL_CACHE;
            for(Map.Entry<InetSocketAddress, Channel> entry : channelCache.entrySet()){
                long startTime = System.currentTimeMillis();
                Channel channel = entry.getValue();
                // 构建心跳请求
                DrpcRequest drpcRequest = DrpcRequest.builder()
                        .requestId(DrpcBootstrap.ID_GENERATOR.getId())
                        .compressType(CompressorFactory.getCompressor(DrpcBootstrap.COMPRESSOR_TYPE).getCode())
                        .serializeType(SerializerFactory.getSerializer(DrpcBootstrap.SERIALIZE_TYPE).getCode())
                        .requestType((RequestType.HEAD_BEAT.getId()))
                        .build();
                // 发送消息，异步监听
                CompletableFuture<Object> objectCompletableFuture = new CompletableFuture<>();
                DrpcBootstrap.PENDING_REQUEST.put(drpcRequest.getRequestId(),objectCompletableFuture);
                channel.writeAndFlush(drpcRequest).addListener(
                        (ChannelFutureListener) promise -> {
                            if(!promise.isSuccess()){
                                objectCompletableFuture.completeExceptionally(promise.cause());
                            }
                        }
                );
                long endTime = 0L;
                try {
                    objectCompletableFuture.get();
                    endTime = System.currentTimeMillis();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
                long time = endTime - startTime;

                // 使用treeMap进行缓存
                DrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.put(time,channel);

                log.debug("和【{}】服务器的响应时间是----->【{}】",entry.getKey(),time);
            }
        }
    }
}
