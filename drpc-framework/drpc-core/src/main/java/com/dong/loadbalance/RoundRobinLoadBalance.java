package com.dong.loadbalance;

import com.dong.DrpcBootstrap;
import com.dong.discovery.Register;
import com.dong.exceptions.LoadBalanceException;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *  轮询的负载均衡
 */
@Slf4j
public class RoundRobinLoadBalance implements LoadBalance{

    private Register register;

    // 一个服务匹配一个selector
    private ConcurrentHashMap<String,Selector> cache = new ConcurrentHashMap<>(8);

    public RoundRobinLoadBalance() {
        register = DrpcBootstrap.getInstance().getRegister();
    }

    @Override
    public InetSocketAddress selectServiceAddress(String serviceName) {
        // 从cache中拿到一个selector
        Selector selector = cache.get(serviceName);
        // cache中没拿到，就新建selector
        if(selector == null){
            // 从注册中心拉取服务列表
            List<InetSocketAddress> serviceList = register.lookup(serviceName, null);
            // 根据服务列表获取selector
            selector = new RoundRobinSelector(serviceList);
            cache.put(serviceName,selector);
        }

        return selector.getNext();
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
            }
            // 游标向后移动一位
            index.incrementAndGet();

            return inetSocketAddress;
        }

        @Override
        public void reLoadBalance() {

        }
    }
}
