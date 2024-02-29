package com.dong.loadbalance.impl;

import com.dong.DrpcBootstrap;
import com.dong.loadbalance.AbstractLoadBalance;
import com.dong.loadbalance.LoadBalance;
import com.dong.loadbalance.Selector;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

@Slf4j
public class MinimumResponseTimeLoadBalance extends AbstractLoadBalance {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return null;
    }

    private static class MinimumResponseTimeSelector implements Selector{

        @Override
        public InetSocketAddress getNext() {
            Map.Entry<Long, Channel> entry = DrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.firstEntry();
            if(entry != null){
                return (InetSocketAddress)entry.getValue().remoteAddress();
            }
            // 直接从缓存中获取一个可用的
            Channel channel = (Channel) DrpcBootstrap.CHANNEL_CACHE.values().toArray()[0];
            log.debug("选取的服务响应时间是【{}】ms",entry.getKey());
            return (InetSocketAddress) channel.remoteAddress();
        }

        @Override
        public void reLoadBalance() {

        }
    }
}
