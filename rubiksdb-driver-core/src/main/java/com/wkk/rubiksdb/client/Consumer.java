package com.wkk.rubiksdb.client;

import com.wkk.rubiksdb.common.Nbuf;
import com.wkk.rubiksdb.common.Slice;

@FunctionalInterface
public interface Consumer {
    int accept(Slice slice, Nbuf hdr);
}
