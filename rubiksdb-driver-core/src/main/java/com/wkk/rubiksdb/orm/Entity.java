package com.wkk.rubiksdb.orm;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class Entity {
    @JsonIgnore public long    seqnum;
    @JsonIgnore public boolean present;
}
