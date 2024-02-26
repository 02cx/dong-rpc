package com.dong.loadbalance;

import java.net.InetSocketAddress;
import java.util.List;

public interface Selector {

    /**
     * 根据服务列表，使用算法算法获取一个服务
     * @return
     */
    InetSocketAddress getNext();


    void reLoadBalance();
}
