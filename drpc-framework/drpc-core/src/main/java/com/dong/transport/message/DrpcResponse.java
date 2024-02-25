package com.dong.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务消费端发起的请求内容
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DrpcResponse {

    // 请求id
    private Long requestId;

    // 响应码
    private byte code;
    private byte compressType; // 压缩类型
    private byte serializeType; // 序列化方式

    // 响应消息体
    private Object body;
}
