package com.wkk.rubiksdb.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Crc128Test {

    @Test
    public void test0() {
        byte[] data = new byte[] {
                (byte) 0x00, (byte) 0xba, (byte) 0xf0, (byte) 0x01, (byte) 0xec, (byte) 0xcf, (byte) 0xdb, (byte) 0x1d, (byte) 0x95, (byte) 0xba,
                (byte) 0x06, (byte) 0x09, (byte) 0x58, (byte) 0xad, (byte) 0x79, (byte) 0x9a, (byte) 0x31, (byte) 0x27, (byte) 0xb3, (byte) 0x49, (byte) 0x9b,
                (byte) 0x7b, (byte) 0xfd, (byte) 0xf5, (byte) 0x8d, (byte) 0x75, (byte) 0xe5, (byte) 0xbb, (byte) 0x71, (byte) 0xea, (byte) 0x6e, (byte) 0x37,
                (byte) 0x3a, (byte) 0x96, (byte) 0x7c, (byte) 0xc5, (byte) 0xf6, (byte) 0x0b, (byte) 0x8c, (byte) 0x26, (byte) 0xd2, (byte) 0x5f, (byte) 0x06,
                (byte) 0xce, (byte) 0x64, (byte) 0x16, (byte) 0xe9, (byte) 0x53, (byte) 0x06, (byte) 0x65, (byte) 0xe9, (byte) 0x38, (byte) 0x39, (byte) 0xde
        };
        Crc128 seed = Crc128.of(0xa668877af48a11c8L, 0xf566d9ef7e137f5fL);
        Crc128 expect = Crc128.of(0xd736f6aefee102e7L,0x7be4e7fe782ce3d9L);

        Crc128 result = Crc128.update(seed, data, 0, data.length);
        Assertions.assertTrue(Crc128.eq(result, expect));
    }
}
