package com.wkk.rubiksdb.ormexample;

import com.wkk.rubiksdb.orm.Table;
import com.wkk.rubiksdb.orm.Entity;
import com.wkk.rubiksdb.orm.Index;
import com.wkk.rubiksdb.orm.Primary;
import lombok.Builder;

@Table(id = 100)
@Builder
public class User extends Entity {
    // primary key
    @Primary
    private long id;

    // simple index
    @Index(id = 101)
    private String email;

    // composite index of street + town + state
    @Index(id = 102)
    private String street;

    @Index(id = 102)
    private String town;

    @Index(id = 102)
    private String state;

    @Index(id = 103)
    private String name;
}
