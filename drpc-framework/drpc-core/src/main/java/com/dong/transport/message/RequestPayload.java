package com.dong.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 请求消息体：描述请求的服务接口、方法、参数列表，返回值类型
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RequestPayload {

    // 接口名
    private String interfaceName;
    // 方法名
    private String methodName;

    // 参数列表：参数类型、参数值
    private Class[] parametersType;
    private Object[] parametersValue;

    // 返回值类型
    private Class returnType;
}
