package com.wkk.rubiksdb.ormexample;

import com.wkk.rubiksdb.api.RubiksApi;
import com.wkk.rubiksdb.client.RubiksClient;
import com.wkk.rubiksdb.client.RubiksI;
import com.wkk.rubiksdb.client.RubiksPlainCM;
import com.wkk.rubiksdb.client.SimpleRetry;
import com.wkk.rubiksdb.orm.Bootstrap;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;

@Slf4j
public class OrmExample {
    static {
        Bootstrap.register(User.class);
    }

    public static void main(String[] args) throws Exception {
        InetSocketAddress[] candidates = new InetSocketAddress[] {
                new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 10008)};

        try (RubiksPlainCM cm = new RubiksPlainCM(candidates)) {
            User user0 = User.builder().id(10).build();

        }
    }
}
