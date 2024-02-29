package com.dong.core;

import com.dong.Configuration;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 心跳检测
 */
@Slf4j
public class HeartbeatDetection {

    public static void detectHeartbeat(String serviceName) {
        // 1.从注册中心拉取服务并建立连接
        Register register = DrpcBootstrap.getInstance().getRegister();
        List<InetSocketAddress> address = register.lookup(serviceName, "");
        // 2.将连接进行缓存
        for (InetSocketAddress inetSocketAddress : address) {
            try {
                if (!DrpcBootstrap.CHANNEL_CACHE.containsKey(inetSocketAddress)) {
                    Channel channel = NettyBootstrapInitializer.getBootstrap().connect(inetSocketAddress).sync().channel();
                    DrpcBootstrap.CHANNEL_CACHE.put(inetSocketAddress, channel);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        // 3。定时发送任务
        Thread thread = new Thread(() -> {
            new Timer().scheduleAtFixedRate(new MyTimerTask(), 0, 2000);
        }, "Heartbeat Thread");
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

            for (Map.Entry<InetSocketAddress, Channel> entry : channelCache.entrySet()) {
                // 心跳检测失败重试次数
                int retryCount = 3;
                while (retryCount > 0) {
                    long startTime = System.currentTimeMillis();
                    Channel channel = entry.getValue();
                    // 构建心跳请求
                    Configuration configuration = DrpcBootstrap.getInstance().getConfiguration();
                    DrpcRequest drpcRequest = DrpcRequest.builder()
                            .requestId(configuration.getIdGenerator().getId())
                            .compressType(CompressorFactory.getCompressor(configuration.getCompressorType()).getCode())
                            .serializeType(SerializerFactory.getSerializer(configuration.getSerializeType()).getCode())
                            .requestType((RequestType.HEAD_BEAT.getId()))
                            .build();
                    // 发送消息，异步监听
                    CompletableFuture<Object> objectCompletableFuture = new CompletableFuture<>();
                    DrpcBootstrap.PENDING_REQUEST.put(drpcRequest.getRequestId(), objectCompletableFuture);
                    channel.writeAndFlush(drpcRequest).addListener(
                            (ChannelFutureListener) promise -> {
                                if (!promise.isSuccess()) {
                                    objectCompletableFuture.completeExceptionally(promise.cause());
                                }
                            }
                    );
                    long endTime = 0L;
                    try {
                        objectCompletableFuture.get(1, TimeUnit.SECONDS);
                        endTime = System.currentTimeMillis();
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        retryCount--;
                        log.error("与服务【{}】的连接发生异常,进行第【{}】重试.......", channel.remoteAddress(), 3 - retryCount);
                        if (retryCount == 0) {
                            // 将失效服务移除列表
                            DrpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                        }
                        try {
                            // 简单睡眠一下，防止重试风暴，以及马上重试
                            Thread.sleep(10 * (3 - retryCount));
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        continue;
                    }
                    long time = endTime - startTime;

                    // 使用treeMap进行缓存
                    DrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.put(time, channel);

                    log.debug("和【{}】服务器的响应时间是----->【{}】", entry.getKey(), time);
                    break;
                }
            }

            log.info("--------------响应时间的TreeMap----------------");
            for (Map.Entry<Long, Channel> entry : DrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.entrySet()) {
                log.debug("[{}]----------->[{}]", entry.getKey(), entry.getValue().id());
            }
        }
    }
}
