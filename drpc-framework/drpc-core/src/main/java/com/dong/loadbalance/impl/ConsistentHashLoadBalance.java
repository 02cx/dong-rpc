package com.dong.loadbalance.impl;

import com.dong.exceptions.LoadBalanceException;
import com.dong.loadbalance.AbstractLoadBalance;
import com.dong.loadbalance.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
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
        private SortedMap<Integer,InetSocketAddress> circle = new TreeMap<>();
        // 虚拟节点的个数
        private int virtualNodes;

        @Override
        public InetSocketAddress getNext() {
            //
            return null;
        }

        public ConsistenHashSelector(List<InetSocketAddress> serviceList, int virtualNodes) {
            // 将服务器节点转为虚拟节点，进行挂载
            this.virtualNodes = virtualNodes;
            for(InetSocketAddress inetSocketAddress : serviceList){
                addNodeToCircle(inetSocketAddress);
            }
        }

        /**
         * 将每个服务节点挂载环上
         * @param inetSocketAddress 服务地址
         */
        private void addNodeToCircle(InetSocketAddress inetSocketAddress) {
            // 生成虚拟节点
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" + i);
                // 挂载到环上
                circle.put(hash,inetSocketAddress);
            }
        }


        /**
         *  具体的hash算法
         * @param s
         * @return
         */
        private int hash(String s){
            return 0;
        }


        @Override
        public void reLoadBalance() {

        }
    }
}
