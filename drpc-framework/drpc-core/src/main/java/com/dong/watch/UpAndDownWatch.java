package com.dong.watch;

import com.dong.DrpcBootstrap;
import com.dong.NettyBootstrapInitializer;
import com.dong.discovery.Register;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;


@Slf4j
public class UpAndDownWatch implements Watcher {
    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeChildrenChanged) {
            log.debug("检测到有服务【{}】下有节点上线/下线，重新拉取服务列表......", event.getPath());

            String serviceName = getServiceName(event.getPath());
            Register register = DrpcBootstrap.getInstance().getRegister();
            List<InetSocketAddress> addresses = register.lookup(serviceName, "");
            // 处理新增的节点
            for (InetSocketAddress address : addresses) {
                // 新增的节点    在addresses中，不在CHANNEL_CACHEH中
                // 下线的节点   可能在CHANNEL_CACHEH中，不在addresses中
                if(!DrpcBootstrap.CHANNEL_CACHE.containsKey(address)){
                    // 根据地址建立连接，并进行缓存
                    Channel channel = null;
                    try {
                        channel = NettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    DrpcBootstrap.CHANNEL_CACHE.put(address,channel);
                }
            }

        }
    }

    private String getServiceName(String path){
        String[] split = path.split("/");
        return split[split.length - 1];
    }
}
