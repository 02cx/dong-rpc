package com.dong.serialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SerializerWrapper {

    private byte code;
    private String type;
    private Serializer serializer;
}
