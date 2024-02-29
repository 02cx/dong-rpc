package com.dong.discovery.impl;

import com.dong.Constant;
import com.dong.DrpcBootstrap;
import com.dong.ServiceConfig;
import com.dong.discovery.AbstractRegistry;
import com.dong.exceptions.NetworkException;
import com.dong.utils.NetUtils;
import com.dong.utils.zookeeper.ZookeeperNode;
import com.dong.utils.zookeeper.ZookeeperUtils;
import com.dong.watch.UpAndDownWatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ZookeeperRegister extends AbstractRegistry {

    private ZooKeeper zooKeeper;

    public ZookeeperRegister() {
        zooKeeper = ZookeeperUtils.createZookeeper();
    }

    public ZookeeperRegister(String connectStr,int timeOut) {
        zooKeeper = ZookeeperUtils.createZookeeper(connectStr,timeOut);
    }

    @Override
    public void register(ServiceConfig<?> service) {
        // 服务节点名称
        String parentNode = Constant.BASE_PROVIDER_PATH + "/" + service.getInterface().getName();
        // 创建一个持久节点
        if(!ZookeeperUtils.exists(zooKeeper,parentNode,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode, null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.PERSISTENT);
        }

        // 创建本机临时节点，  ip:port
        //WYD TODO 2024-02-22: 后续处理全局端口问题
        String node = parentNode + "/" + NetUtils.getIpAddress() + ":" + DrpcBootstrap.getInstance().getConfiguration().getPort();
        if(!ZookeeperUtils.exists(zooKeeper,node,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(node, null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.EPHEMERAL);
        }


        log.debug("{}服务已注册", service.toString());
    }

    @Override
    public List<InetSocketAddress> lookup(String serviceName, String group) {
        // 1。找到服务对应的节点
        String serviceNode = Constant.BASE_PROVIDER_PATH + "/" + serviceName;
        // 2.从zk中获取它的子节点
        List<String> children = ZookeeperUtils.getChildren(zooKeeper, serviceNode, new UpAndDownWatch());
        List<InetSocketAddress> list = children.stream().map(ipString -> {
            String[] ipAndPort = ipString.split(":");
            return new InetSocketAddress(ipAndPort[0], Integer.valueOf(ipAndPort[1]));
        }).collect(Collectors.toList());

        if(list.size() == 0){
            throw new NetworkException("未能从注册中心拉取到服务");
        }

        return list;
    }
}
