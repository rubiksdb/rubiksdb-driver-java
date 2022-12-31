package com.wkk.rubiksdb.api;

import com.wkk.rubiksdb.common.DerLE;
import com.wkk.rubiksdb.common.Invariant;
import com.wkk.rubiksdb.common.Perm64;
import com.wkk.rubiksdb.common.Siphash;
import lombok.AllArgsConstructor;
import com.wkk.rubiksdb.common.Slice;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import static java.lang.String.format;

@AllArgsConstructor
public class RubiksKK {
    public long  table;
    public Slice key;

    @Override
    public String toString() {
        return format("table=%d,len=%d", table, key.length());
    }

    @Override
    public boolean equals(Object other) {
        Invariant.assertY(other instanceof RubiksKK);
        RubiksKK otherKK = (RubiksKK) other;

        return this == otherKK ||
                (table == otherKK.table && Slice.eq(key, otherKK.key));
    }

    @Override
    public int hashCode() {
        long hash = Perm64.perm(table)
                ^ Siphash.of(DerLE.of(key), Siphash.TWEAK);
        return (int) ((hash >>> 32) ^ hash);
    }

    public static boolean neq(RubiksKK kk1, RubiksKK kk2) {
        return kk1.table != kk2.table || Slice.neq(kk1.key, kk2.key);
    }
}
