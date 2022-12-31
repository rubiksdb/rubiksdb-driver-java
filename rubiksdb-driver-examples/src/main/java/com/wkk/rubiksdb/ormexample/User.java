package com.wkk.rubiksdb.ormexample;

import com.wkk.rubiksdb.orm.Table;
import com.wkk.rubiksdb.orm.Entity;
import com.wkk.rubiksdb.orm.Index;
import com.wkk.rubiksdb.orm.Primary;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Table(id = 100)
@Builder
@Setter
@Getter
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

    @Override
    public String toString() {
        if (present) {
            return String.format(
                    "present=%b,seqnum=%d id=%d,email=%s,address=%s %s %s,name=%s",
                    present, seqnum, id, email, street, town, state, name);
        } else {
            return String.format(
                    "present=%b,seqnum=%d id=%d", present, seqnum, id);
        }
    }
}
