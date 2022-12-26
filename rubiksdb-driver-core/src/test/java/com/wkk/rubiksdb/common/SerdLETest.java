package com.wkk.rubiksdb.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Random;

public class SerdLETest {
    private static final Random RANDOM = new SecureRandom();

    @Test
    public void test0() {
        byte[] data = new byte[128];
        SerLE ser = SerLE.of(data);

        ser.put1(0x12L);
        ser.put2(0x1234L);
        ser.put3(0x123456L);
        ser.put8(0x1234567887654321L);

        DerLE der = DerLE.of(ser.mark());
        Assertions.assertEquals(0x12L, der.get1());
        Assertions.assertEquals(0x1234L, der.get2());
        Assertions.assertEquals(0x123456L, der.get3());
        Assertions.assertEquals(0x1234567887654321L, der.get8());
    }

    @Test
    public void test1() {
        byte[] data = new byte[]{
                (byte) 0x31, (byte) 0x0E, (byte) 0x0E, (byte) 0xDD,
                (byte) 0x47, (byte) 0xDB, (byte) 0x6F, (byte) 0x72
        };

        Assertions.assertEquals(0x31, DerLE.of(data).get1());
        Assertions.assertEquals(0x0E0E31, DerLE.of(data).get3());
        Assertions.assertEquals(0x726FDB47DD0E0E31L, DerLE.of(data).get8());
    }
}
