package com.wkk.rubiksdb.common;

import lombok.AllArgsConstructor;

import java.nio.charset.StandardCharsets;

@AllArgsConstructor
public class SerBE {
    private final byte[] data;
    private final int i1;
    private int       i0;
    private int       ii;

    public static SerBE of(byte[] dst) {
        return new SerBE(dst, dst.length, 0, 0);
    }

    public static SerBE of(int len) {
        return new SerBE(new byte[len], len, 0, 0);
    }

    public void put8(long val) {
        Invariant.assertY(ii + 8 <= i1);

        data[ii++] = (byte) ((val >> 56) & 0xFF);
        data[ii++] = (byte) ((val >> 48) & 0xFF);
        data[ii++] = (byte) ((val >> 40) & 0xFF);
        data[ii++] = (byte) ((val >> 32) & 0xFF);
        data[ii++] = (byte) ((val >> 24) & 0xFF);
        data[ii++] = (byte) ((val >> 16) & 0xFF);
        data[ii++] = (byte) ((val >> 8) & 0xFF);
        data[ii++] = (byte) (val & 0xFF);
    }

    public void put1(long val) {
        Invariant.assertY(ii + 1 <= i1);
        data[ii++] = (byte) (val & 0xFF);
    }

    public void put2(long val) {
        Invariant.assertY(ii + 2 <= i1);
        data[ii++] = (byte) ((val >> 8) & 0xFF);
        data[ii++] = (byte) (val & 0xFF);
    }

    public void put3(long val) {
        Invariant.assertY(ii + 3 <= i1);
        data[ii++] = (byte) ((val >> 16) & 0xFF);
        data[ii++] = (byte) ((val >> 8) & 0xFF);
        data[ii++] = (byte) (val & 0xFF);
    }

    public void put(String str) {
        byte[] src = str.getBytes(StandardCharsets.UTF_8);
        Invariant.assertY(ii + src.length <= i1);

        System.arraycopy(src, 0, data, ii, src.length);
        ii += src.length;
    }

    public void put(Slice slice) {
        Invariant.assertY(ii + slice.length() <= i1);

        System.arraycopy(slice.data, slice.i0, data, ii, slice.length());
        ii += slice.length();
    }

    public Slice flip() {
        Slice slice = new Slice(data, i0, ii);

        i0 = ii;
        return slice;
    }
}
