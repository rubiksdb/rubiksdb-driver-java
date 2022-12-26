package com.wkk.rubiksdb.common;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Wire {
    private static final long EOF_MAGIC = 0x69e1;

    public static int consumable(DerLE le, long magic, Nbuf hdr) {
        if (le.length() < 2 + 3) {
            return 0;
        }

        if (le.get2() != magic) {
            return -1;
        }

        long len = le.get3();
        if (2 + 3 + le.length() < len) {
            return 0;
        }

        le.skip(2);
        Nbuf.deserialize(le, hdr);
        return (int) len;
    }

    public static Slice serialize(SerLE le, long magic,
                                  Nbuf[] nbufs, Slice[] slices) {
        le.put2(magic);
        int lenI = le.reserve(3);
        le.put1(nbufs.length);
        le.put1(countNonEmptySlices(slices));

        for (Nbuf nbuf : nbufs) {
            Nbuf.serialize(le, nbuf);
        }

        for (int i = 0, j = 0; i < slices.length; ++i) {
            Slice s = slices[i];

            if (s.length() != 0) {
                le.put1(j++);   // index
                le.put3(s.length());
                le.put(s);
            }
        }

        le.put2(EOF_MAGIC);
        le.put3(le.length(), lenI);
        return le.mark();
    }

    public static void deserialize(DerLE le, Nbuf[] nbufs, Slice[] slices) {
        le.skip(2 + 3); // magic + len
        int nnb = (int) le.get1();
        int ncb = (int) le.get1();

        Invariant.assertY(nnb == nbufs.length);
        Invariant.assertY(ncb <= slices.length);

        for (Nbuf nb : nbufs) {
            Nbuf.deserialize(le, nb);
        }

        for (int i = 0; i < ncb; ++i) {
            final int index = (int) le.get1();
            final int len = (int) le.get3();
            final int pos = le.position();

            slices[index] = new Slice(le.getData(), pos, pos + len);
            le.skip(len);
        }

        le.skip(2); // end magic
        Invariant.assertY(le.remain() == 0);
    }

    private static int countNonEmptySlices(Slice[] slices) {
        int count = 0;

        for (Slice s : slices) {
            count += s.length() == 0 ? 0 : 1;
        }
        return count;
    }
}
