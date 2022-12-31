package com.wkk.rubiksdb.orm;

import com.wkk.rubiksdb.api.Pair;
import com.wkk.rubiksdb.api.RubiksApi;
import com.wkk.rubiksdb.api.RubiksKK;
import com.wkk.rubiksdb.api.RubiksVV;
import com.wkk.rubiksdb.client.RubiksException;
import com.wkk.rubiksdb.client.RubiksI;
import com.wkk.rubiksdb.common.Invariant;
import com.wkk.rubiksdb.iterator.Forward2;
import com.wkk.rubiksdb.iterator.Iterator;
import lombok.AllArgsConstructor;

import java.util.function.Consumer;

@AllArgsConstructor
public class RubiksOrmImpl implements RubiksOrm, RubiksApi {
    private final RubiksI rubiks;

    @Override
    public void get(Entity... entities) throws RubiksException {
        RubiksVV[] vvs = rubiks.RPCGet(primaryKKOf(entities), deadline());
        Invariant.assertY(vvs.length == entities.length);

        for (int i = 0; i < entities.length; ++i) {
            map(vvs[i], entities[i]);
        }
    }

    @Override
    public void confirm(Entity... entities) throws RubiksException {
        rubiks.RPCConfirm(
                primaryKKOf(entities),
                confirmVVOf(entities),
                deadline());
    }

    @Override
    public void commit(Entity... entities) throws RubiksException {
        RubiksKK[] kks = commitKKOf(entities);
        RubiksVV[] vvs = commitVVOf(entities);

        if (kks.length > MAX_NPAIRS) {
            throw RubiksException.of(RUBIKS_INVAL);
        }
        Invariant.assertY(kks.length == vvs.length);

        rubiks.RPCCommit(kks, vvs, deadline());

        for (int i = 0; i < entities.length; ++i) {
            Invariant.assertY(entities[i].present == vvs[i].present);
            // present bit matches and carry new seqnum
            entities[i].seqnum = vvs[i].seqnum;
        }
    }

    @Override
    public void listBy(Entity entity, long index,
                       Consumer<Entity> consumer) throws RubiksException {
        Iterator<Pair> itr = new Forward2(rubiks);
        Pair result = itr.next(Bootstrap.indexKKOf(entity, index));

        while (result != null) {
            map(result.vv, entity);
            result = itr.next(result.kk);
        }
    }
}
