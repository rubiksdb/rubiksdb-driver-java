package com.wkk.rubiksdb.api;

import com.wkk.rubiksdb.client.RubiksException;
import com.wkk.rubiksdb.common.DerLE;
import com.wkk.rubiksdb.common.SerLE;
import com.wkk.rubiksdb.common.Blob;
import com.wkk.rubiksdb.common.Crc128;
import com.wkk.rubiksdb.common.Slice;

import java.util.ArrayList;
import java.util.List;

public interface RubiksApi {
    long WIRE_MAGIC = 0x143e;
    long PORT_DELTA = 8;

    int MAX_NPAIRS       = 8;
    int MAX_PAIR_SIZE    = 15 * 1024;
    int MAX_PAYLOAD_SIZE = 30 * 1024;

    // we don't want single page be split twice in on txn
    int MAX_COMMIT_SIZE  = MAX_PAIR_SIZE;
    int SERIALIZE_SIZE   = 1024 /*stuff*/ + MAX_PAYLOAD_SIZE;

    long ITERATE_HINT_BACK   = 0x01;
    long ITERATE_HINT_VALUE  = 0x02;
    long ITERATE_HINT_SEQNUM = 0x04;
    long ITERATE_HINT_ALL    = ITERATE_HINT_SEQNUM | ITERATE_HINT_VALUE;
    long ITERATE_HINT_MASK   = ITERATE_HINT_ALL | ITERATE_HINT_BACK;

    long SEQNUM_INF = ~0L;

    // outcomes
    int RUBIKS_OK      = 0;
    int RUBIKS_TIMEOUT = 1;
    int RUBIKS_INVAL   = 2;
    int RUBIKS_STALE   = 3;
    int RUBIKS_NONEXT  = 4;
    int RUBIKS_EIO     = 5;

    // RPC kind
    long KIND_GET     = 1;
    long KIND_COMMIT  = 2;
    long KIND_CONFIRM = 3;
    long KIND_ITERATE = 4;

    // tags
    int TAG_KIND         = 0;
    int TAG_NPAIRS       = 1;
    int TAG_PAYLOAD_CRC  = 2;
    int TAG_OUTCOME      = 4;
    int TAG_PRESENT      = 5;
    int TAG_ITERATE_HINT = 6;
    int TAG_SEQNUM       = 7;

    // header
    int TAG_CLIENT_ID    = 0;
    int TAG_REQUEST_ID   = 1;
    int TAG_ALLOWANCE    = 2;

    Crc128 PAYLOAD_CRC = Crc128.of(0xe8a8918ad6ebdce4L, 0x60457c9dceee5effL);
    Blob PAYLOAD_ZERO = new Blob(PAYLOAD_CRC, new byte[]{}, 0, 0);

    static Slice serialize(SerLE ser, RubiksKK[] kks) {
        for (RubiksKK kk : kks) {
            ser.put8(kk.table);
            ser.put3(kk.key.length());
            ser.put (kk.key);
        }
        return ser.mark();
    }

    static Slice serialize(SerLE ser, RubiksKK[] kks, RubiksVV[] vvs) {
        for (int i = 0; i < kks.length; ++i) {
            RubiksKK kk = kks[i];
            RubiksVV vv = vvs[i];

            ser.put8(kk.table);
            ser.put3(kk.key.length());
            ser.put3(vv.val.length());
            ser.put (kk.key);
            ser.put (vv.val);
        }
        return ser.mark();
    }

    static RubiksKK[] deserializeKKs(DerLE der) throws RubiksException {
        List<RubiksKK> result = new ArrayList<>();

        while (der.remain() != 0) {
            if (der.remain() < 8 + 3) {
                throw RubiksException.of(RUBIKS_EIO);
            }
            long table = der.get8();
            int len = (int) der.get3();

            if (der.remain() < len) {
                throw RubiksException.of(RUBIKS_EIO);
            }
            result.add(new RubiksKK(table, der.slice(len)));
        }
        return result.toArray(new RubiksKK[0]);
    }

    static Pair[] deserializeKVs(DerLE der) throws RubiksException {
        List<Pair> result = new ArrayList<>();
        final boolean present = false;  // filled in by caller
        final long seqnum = 0;          // filled in by caller

        while (der.remain() != 0) {
            if (der.remain() < 8 + 3 + 3) {
                throw RubiksException.of(RUBIKS_EIO);
            }

            long table = der.get8();
            int klen = (int) der.get3();
            int vlen = (int) der.get3();

            if (der.remain() < klen + vlen) {
                throw RubiksException.of(RUBIKS_EIO);
            }

            result.add(
                    Pair.of(
                            new RubiksKK(table, der.slice(klen)),
                            new RubiksVV(present, seqnum, der.slice(vlen))));
        }
        return result.toArray(new Pair[0]);
    }

    default boolean validIterateHint(long hint) {
        return (hint & ~ITERATE_HINT_MASK) == 0;
    }
}
