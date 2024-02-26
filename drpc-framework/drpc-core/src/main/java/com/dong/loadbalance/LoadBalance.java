package com.dong.loadbalance;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡器接口
 */
public interface LoadBalance {

    // 根据服务名，获取一个可用的服务
    InetSocketAddress selectServiceAddress(String serviceName);
}
