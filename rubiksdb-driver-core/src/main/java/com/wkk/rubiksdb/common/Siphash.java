package com.wkk.rubiksdb.common;

import lombok.AllArgsConstructor;

public class Siphash {
    public static final Tweak TWEAK =
            new Tweak(0x967af6cfdf9eee0dL, 0x65d44548a2b4df17L);

    public static long of(DerLE le, Tweak tweak) {
        Tuple tup = new Tuple(
                0x736F6D6570736575L ^ tweak.T0,
                0x646F72616E646F6DL ^ tweak.T1,
                0x6C7967656E657261L ^ tweak.T0,
                0x7465646279746573L ^ tweak.T1);
        long b = ((long) le.length()) << 56;

        while (le.remain() >= 8) {
            long x = le.get8();
            tup.v3 ^= x;
            tup.round();
            tup.round();
            tup.v0 ^= x;
        }

        if (le.remain() > 0) {
            b |= le.getN(le.remain());
        }

        tup.v3 ^= b;
        tup.round();
        tup.round();
        tup.v0 ^= b;

        tup.v2 ^= 0xFF;
        tup.round();
        tup.round();
        tup.round();
        tup.round();

        return tup.v0 ^ tup.v1 ^ tup.v2 ^ tup.v3;
    }

    private static long rotl64(long v, long n) {
        assert 0 < n && n < 64;
        return (v << n) | (v >>> (64 - n));
    }

    @AllArgsConstructor
    public static class Tweak {
        long T0, T1;
    }

    @AllArgsConstructor
    static class Tuple {
        long v0, v1, v2, v3;

        void round() {
            v0 ^= v1;
            v1 = rotl64(v1, 13);
            v1 ^= v0;
            v0 = rotl64(v0, 32);
            v2 ^= v3;
            v3 = rotl64(v3, 16);
            v3 ^= v2;
            v0 ^= v3;
            v3 = rotl64(v3, 21);
            v3 ^= v0;
            v2 ^= v1;
            v1 = rotl64(v1, 17);
            v1 ^= v2;
            v2 = rotl64(v2, 32);
        }
    }
}
