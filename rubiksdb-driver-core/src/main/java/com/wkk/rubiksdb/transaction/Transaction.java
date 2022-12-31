package com.wkk.rubiksdb.transaction;

import com.wkk.rubiksdb.api.RubiksApi;
import com.wkk.rubiksdb.api.RubiksKK;
import com.wkk.rubiksdb.api.RubiksVV;
import com.wkk.rubiksdb.client.RubiksException;
import com.wkk.rubiksdb.client.RubiksI;
import com.wkk.rubiksdb.common.Invariant;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
public class Transaction implements AutoCloseable, RubiksApi {
    private final Map<RubiksKK, RubiksVV> map = new HashMap<>();

    private final RubiksI rubiks;
    private boolean aborted;

    @Override
    public void close() throws Exception {
        // commit
        if (!aborted && map.size() > 0) {
            Invariant.assertY(map.size() <= MAX_NPAIRS);

            Set<Map.Entry<RubiksKK, RubiksVV>> set = map.entrySet();
            RubiksKK[] kks = set.stream()
                    .map(Map.Entry::getKey).toArray(RubiksKK[]::new);
            RubiksVV[] vvs = set.stream()
                    .map(Map.Entry::getValue).toArray(RubiksVV[]::new);

            rubiks.RPCCommit(kks, vvs, deadline());
        }
    }

    public RubiksVV get(RubiksKK kk) throws RubiksException {
        if (map.containsKey(kk)) {
            return map.get(kk);
        }

        try {
            RubiksVV[] result = rubiks.RPCGet(new RubiksKK[]{kk}, deadline());
            map.put(kk, result[0]);
            return result[0];
        } catch (RubiksException exception) {
            abort();
            throw exception;
        }
    }

    public RubiksVV[] get(RubiksKK[] kks) throws RubiksException {
        boolean miss = Arrays.stream(kks).anyMatch(kk -> !map.containsKey(kk));

        if (miss) {
            try {
                RubiksVV[] result = rubiks.RPCGet(kks, deadline());
                Invariant.assertY(kks.length == result.length);

                for (int i = 0; i < kks.length; ++i) {
                    map.put(kks[i], result[i]);
                }
                return result;
            } catch (RubiksException exception) {
                abort();
                throw exception;
            }
        }
        return Arrays.stream(kks).map(map::get).toArray(RubiksVV[]::new);
    }

    public void confirm() throws RubiksException {
        if (map.size() > 0) {
            Invariant.assertY(map.size() <= MAX_NPAIRS);

            Set<Map.Entry<RubiksKK, RubiksVV>> set = map.entrySet();
            RubiksKK[] kks = set.stream()
                    .map(Map.Entry::getKey).toArray(RubiksKK[]::new);
            RubiksVV[] vvs = set.stream()
                    .map(Map.Entry::getValue).toArray(RubiksVV[]::new);

            try {
                rubiks.RPCConfirm(kks, vvs, deadline());
            } catch (RubiksException exception) {
                abort();
                throw exception;
            }
        }
    }

    public void abort() {
        Invariant.assertN(aborted);
        aborted = true;
    }

    private static Instant deadline() {
        return Instant.now().plusMillis(1000);
    }
}
