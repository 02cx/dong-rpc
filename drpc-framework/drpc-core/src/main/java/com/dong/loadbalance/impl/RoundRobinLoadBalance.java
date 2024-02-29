package com.dong.loadbalance.impl;

import com.dong.DrpcBootstrap;
import com.dong.discovery.Register;
import com.dong.exceptions.LoadBalanceException;
import com.dong.loadbalance.AbstractLoadBalance;
import com.dong.loadbalance.LoadBalance;
import com.dong.loadbalance.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *  轮询的负载均衡
 */
@Slf4j
public class RoundRobinLoadBalance extends AbstractLoadBalance {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new RoundRobinSelector(serviceList);
    }

    private static class RoundRobinSelector implements Selector{

        private List<InetSocketAddress> serviceList;
        private AtomicInteger index;

        public RoundRobinSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress getNext() {
            if(serviceList == null || serviceList.size() == 0){
                log.error("服务列表为空，负载均衡失效");
                throw new LoadBalanceException();
            }
            InetSocketAddress inetSocketAddress = serviceList.get(index.get());

            // 游标指到最后一位，重置
            if(index.get() == serviceList.size() - 1){
                index.set(0);
            }else{
                // 游标向后移动一位
                index.incrementAndGet();
            }


            return inetSocketAddress;
        }

    }
}
