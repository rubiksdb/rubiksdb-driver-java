package com.wkk.rubiksdb.api;

import lombok.AllArgsConstructor;
import com.wkk.rubiksdb.common.Slice;

import static java.lang.String.format;

@AllArgsConstructor
public class RubiksKK {
    public long  table;
    public Slice key;

    @Override
    public String toString() {
        return format("%d,fixme", table);
    }

    public static boolean neq(RubiksKK kk1, RubiksKK kk2) {
        return kk1.table != kk2.table
                || Slice.neq(kk1.key, kk2.key);
    }
}
