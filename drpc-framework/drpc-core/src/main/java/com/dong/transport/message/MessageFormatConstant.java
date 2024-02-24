package com.dong.transport.message;

import java.nio.charset.Charset;

public class MessageFormatConstant {

    public static final byte[] MAGIC = "drpc".getBytes(Charset.defaultCharset());
    public static final byte VERSION = 1;
    public static final short HEADER_LENGTH = (short) (MAGIC.length + 18);
}
