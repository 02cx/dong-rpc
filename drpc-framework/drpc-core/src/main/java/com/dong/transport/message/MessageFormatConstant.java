package com.dong.transport.message;

import java.nio.charset.Charset;

public class MessageFormatConstant {

    public static final byte[] MAGIC = "drpc".getBytes(Charset.defaultCharset());
    public static final byte VERSION = 1;
    // 头部信息长度
    public static final short HEADER_LENGTH = (short) (MAGIC.length + 18);

    public static final int MAX_FRAME_LENGTH = 1024 * 1024;
    public static final int VERSION_LENGTH = 1;
    // 描述头部信息长度的长度
    public static final int HEADER_LENGTH_LENGTH = 2;
    // 总长度占用的字节
    public static final int FULL_LENGTH_LENGTH = 4;
}
