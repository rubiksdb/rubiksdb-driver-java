package com.wkk.rubiksdb.client;

import com.wkk.rubiksdb.api.Pair;
import com.wkk.rubiksdb.api.RubiksApi;
import com.wkk.rubiksdb.api.RubiksKK;
import com.wkk.rubiksdb.api.RubiksVV;
import com.wkk.rubiksdb.common.Invariant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Arrays;
import java.util.stream.IntStream;

@AllArgsConstructor
@Slf4j
public class RubiksClient implements RubiksI {
    private final RubiksCM cm;
    private final Retry retry;

    @Override
    public RubiksVV[] RPCGet(RubiksKK[] kks,
                             Instant deadline) throws RubiksException {
        RubiksR rbr = new RubiksR();
        rbr.initGET(kks, deadline);

        Pair[] result = retry.process2(
                () -> cm.RPCWithKVs(rbr, cm.coarseHint(kks[0])), deadline);

        final int npairs = (int) rbr.resp.get(RubiksApi.TAG_NPAIRS);
        final long present = rbr.resp.get(RubiksApi.TAG_PRESENT);

        invariant(kks, result, npairs);

        for (int i = 0; i < npairs; ++i) {
            result[i].vv.present = ((1L << i) & present) != 0;
            result[i].vv.seqnum = rbr.resp.get(RubiksApi.TAG_SEQNUM + i);
        }
        return Arrays.stream(result).map(pair -> pair.vv).toArray(RubiksVV[]::new);
    }

    @Override
    public void RPCCommit(RubiksKK[] kks, RubiksVV[] vvs,
                          Instant deadline) throws RubiksException {
        RubiksR rbr = new RubiksR();
        rbr.initCOMMIT(kks, vvs, deadline);

        RubiksKK[] result = retry.process0(() ->
            cm.RPCWithKKs(rbr, cm.coarseHint(kks[0])), deadline);

        final int npairs = (int) rbr.resp.get(RubiksApi.TAG_NPAIRS);
        final long present = rbr.resp.get(RubiksApi.TAG_PRESENT);

        invariant(kks, result, npairs);

        for (int i = 0; i < npairs; ++i) {
            Invariant.assertY(vvs[i].present == ((present & (1L << i)) != 0));
            vvs[i].seqnum = rbr.resp.get(RubiksApi.TAG_SEQNUM + i);
        }
    }

    @Override
    public void RPCConfirm(RubiksKK[] kks, RubiksVV[] vvs,
                           Instant deadline) throws RubiksException {
        RubiksR rbr = new RubiksR();
        rbr.initCONFIRM(kks, vvs, deadline);

        retry.process3(() -> {
            cm.RPC(rbr, cm.coarseHint(kks[0]));
            return null;
        }, deadline);
    }

    @Override
    public Pair[] RPCIterate(RubiksKK cursor, int npairs, long hint,
                             Instant deadline) throws RubiksException {
        RubiksR rbr = new RubiksR();
        rbr.initITERATE(cursor, npairs, hint, deadline);

        if ((hint & RubiksApi.ITERATE_HINT_VALUE) == 0) {
            RubiksKK[] result = retry.process0(() ->
                 cm.RPCWithKKs(rbr, cm.coarseHint(cursor)), deadline);

            return Arrays.stream(result)
                    .map(kk -> Pair.of(kk, null))
                    .toArray(Pair[]::new);
        } else {
            Pair[] result = retry.process2(() ->
                    cm.RPCWithKVs(rbr, cm.coarseHint(cursor)), deadline);
            npairs = (int) rbr.resp.get(RubiksApi.TAG_NPAIRS);

            if (npairs != result.length) {
                throw RubiksException.of(RubiksApi.RUBIKS_EIO);
            }
            for (int i = 0; i < npairs; ++i) {
                result[i].vv.present = true;    // always present with iterate

                if ((hint & RubiksApi.ITERATE_HINT_SEQNUM) != 0) {
                    result[i].vv.seqnum = rbr.resp.get(RubiksApi.TAG_SEQNUM + i);
                }
            }
            return result;
        }
    }

    private void invariant(RubiksKK[] kks, Pair[] result, int npairs) throws RubiksException {
        if (kks.length != result.length || result.length != npairs ||
                IntStream.range(0, npairs).anyMatch(
                        i -> RubiksKK.neq(kks[i], result[i].kk))) {
            throw RubiksException.of(RubiksApi.RUBIKS_EIO);
        }
    }

    private void invariant(RubiksKK[] kks, RubiksKK[] result, int npairs) throws RubiksException {
        if (kks.length != result.length || result.length != npairs ||
                IntStream.range(0, npairs).anyMatch(
                        i -> RubiksKK.neq(kks[i], result[i]))) {
            throw RubiksException.of(RubiksApi.RUBIKS_EIO);
        }
    }
}
