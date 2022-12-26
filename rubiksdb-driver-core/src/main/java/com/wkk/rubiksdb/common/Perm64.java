package com.wkk.rubiksdb.common;

public class Perm64 {
    private static final long FNV_OFFSET        = 0xcbf29ce484222325L;
    private static final long FNV_PRIME         = 0x100000001b3L;
    private static final long FNV_PRIME_INVERSE = 0xce965057aff6957bL;

    public static long perm(long x) {
        x += FNV_OFFSET;
        x *= FNV_PRIME;
        x ^= x >> 24;
        x *= FNV_PRIME;
        x ^= x >> 14;
        x *= FNV_PRIME;
        x ^= x >> 28;
        return x;
    }

    public static long merp(long x) {
        x  = step(x, 28);
        x *= FNV_PRIME_INVERSE;
        x  = step(x, 14);
        x *= FNV_PRIME_INVERSE;
        x  = step(x, 24);
        x *= FNV_PRIME_INVERSE;
        x -= FNV_OFFSET;
        return x;
    }

    public static long step(long x, int n) {
        long t = x;
        for (int i = n; i < 64; i += n) {
            t = x ^ (t >> n);
        }
        return t;
    }
}
