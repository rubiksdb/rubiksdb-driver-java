package com.wkk.rubiksdb.client;

import com.wkk.rubiksdb.api.Pair;
import com.wkk.rubiksdb.api.RubiksApi;
import com.wkk.rubiksdb.api.RubiksKK;
import com.wkk.rubiksdb.common.DerLE;
import com.wkk.rubiksdb.common.Siphash;
import com.wkk.rubiksdb.common.Perm64;

public interface RubiksCM {
    void submit(RubiksR rbr, long hint) throws RubiksException;

    void waitForCompletion(RubiksR rbr) throws RubiksException;

    default void RPC(RubiksR rbr, long hint) throws RubiksException {
        submit(rbr, hint);
        waitForCompletion(rbr);
    }

    default RubiksKK[] RPCWithKKs(RubiksR rbr, long hint) throws RubiksException {
        submit(rbr, hint);
        waitForCompletion(rbr);

        if (rbr.resp.payload == RubiksApi.PAYLOAD_ZERO) {
            throw RubiksException.of(RubiksApi.RUBIKS_EIO);
        }
        return RubiksApi.deserializeKKs(DerLE.of(rbr.resp.payload));
    }

    default Pair[] RPCWithKVs(RubiksR rbr, long hint) throws RubiksException {
        submit(rbr, hint);
        waitForCompletion(rbr);

        if (rbr.resp.payload == RubiksApi.PAYLOAD_ZERO) {
            throw RubiksException.of(RubiksApi.RUBIKS_EIO);
        }
        return RubiksApi.deserializeKVs(DerLE.of(rbr.resp.payload));
    }

    default long fineHint(RubiksKK kk) {
        if (kk.key.length() > 1) {
            return Perm64.perm(kk.table) ^
                    Siphash.of(DerLE.of(kk.key, kk.key.length() - 1), Siphash.TWEAK);
        }
        return Perm64.perm(kk.table);
    }

    default long coarseHint(RubiksKK kk) {
        if (kk.key.length() > 2) {
            return Perm64.perm(kk.table) ^
                    Siphash.of(DerLE.of(kk.key, kk.key.length() - 2), Siphash.TWEAK);
        }
        return Perm64.perm(kk.table);
    }
}
