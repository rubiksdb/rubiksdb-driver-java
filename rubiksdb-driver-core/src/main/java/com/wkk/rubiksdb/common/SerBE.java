package com.wkk.rubiksdb.common;

import lombok.AllArgsConstructor;

import java.nio.charset.StandardCharsets;

@AllArgsConstructor
public class SerBE {
    private final byte[] dst;
    private final int i0, i1;
    private int       ii;

    public static SerBE of(byte[] dst) {
        return new SerBE(dst, 0, dst.length, 0);
    }

    public void put8(long val) {
        Invariant.assertY(ii + 8 <= i1);

        dst[ii++] = (byte) ((val >> 56) & 0xFF);
        dst[ii++] = (byte) ((val >> 48) & 0xFF);
        dst[ii++] = (byte) ((val >> 40) & 0xFF);
        dst[ii++] = (byte) ((val >> 32) & 0xFF);
        dst[ii++] = (byte) ((val >> 24) & 0xFF);
        dst[ii++] = (byte) ((val >> 16) & 0xFF);
        dst[ii++] = (byte) ((val >> 8) & 0xFF);
        dst[ii++] = (byte) (val & 0xFF);
    }

    public void put1(long val) {
        Invariant.assertY(ii + 1 <= i1);
        dst[ii++] = (byte) (val & 0xFF);
    }

    public void put2(long val) {
        Invariant.assertY(ii + 2 <= i1);
        dst[ii++] = (byte) ((val >> 8) & 0xFF);
        dst[ii++] = (byte) (val & 0xFF);
    }

    public void put(String str) {
        byte[] data = str.getBytes(StandardCharsets.UTF_8);
        Invariant.assertY(ii + data.length <= i1);

        System.arraycopy(data, 0, dst, i0, data.length);
        ii += data.length;
    }

    public Slice mark() {
        return new Slice(dst, i0, ii);
    }
}
