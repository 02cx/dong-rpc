package com.dong.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

@Slf4j
public class NetUtils {

    // 获取ip地址
    public static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                // 过滤回环、虚拟、断开的网卡
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                }

                // 过滤虚拟机网卡
                if (netInterface.getDisplayName().contains("Virtual")) {
                    continue;
                }

                // 过滤VPN
                if (netInterface.getDisplayName().contains("VPN")) {
                    continue;
                }

                // 获取真实可用的网卡
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement();
                    if (ip != null && ip instanceof Inet4Address) { // 必须是IPv4
                        return ip.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            log.error("IP地址获取失败", e);
            e.printStackTrace();
        }
        return "";
    }


   /* public static void main(String[] args) {
        String ipAddress = NetUtils.getIpAddress();
        System.out.println(ipAddress);
    }*/
}
