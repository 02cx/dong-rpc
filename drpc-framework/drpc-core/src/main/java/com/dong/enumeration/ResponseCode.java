package com.dong.enumeration;

/**
 * 响应状态码
 */
public enum ResponseCode {
    SUCCESS((byte)1,"成功"),FAIL((byte)2,"失败");

    private byte code;
    private String desc;

    public byte getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    ResponseCode(byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
