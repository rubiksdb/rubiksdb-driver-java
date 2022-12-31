package com.wkk.rubiksdb.orm;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class Entity {
    @JsonIgnore public boolean present = false;
    @JsonIgnore public long    seqnum = 0;
}
