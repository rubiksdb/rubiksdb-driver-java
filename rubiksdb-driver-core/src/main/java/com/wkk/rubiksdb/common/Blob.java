package com.wkk.rubiksdb.common;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Blob {
    public final Crc128 crc128;
    public final byte[] data;
    public final int    i0, i1;

    public int length() {
        return i1 - i0;
    }

    public static Blob seal(Slice slice, Crc128 crc0) {
        return new Blob(
                Crc128.update(crc0, slice.data, slice.i0, slice.i1),
                slice.data,
                slice.i0,
                slice.i1);
    }
}
