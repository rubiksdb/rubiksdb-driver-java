package com.wkk.rubiksdb.client;

import com.wkk.rubiksdb.api.RubiksApi;
import com.wkk.rubiksdb.api.RubiksKK;
import com.wkk.rubiksdb.api.RubiksVV;
import com.wkk.rubiksdb.common.Invariant;
import com.wkk.rubiksdb.common.SerLE;
import com.wkk.rubiksdb.common.Slice;
import com.wkk.rubiksdb.api.Message;
import com.wkk.rubiksdb.common.Blob;

import java.time.Instant;

public class RubiksR implements RubiksApi {
    public Instant deadline;

    // cm
    public long    requestId;
    public Instant submitT0;
    public int     endpoint;
    public Slice   respSlice;

    public final Message req = new Message();
    public final Message resp = new Message();

    public final byte[] payload = new byte[MAX_NPAIRS * (14 + MAX_PAIR_SIZE)];
    public final byte[] serialize = new byte[SERIALIZE_SIZE];

    public void init(Instant deadline, long kind, int npairs, Blob payload) {
        this.deadline  = deadline;
        this.requestId = -1;
        this.endpoint  = -1;
        this.submitT0  = null;
        this.respSlice = Slice.ZERO;

        this.req.init(kind, npairs, payload);
        this.resp.reset();
    }

    public void initGET(RubiksKK[] kks, Instant deadline) {
        this.init(deadline, KIND_GET, kks.length, serialize(kks));
    }

    public void initCOMMIT(RubiksKK[] kks, RubiksVV[] vvs, Instant deadline) {
        this.init(deadline, KIND_COMMIT, kks.length, serialize(kks, vvs));
        long present = 0;

        for (int i = 0; i < vvs.length; ++i) {
            present |= (vvs[i].present ? 1L : 0L) << i;
            this.req.put(TAG_SEQNUM + i, vvs[i].seqnum);
        }
        this.req.put(RubiksApi.TAG_PRESENT, present);
    }

    public void initCONFIRM(RubiksKK[] kks, RubiksVV[] vvs, Instant deadline) {
        this.init(deadline, KIND_CONFIRM, kks.length, serialize(kks));

        for (int i =0; i < vvs.length; ++i) {
            this.req.put(TAG_SEQNUM + i, vvs[i].seqnum);
        }
    }

    public void initITERATE(RubiksKK kk, int npairs, long hint, Instant deadline) {
        Invariant.assertY(validIterateHint(hint));

        this.init(deadline, KIND_CONFIRM,  npairs, serialize(new RubiksKK[]{kk}));
        this.req.put(TAG_ITERATE_HINT, hint);
    }

    public void putHdr(long clientId,long allowance) {
        req.hdr.put(RubiksApi.TAG_CLIENT_ID, clientId);
        req.hdr.put(RubiksApi.TAG_REQUEST_ID, requestId);
        req.hdr.put(TAG_ALLOWANCE, allowance);
    }

    private Blob serialize(RubiksKK[] kks) {
        return Blob.seal(
                RubiksApi.serialize(SerLE.of(payload), kks),
                PAYLOAD_ZERO.crc128);
    }

    private Blob serialize(RubiksKK[] kks, RubiksVV[] vvs) {
        Invariant.assertY(kks.length == vvs.length);

        return Blob.seal(
                RubiksApi.serialize(SerLE.of(payload), kks, vvs),
                PAYLOAD_ZERO.crc128);
    }
}
