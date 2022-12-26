package com.wkk.rubiksdb.common;

public class Nbuf {
    public long   mask = 0;
    public long[] vals = new long[63];

    public long get(int tag) {
        assert has(tag);
        return vals[tag];
    }

    public void put(int tag, long v) {
        assert !has(tag);
        mask |= 1L << tag;
        vals[tag] = v;
    }

    public boolean has(int tag) {
        return ((1L << tag) & mask) == (1L << tag);
    }

    public boolean have(long mask) {
        return (this.mask & mask) == mask;
    }

    public void reset() {
        this.mask = 0;
    }

    public static void serialize(SerLE le, Nbuf nbuf) {
        le.put8(nbuf.mask);

        for (int i = 0; i < 63; ++i) {
            if (nbuf.has(i)) {
                le.put8(nbuf.vals[i]);
            }
        }
    }

    public static void deserialize(DerLE le, Nbuf nbuf) {
        nbuf.mask = le.get8();
        for (int i = 0; i < 63; ++i) {
            nbuf.vals[i] = nbuf.has(i) ? le.get8() : 0;
        }
    }

    public static void skip(DerLE le) {
        long mask = le.get8();
        for (int i = 0; i < 63; ++i) {
            if (((1L << i) & mask) == 1L << i) {
                le.skip(8);
            }
        }
    }
}
