package com.dong.channel.handler;

import com.dong.DrpcBootstrap;
import com.dong.ServiceConfig;
import com.dong.transport.message.DrpcRequest;
import com.dong.transport.message.RequestPayload;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class MethodCallHandler  extends SimpleChannelInboundHandler<DrpcRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DrpcRequest drpcRequest) throws Exception {
        // 1、获取负载内容
        RequestPayload requestPayload = drpcRequest.getRequestPayload();
        // 2、根据负载内容进行方法调用
        Object object = callTargetMethod(requestPayload);
        // 3、封装响应

        // 4、写出响应
        ctx.channel().writeAndFlush(object);
    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class[] parametersType = requestPayload.getParametersType();
        Object[] parametersValue = requestPayload.getParametersValue();

        // 获取暴露的服务的具体实现
        ServiceConfig<?> serviceConfig = DrpcBootstrap.SERVER_LIST.get(interfaceName);
        Object refImpl = serviceConfig.getRef();

        // 反射调用
        Object result = null;
        try {
            Class<?> aClass = refImpl.getClass();
            Method targetMethod = aClass.getMethod(methodName, parametersType);
             result = targetMethod.invoke(refImpl, parametersValue);
            return result;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.debug("调用服务【{}】的方法【{}】时发生了异常",interfaceName,methodName );
            throw new RuntimeException(e);
        }
    }
}
