package com.wkk.rubiksdb.api;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Pair {
    public RubiksKK kk;
    public RubiksVV vv;

    public static Pair of(RubiksKK kk, RubiksVV vv) {
        return new Pair(kk, vv);
    }
}
