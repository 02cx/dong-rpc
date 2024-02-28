package com.dong.test;

import com.dong.DrpcBootstrap;
import com.dong.core.HeartbeatDetection;
import com.dong.discovery.Register;
import com.dong.discovery.RegisterConfig;
import com.dong.discovery.impl.ZookeeperRegister;
import com.dong.utils.zookeeper.ZookeeperUtils;

import java.net.InetSocketAddress;
import java.util.List;

public class LookupTest {
    public static void main(String[] args) {
        DrpcBootstrap.getInstance().register(new RegisterConfig("zookeeper://192.168.183.130:2181"));
        List<InetSocketAddress> lookup = DrpcBootstrap.getInstance().getRegister().lookup("com.dong.HelloDrpc", "");
        for(InetSocketAddress address : lookup){
            System.out.println("拉取：" + address);
        }

        HeartbeatDetection.detectHeartbeat("com.dong.HelloDrpc");
    }
}
