package com.dong.compress;

import com.dong.serialize.Serializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CompressorWrapper {

    private byte code;
    private String type;
    private Compressor compressor;
}
