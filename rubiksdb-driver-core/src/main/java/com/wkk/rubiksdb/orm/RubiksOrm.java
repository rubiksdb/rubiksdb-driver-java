package com.wkk.rubiksdb.orm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkk.rubiksdb.api.RubiksApi;
import com.wkk.rubiksdb.api.RubiksKK;
import com.wkk.rubiksdb.api.RubiksVV;
import com.wkk.rubiksdb.client.RubiksException;
import com.wkk.rubiksdb.common.SerBE;
import com.wkk.rubiksdb.common.Slice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public abstract class RubiksOrm implements RubiksApi {
    // get entity by primary key
    public abstract void get(Entity... entities) throws RubiksException;

    public abstract void confirm(Entity... entities) throws RubiksException;

    public abstract void commit(Entity... entities) throws RubiksException;

    public abstract  void listBy(Entity entity, long index,
                                 Consumer<Entity> consumer) throws RubiksException;

    protected void map(RubiksVV vv, Entity entity) throws RubiksException {
        Slice slice = vv.val;

        if (slice.length() > 0) {
            try {
                new ObjectMapper()
                        .readerForUpdating(entity)
                        .readValue(slice.data, slice.i0, slice.length());
            } catch (IOException exception) {
                throw RubiksException.of(RUBIKS_INVAL, exception);
            }
        }
        entity.present = vv.present;
        entity.seqnum = vv.seqnum;
    }

    protected RubiksKK[] primaryKKOf(SerBE ser, Entity... entities) throws RubiksException {
        List<RubiksKK> kks = new ArrayList<>();

        for (Entity entity : entities) {
            kks.add(Bootstrap.primaryKKOf(entity, ser));
        }
        return kks.toArray(RubiksKK[]::new);
    }

    protected RubiksVV primaryVVOf(Entity entity) throws RubiksException {
        // serialize the entity into json as string and then take utf8 byte array
        try {
            String str = new ObjectMapper().writeValueAsString(entity);

            if (entity.present) {
                return new RubiksVV(
                        true, entity.seqnum,
                        Slice.of(str.getBytes(StandardCharsets.UTF_8)));
            } else {
                return new RubiksVV(false, entity.seqnum, Slice.ZERO);
            }
        } catch (Exception exception) {
            throw RubiksException.of(RubiksApi.RUBIKS_INVAL, exception);
        }
    }

    protected RubiksVV[] primaryVVOf(Entity... entities) throws RubiksException {
        List<RubiksVV> vvs = new ArrayList<>();

        for (Entity entity : entities) {
            vvs.add(primaryVVOf(entity));
        }
        return vvs.toArray(RubiksVV[]::new);
    }

    protected RubiksKK[] commitKKOf(SerBE ser, Entity... entities) throws RubiksException {
        List<RubiksKK> kks = new ArrayList<>();
        int ii = 0;

        // primary kk + index kks
        for (Entity entity : entities) {
            kks.add(Bootstrap.primaryKKOf(entity, ser));
        }

        for (Entity entity : entities) {
            RubiksKK pk = kks.get(ii++);
            Bootstrap.indexKKOf(entity, pk, kks, ser);
        }
        return kks.toArray(RubiksKK[]::new);
    }

    protected RubiksVV[] commitVVOf(Entity... entities) throws RubiksException {
        List<RubiksVV> vvs = new ArrayList<>();

        // primary vv + index vvs
        for (Entity entity : entities) {
            vvs.add(primaryVVOf(entity));
        }
        for (Entity entity : entities) {
            Bootstrap.indexVVOf(entity, vvs);
        }
        return vvs.toArray(RubiksVV[]::new);
    }

    protected RubiksVV[] confirmVVOf(Entity... entities) {
        // we don't confirm the index (not now).
        return Arrays.stream(entities)
                .map(ent -> new RubiksVV(ent.present, ent.seqnum, Slice.ZERO))
                .toArray(RubiksVV[]::new);
    }

    protected static Instant deadline() {
        return Instant.now().plusMillis(1000);
    }
}
