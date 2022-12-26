package com.wkk.rubiksdb.common;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SerLE {
    private final byte[] dst;
    private final int i0, i1;
    private int       ii;

    public static SerLE of(byte[] dst) {
        return new SerLE(dst, 0, dst.length, 0);
    }

    public void put8(long val) {
        Invariant.assertY(ii + 8 <= i1);

        dst[ii++] = (byte) (val & 0xFF);
        dst[ii++] = (byte) ((val >> 8) & 0xFF);
        dst[ii++] = (byte) ((val >> 16) & 0xFF);
        dst[ii++] = (byte) ((val >> 24) & 0xFF);
        dst[ii++] = (byte) ((val >> 32) & 0xFF);
        dst[ii++] = (byte) ((val >> 40) & 0xFF);
        dst[ii++] = (byte) ((val >> 48) & 0xFF);
        dst[ii++] = (byte) ((val >> 56) & 0xFF);
    }

    public void put1(long val) {
        Invariant.assertY(ii + 1 <= i1);
        dst[ii++] = (byte) (val & 0xFF);
    }

    public void put2(long val) {
        Invariant.assertY(ii + 2 <= i1);
        dst[ii++] = (byte) (val & 0xFF);
        dst[ii++] = (byte) ((val >> 8) & 0xFF);
    }

    public void put3(long val) {
        Invariant.assertY(ii + 3 <= i1);
        dst[ii++] = (byte) (val & 0xFF);
        dst[ii++] = (byte) ((val >> 8) & 0xFF);
        dst[ii++] = (byte) ((val >> 16) & 0xFF);
    }

    public int reserve(int n) {
        Invariant.assertY(ii + n <= i1);
        ii += n;
        return ii - n;
    }

    public void put3(long val, int jj) {
        Invariant.assertY(jj + 3 < i1);
        dst[jj++] = (byte) (val & 0xFF);
        dst[jj++] = (byte) ((val >> 8) & 0xFF);
        dst[jj  ] = (byte) ((val >> 16) & 0xFF);
    }

    public void put(byte[] data) {
        Invariant.assertY(ii + data.length <= i1);
        System.arraycopy(data, 0,
                dst, ii, data.length);
        ii += data.length;
    }

    public void put(Slice slice) {
        Invariant.assertY(ii + slice.length() <= i1);
        System.arraycopy(slice.data, slice.i0,
                dst, ii, slice.length());
        ii += slice.length();
    }

    public Slice mark() {
        return new Slice(dst, i0, ii);
    }

    public int length() {
        return ii - i0;
    }
}
