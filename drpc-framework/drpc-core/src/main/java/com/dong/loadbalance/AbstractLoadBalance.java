package com.dong.loadbalance;

import com.dong.DrpcBootstrap;
import com.dong.discovery.Register;
import com.dong.loadbalance.impl.RoundRobinLoadBalance;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractLoadBalance implements LoadBalance{
    // 一个服务匹配一个selector
    private ConcurrentHashMap<String, Selector> cache = new ConcurrentHashMap<>(8);

    @Override
    public InetSocketAddress selectServiceAddress(String serviceName) {
        // 从cache中拿到一个selector
        Selector selector = cache.get(serviceName);
        // cache中没拿到，就新建selector
        if(selector == null){
            // 从注册中心拉取服务列表
            List<InetSocketAddress> serviceList = DrpcBootstrap.getInstance().getRegister().lookup(serviceName, null);
            // 根据服务列表获取selector
            selector = getSelector(serviceList);
            cache.put(serviceName,selector);
        }

        return selector.getNext();
    }

    @Override
    public synchronized void reLoadBalance(String serviceName,List<InetSocketAddress> addresses) {
        cache.put(serviceName,getSelector(addresses));
    }

    /**
     *  由子类扩展
     * @param serviceList
     * @return
     */
    protected abstract Selector getSelector(List<InetSocketAddress> serviceList);

}
