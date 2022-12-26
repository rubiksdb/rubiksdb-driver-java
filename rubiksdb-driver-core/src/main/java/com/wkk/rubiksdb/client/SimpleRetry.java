package com.wkk.rubiksdb.client;

import com.wkk.rubiksdb.api.Pair;
import com.wkk.rubiksdb.api.RubiksKK;
import com.wkk.rubiksdb.api.RubiksVV;

import java.time.Instant;

public class SimpleRetry implements Retry {
    private static final int COUNT = 5;

    @Override
    public RubiksKK[] process0(Supplier<RubiksKK[]> supplier,
                               Instant deadline) throws RubiksException {
        return new Getter<RubiksKK[]>().fn(supplier, deadline);
    }

    @Override
    public RubiksVV[] process1(Supplier<RubiksVV[]> supplier,
                               Instant deadline) throws RubiksException {
        return new Getter<RubiksVV[]>().fn(supplier, deadline);
    }

    @Override
    public Pair[] process2(Supplier<Pair[]> supplier,
                           Instant deadline) throws RubiksException {
        return new Getter<Pair[]>().fn(supplier, deadline);
    }

    @Override
    public void process3(Supplier<Void> supplier, Instant deadline) throws RubiksException {
        new Getter<Void>().fn(supplier, deadline);
    }

    static boolean due(Instant deadline) {
        return Instant.now().plusMillis(1).isAfter(deadline);
    }

    class Getter<R> {
        R fn(Supplier<R> supplier, Instant deadline) throws RubiksException {
            RubiksException last = null;

            for (int i = 0; i < COUNT; ++i) {
                try {
                    return supplier.get();
                } catch (RubiksException exception) {
                    last = exception;

                    if (!retryable(exception) || due(deadline)) {
                        throw exception;
                    }
                }
            }
            throw last;
        }
    }
}
