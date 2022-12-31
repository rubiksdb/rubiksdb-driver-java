package com.wkk.rubiksdb.orm;

public class RubiksOrmImpl implements RubiksOrm {
    @Override
    public void get(Entity... entities) throws RuntimeException {
        for (Entity entity : entities) {
            long table = Bootstrap.tableOf(entity);

        }
    }

    @Override
    public void confirm(Entity... entities) throws RuntimeException {

    }

    @Override
    public void commit(Entity... entities) throws RuntimeException {

    }

    @Override
    public void listBy(Entity entity, long index) throws RuntimeException {

    }
}
