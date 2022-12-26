package com.wkk.rubiksdb.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SiphashTest {
    @Test
    public void test0() {
        Siphash.Tweak tweak = new Siphash.Tweak(0x0706050403020100L, 0x0f0e0d0c0b0a0908L);
        long[] expected = {
                0x2231a79b14d64fc1L,
                0x47ac8edd63640fa1L,
                0xc04d82a5bbd2aa9cL,
        };
        byte[] data = new byte[] {0, 1, 2};

        for (int i = 0; i < expected.length; ++i) {
            Assertions.assertEquals(
                    expected[i],
                    Siphash.of(DerLE.of(data, i), tweak));
        }
    }
}
