package com.wkk.rubiksdb.orm;

import com.wkk.rubiksdb.client.RubiksException;

public interface RubiksOrm {
    // get entity by primary key
    void get(Entity... entities) throws RubiksException;

    void confirm(Entity... entities) throws RubiksException;

    void commit(Entity... entities) throws RubiksException;

    void listBy(Entity entity, long index) throws RubiksException;
}
