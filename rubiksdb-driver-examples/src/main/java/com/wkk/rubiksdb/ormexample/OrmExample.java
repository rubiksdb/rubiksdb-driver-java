package com.wkk.rubiksdb.ormexample;

import com.wkk.rubiksdb.client.RubiksClient;
import com.wkk.rubiksdb.client.RubiksI;
import com.wkk.rubiksdb.client.RubiksPlainCM;
import com.wkk.rubiksdb.client.SimpleRetry;
import com.wkk.rubiksdb.orm.Bootstrap;
import com.wkk.rubiksdb.orm.RubiksOrm;
import com.wkk.rubiksdb.orm.RubiksOrmImpl;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;

@Slf4j
public class OrmExample {
    static {
        Bootstrap.register(User.class);
    }

    public static void main(String[] args) throws Exception {
        InetSocketAddress[] candidates =
                new InetSocketAddress[]{
                        new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 10008)};

        try (RubiksPlainCM cm = new RubiksPlainCM(candidates)) {
            RubiksI rubiks = new RubiksClient(cm, new SimpleRetry());
            RubiksOrm orm = new RubiksOrmImpl(rubiks);

            // get users by primary key
            User user0 = User.builder().id(10).build();
            User user1 = User.builder().id(11).build();

            orm.get(user0, user1);
            log.info("user0: {}", user0);
            log.info("user1: {}", user1);

            // construct users and commit to rubiks
            user0.present = true;
            user0.setEmail("rubiksdb.kvdb@gmail.com");
            user0.setName("rubiks kvdb");
            user0.setStreet("111 WindsorRidge Dr");
            user0.setTown("Westboro");
            user0.setState("MA");

            user1.present = true;
            user1.setEmail("foo.bar@gmail.com");
            user1.setName("Foo Bar");
            user1.setStreet("111 WindsorRidge Dr");
            user1.setTown("Westboro");
            user1.setState("MA");

            orm.commit(user0, user1);

            // users by primary key
            orm.get(user0, user1);
            log.info("user0: {}", user0);
            log.info("user1: {}", user1);

            // list by index
        }
    }
}
