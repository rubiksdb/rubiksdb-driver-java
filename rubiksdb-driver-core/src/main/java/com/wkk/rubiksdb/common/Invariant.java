package com.wkk.rubiksdb.common;

public class Invariant {
    public static void assertY(boolean cond) {
        if (!cond) {
            throw new AssertionError("!true");
        }
    }

    public static void assertN(boolean cond) {
        if (cond) {
            throw new AssertionError("true");
        }
    }

    public static boolean pow2(long x) {
        return (x & (x-1)) == 0;
    }
}
