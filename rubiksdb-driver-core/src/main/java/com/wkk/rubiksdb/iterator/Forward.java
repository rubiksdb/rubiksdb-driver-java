package com.wkk.rubiksdb.iterator;

import com.wkk.rubiksdb.api.Pair;
import com.wkk.rubiksdb.api.RubiksApi;
import com.wkk.rubiksdb.api.RubiksKK;
import com.wkk.rubiksdb.client.RubiksException;
import com.wkk.rubiksdb.client.RubiksI;
import com.wkk.rubiksdb.common.Invariant;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class Forward implements Iterator<RubiksKK>, RubiksApi {
    private final Map<RubiksKK, RubiksKK> map = new HashMap<>();
    private final RubiksI rubiks;

    @Override
    public RubiksKK next(RubiksKK kk) throws RubiksException {
        if (map.containsKey(kk)) {
            return map.get(kk);
        }
        map.clear();

        try {
            Pair[] results = rubiks.RPCIterate(kk, MAX_NPAIRS, 0, deadline());
            Invariant.assertY(results.length > 0);    // otherwise exception throws

            for (int i = 0; i < results.length - 1; ++i) {
                map.put(results[i].kk, results[i + 1].kk);
            }
            return results[0].kk;
        } catch (RubiksException exception) {
            if (exception.what == RUBIKS_NONEXT) {
                return null;
            }
            throw exception;
        }
    }
}
