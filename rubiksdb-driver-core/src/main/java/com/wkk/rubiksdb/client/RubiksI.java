package com.wkk.rubiksdb.client;

import com.wkk.rubiksdb.api.Pair;
import com.wkk.rubiksdb.api.RubiksKK;
import com.wkk.rubiksdb.api.RubiksVV;

import java.time.Instant;

public interface RubiksI {
    RubiksVV[] RPCGet(RubiksKK[] kks,
                      Instant deadline) throws RubiksException;

    void RPCCommit(RubiksKK[] kks, RubiksVV[] vvs,
                   Instant deadline) throws RubiksException;

    void RPCConfirm(RubiksKK[] kks, RubiksVV[] vvs,
                    Instant deadline) throws RubiksException;

    Pair[] RPCIterate(RubiksKK cursor, int npairs, long hint,
                      Instant deadline) throws RubiksException;
}
