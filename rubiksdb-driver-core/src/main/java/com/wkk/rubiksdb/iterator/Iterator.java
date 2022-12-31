package com.wkk.rubiksdb.iterator;

import com.wkk.rubiksdb.api.RubiksKK;
import com.wkk.rubiksdb.client.RubiksException;

import java.time.Instant;

public interface Iterator<T> {
    T next(RubiksKK kk) throws RubiksException;

    default Instant deadline() {
        return Instant.now().plusMillis(1000);
    }
}
