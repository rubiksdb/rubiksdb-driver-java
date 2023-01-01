package com.wkk.rubiksdb.orm;

import com.wkk.rubiksdb.api.RubiksApi;
import com.wkk.rubiksdb.client.RubiksException;
import com.wkk.rubiksdb.common.SerBE;
import lombok.Builder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OrmTest {
    private static final long USER_TABLE = 100;
    private static final long NAME_INDEX = 101;
    private static final long ADDR_INDEX = 102;

    static {
        Bootstrap.register(User.class);
    }

    @Test
    public void test0() throws RubiksException {
        SerBE ser = SerBE.of(RubiksApi.MAX_PAYLOAD_SIZE);
        User user0 =
                User.builder()
                        .id(1234)
                        .name("Carl X")
                        .street("111 Windsor Ridge DR")
                        .town("Westboro")
                        .state("MA")
                        .build();
        Assertions.assertEquals(USER_TABLE, Bootstrap.tableOf(user0));

        Bootstrap.serialize(user0, USER_TABLE, ser);
        Assertions.assertEquals(8, ser.flip().length());

        Bootstrap.serialize(user0, NAME_INDEX, ser);
        Assertions.assertEquals(6, ser.flip().length());

        Bootstrap.serialize(user0, ADDR_INDEX, ser);
        Assertions.assertEquals(30, ser.flip().length());
    }

    @Table(id = USER_TABLE)
    @Builder
    static class User extends Entity {
        // primary key
        @Primary
        private final long id;

        // simple index
        @Index(id = NAME_INDEX)
        private final String name;

        // composite index of street + town + state
        @Index(id = ADDR_INDEX)
        private final String street;

        @Index(id = ADDR_INDEX)
        private final String town;

        @Index(id = ADDR_INDEX)
        private final String state;
    }
}
