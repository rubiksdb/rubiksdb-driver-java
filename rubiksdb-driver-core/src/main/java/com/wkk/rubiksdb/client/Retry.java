package com.wkk.rubiksdb.client;

import com.wkk.rubiksdb.api.Pair;
import com.wkk.rubiksdb.api.RubiksApi;
import com.wkk.rubiksdb.api.RubiksKK;
import com.wkk.rubiksdb.api.RubiksVV;

import java.time.Instant;

public interface Retry {
    RubiksKK[] process0(Supplier<RubiksKK[]> supplier, Instant deadline) throws RubiksException;

    RubiksVV[] process1(Supplier<RubiksVV[]> supplier, Instant deadline) throws RubiksException;

    Pair[] process2(Supplier<Pair[]> supplier, Instant deadline) throws RubiksException;

    void process3(Supplier<Void> supplier, Instant deadline) throws RubiksException;

    default boolean retryable(RubiksException exception) {
        return exception.what == RubiksApi.RUBIKS_EIO;
    }
}
