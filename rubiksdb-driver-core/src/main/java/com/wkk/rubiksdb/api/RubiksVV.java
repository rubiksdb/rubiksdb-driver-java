package com.wkk.rubiksdb.api;

import lombok.AllArgsConstructor;
import com.wkk.rubiksdb.common.Slice;

import static java.lang.String.format;

@AllArgsConstructor
public class RubiksVV {
    public boolean present;
    public long    seqnum;
    public Slice   val;

    @Override
    public String toString() {
        return format("present=%d,seqnum=%d,fixme", present ? 1 : 0, seqnum);
    }
}
