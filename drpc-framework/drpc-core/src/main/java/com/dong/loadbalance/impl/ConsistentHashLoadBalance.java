package com.dong.loadbalance.impl;

import com.dong.DrpcBootstrap;
import com.dong.exceptions.LoadBalanceException;
import com.dong.loadbalance.AbstractLoadBalance;
import com.dong.loadbalance.Selector;
import com.dong.loadbalance.algorithm.HashAlgorithm;
import com.dong.transport.message.DrpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *  轮询的负载均衡
 */
@Slf4j
public class ConsistentHashLoadBalance extends AbstractLoadBalance {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new ConsistenHashSelector(serviceList,128);
    }

    private static class ConsistenHashSelector implements Selector{

        // hash环用来存储服务器节点
        private SortedMap<Long,InetSocketAddress> circle = new TreeMap<>();
        // 虚拟节点的个数
        private int virtualNodes;
        // hash算法
        private HashAlgorithm hashAlg;


        @Override
        public InetSocketAddress getNext() {
             // 拿到请求
            DrpcRequest drpcRequest = DrpcBootstrap.REQUEST_THREAD_LOCAL.get();
            // 根据请求选择服务器
            String requestId = Long.toString(drpcRequest.getRequestId());
            // 判断请求hash是否与服务器hash在一个点上，是：返回   否：顺时针找到最近的一个服务器返回
            InetSocketAddress service = getPrimary(requestId);
            return service;
        }

        public ConsistenHashSelector(List<InetSocketAddress> serviceList, int virtualNodes) {
            // 将服务器节点转为虚拟节点，进行挂载
            this.virtualNodes = virtualNodes;
            this.hashAlg = HashAlgorithm.KETAMA_HASH;

            //将每个服务节点挂载环上
            for (InetSocketAddress inetSocketAddress: serviceList) {
                for (int i = 0; i < virtualNodes / 4; i++) {
                    byte[] digest = hashAlg.computeMd5(inetSocketAddress.toString() + "-" + i);
                    for(int h = 0; h < 4; h++) {
                        long m = hashAlg.hash(digest, h);
                        circle.put(m, inetSocketAddress);
                    }
                }
            }
        }

        public InetSocketAddress getPrimary(String k) {
            byte[] digest = hashAlg.computeMd5(k);
            long hash = hashAlg.hash(digest, 0);
            log.debug("客户端【{}】的hash值为【{}】",k,hash);
            InetSocketAddress rv=getNodeForKey(hash);
            return rv;
        }

        InetSocketAddress getNodeForKey(long hash) {
            final InetSocketAddress rv;
            Long key = hash;
            if(!circle.containsKey(key)) {
                SortedMap<Long, InetSocketAddress> tailMap=circle.tailMap(key);
                if(tailMap.isEmpty()) {
                    key=circle.firstKey();
                } else {
                    key=tailMap.firstKey();
                }
            }

            rv=circle.get(key);
            return rv;
        }

        @Override
        public void reLoadBalance() {

        }
    }
}
