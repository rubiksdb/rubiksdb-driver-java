package com.wkk.rubiksdb.client;

public interface Supplier<T> {
    T get() throws RubiksException;
}
