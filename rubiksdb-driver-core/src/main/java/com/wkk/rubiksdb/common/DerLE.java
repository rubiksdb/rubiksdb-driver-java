package com.wkk.rubiksdb.common;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DerLE {
    private final byte[] src;
    private final int i0, i1;

    private int ii;

    public static DerLE of(byte[] src, int len) {
        return new DerLE(src, 0, len, 0);
    }

    public static DerLE of(Blob blob) {
        return new DerLE(blob.data, blob.i0, blob.i1, blob.i0);
    }

    public static DerLE of(byte[] src) {
        return new DerLE(src, 0, src.length, 0);
    }

    public static DerLE of(Slice slice) {
        return new DerLE(slice.data, slice.i0, slice.i1, slice.i0);
    }

    public static DerLE of(Slice slice, int len) {
        Invariant.assertY(len <= slice.length());
        return new DerLE(slice.data, slice.i0, slice.i0 + len, slice.i0);
    }

    public int length() {
        return i1 - i0;
    }

    public int remain() {
        return i1 - ii;
    }

    public byte[] getData() {
        return src;
    }

    public int skip(int n) {
        ii += n;
        return ii - n;
    }

    public int position() {
        return ii;
    }

    public long get1() {
        Invariant.assertY(ii + 1 <= i1);
        return (long) src[ii++] & 0xFF;
    }

    public long get2() {
        Invariant.assertY(ii + 2 <= i1);
        return ((long) src[ii++] & 0xFF)
                | ((long) src[ii++] & 0xFF) << 8
                ;
    }

    public long get3() {
        Invariant.assertY(ii + 3 <= i1);
        return ((long) src[ii++] & 0x0FF)
                | ((long) src[ii++] & 0xFF) << 8
                | ((long) src[ii++] & 0xFF) << 16
                ;
    }

    public long get8() {
        Invariant.assertY(ii + 8 <= i1);
        return ((long) src[ii++] & 0xFF)
                | ((long) src[ii++] & 0xFF) << 8
                | ((long) src[ii++] & 0xFF) << 16
                | ((long) src[ii++] & 0xFF) << 24
                | ((long) src[ii++] & 0xFF) << 32
                | ((long) src[ii++] & 0xFF) << 40
                | ((long) src[ii++] & 0xFF) << 48
                | ((long) src[ii++] & 0xFF) << 56
                ;
    }

    public long getN(int n) {
        Invariant.assertY(ii + n <= i1);

        long result = 0;
        for (int i = 0; i < n; ++i) {
            result |= ((long) src[ii++] & 0xFF) << (i * 8);
        }
        return result;
    }

    public Slice slice(int len) {
        Invariant.assertY(len <= length());
        int i1 = position() + len;
        return new Slice(src, skip(len), i1);
    }
}
