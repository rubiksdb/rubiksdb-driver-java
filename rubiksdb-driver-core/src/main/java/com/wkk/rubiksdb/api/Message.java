package com.wkk.rubiksdb.api;

import com.wkk.rubiksdb.common.Crc128;
import com.wkk.rubiksdb.common.Blob;
import com.wkk.rubiksdb.common.Nbuf;

public class Message implements RubiksApi {
    public final Nbuf hdr = new Nbuf();
    public final Nbuf msg = new Nbuf();
    public Blob payload = PAYLOAD_ZERO;

    public void reset() {
        this.hdr.reset();
        this.msg.reset();
        this.payload = PAYLOAD_ZERO;
    }

    public void put(int tag, long val) {
        this.msg.put(tag, val);
    }

    public void init(long kind, int npairs, Blob payload) {
        this.hdr.reset();
        this.msg.reset();

        this.msg.put(TAG_KIND, kind);
        this.msg.put(TAG_NPAIRS, npairs);

        if (payload != PAYLOAD_ZERO) {
            this.msg.put(TAG_PAYLOAD_CRC, payload.crc128.v0);
            this.msg.put(TAG_PAYLOAD_CRC+1, payload.crc128.v1);
        }
        this.payload = payload;
    }

    public long get(int tag) {
        return msg.get(tag);
    }

    public Crc128 getCrc128() {
        return Crc128.of(msg.get(TAG_PAYLOAD_CRC), msg.get(TAG_PAYLOAD_CRC+1));
    }
}
