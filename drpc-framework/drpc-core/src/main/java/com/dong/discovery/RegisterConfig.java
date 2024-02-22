package com.dong.discovery;

import com.dong.Constant;
import com.dong.discovery.impl.NacosRegister;
import com.dong.discovery.impl.ZookeeperRegister;
import com.dong.exceptions.DiscoveryException;

public class RegisterConfig {

    // 连接地址    zookeeper://192.168.183.130:2181   redis://192.168.183.130:3306
    private String connectURL;

    public RegisterConfig(String connectURL) {
        this.connectURL = connectURL;
    }

    /**
     *  简单工厂
     * @return
     */
    public Register getRegister() {
        // 1.获取注册中心类型
        String registerType = getRegisterType(connectURL,true).toLowerCase().trim();
        if(registerType.equals("zookeeper")){
            String host = getRegisterType(connectURL, false).toLowerCase().trim();
            return new ZookeeperRegister(host, Constant.TIME_OUT);
        }else if(registerType.equals("nacos")){
            String host = getRegisterType(connectURL, false).toLowerCase().trim();
            return new NacosRegister(host, Constant.TIME_OUT);
        }

        throw new DiscoveryException("未发现合适的注册中心");



    }

    public String getRegisterType(String url,boolean isType){
        String[] typeAndHost = url.split("://");
        if(typeAndHost.length != 2){
            throw new RuntimeException("给定的注册中心连接地址不合法");
        }

        if(isType){
            return typeAndHost[0];
        }else{
            return typeAndHost[1];
        }


    }
}
