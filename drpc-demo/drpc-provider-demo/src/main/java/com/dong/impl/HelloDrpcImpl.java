package com.dong.impl;

import com.dong.HelloDrpc;

public class HelloDrpcImpl implements HelloDrpc {
    @Override
    public void sayHi(String content) {
        System.out.println(content);
    }
}
