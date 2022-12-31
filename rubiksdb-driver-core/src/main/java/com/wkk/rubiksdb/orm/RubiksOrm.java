package com.wkk.rubiksdb.orm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkk.rubiksdb.api.RubiksApi;
import com.wkk.rubiksdb.api.RubiksKK;
import com.wkk.rubiksdb.api.RubiksVV;
import com.wkk.rubiksdb.client.RubiksException;
import com.wkk.rubiksdb.common.Slice;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public interface RubiksOrm extends RubiksApi {
    // get entity by primary key
    void get(Entity... entities) throws RubiksException;

    void confirm(Entity... entities) throws RubiksException;

    void commit(Entity... entities) throws RubiksException;

    void listBy(Entity entity, long index,
                Consumer<Entity> consumer) throws RubiksException;

    default void map(RubiksVV vv, Entity entity) throws RubiksException {
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

    default RubiksKK[] primaryKKOf(Entity... entities) throws RubiksException {
        List<RubiksKK> kks = new ArrayList<>();

        for (Entity entity : entities) {
            kks.add(Bootstrap.primaryKKOf(entity));
        }
        return kks.toArray(RubiksKK[]::new);
    }

    default RubiksVV primaryVVOf(Entity entity) throws RubiksException {
        // serialize the entity into json as string and then take utf8 byte array
        try {
            String str = new ObjectMapper().writeValueAsString(entity);

            return new RubiksVV(
                    entity.present, entity.seqnum,
                    Slice.of(str.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw RubiksException.of(RubiksApi.RUBIKS_INVAL, exception);
        }
    }

    default RubiksVV[] primaryVVOf(Entity... entities) throws RubiksException {
        List<RubiksVV> vvs = new ArrayList<>();

        for (Entity entity : entities) {
            vvs.add(primaryVVOf(entity));
        }
        return vvs.toArray(RubiksVV[]::new);
    }

    default RubiksKK[] commitKKOf(Entity... entities) throws RubiksException {
        List<RubiksKK> kks = new ArrayList<>();

        // primary kk + index kks
        for (Entity entity : entities) {
            kks.add(Bootstrap.primaryKKOf(entity));
        }
        for (Entity entity : entities) {
            Bootstrap.indexKKOf(entity, kks);
        }
        return kks.toArray(RubiksKK[]::new);
    }

    default RubiksVV[] commitVVOf(Entity... entities) throws RubiksException {
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

    default RubiksVV[] confirmVVOf(Entity... entities) {
        // we don't confirm the index (not now).
        return Arrays.stream(entities)
                .map(ent -> new RubiksVV(ent.present, ent.seqnum, Slice.ZERO))
                .toArray(RubiksVV[]::new);
    }

    default Instant deadline() {
        return Instant.now().plusMillis(1000);
    }
}
