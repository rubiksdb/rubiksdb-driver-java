package com.wkk.rubiksdb.common;

import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public class Slice {
    public final byte[] data;
    public final int i0, i1;

    public static final Slice ZERO = new Slice(null, 0, 0);

    public static Slice of(byte[] data, int len) {
        return new Slice(data, 0, len);
    }

    public static Slice of(byte[] data) {
        return new Slice(data, 0, data.length);
    }

    public static Slice of(Blob blob) {
        return new Slice(blob.data, blob.i0, blob.i1);
    }

    public static boolean neq(Slice s1, Slice s2) {
        return s1.length() != s2.length()
                || Arrays.compare(s1.data, s1.i0, s1.i1, s2.data, s2.i0, s2.i1) != 0
                ;
    }

    public static boolean eq(Slice s1, Slice s2) {
        return s1.length() == s2.length()
                && Arrays.compare(s1.data, s1.i0, s1.i1, s2.data, s2.i0, s2.i1) == 0
                ;
    }

    public int length() {
        return i1 - i0;
    }
}
