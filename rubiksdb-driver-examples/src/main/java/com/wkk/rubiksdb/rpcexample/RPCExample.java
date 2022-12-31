package com.wkk.rubiksdb.rpcexample;

import com.wkk.rubiksdb.api.RubiksKK;
import com.wkk.rubiksdb.api.RubiksVV;
import com.wkk.rubiksdb.client.RubiksClient;
import com.wkk.rubiksdb.client.RubiksException;
import com.wkk.rubiksdb.client.RubiksI;
import com.wkk.rubiksdb.client.RubiksPlainCM;
import com.wkk.rubiksdb.client.SimpleRetry;
import com.wkk.rubiksdb.common.Invariant;
import com.wkk.rubiksdb.common.Slice;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Instant;

@Slf4j
public class RPCExample {
    public static void main(String[] args) throws RubiksException, UnknownHostException {
        InetSocketAddress[] candidates =
                new InetSocketAddress[]{
                        new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 10008)};

        try (RubiksPlainCM cm = new RubiksPlainCM(candidates)) {
            RubiksI rubiks = new RubiksClient(cm, new SimpleRetry());

            long table = 1;
            byte[] key = new byte[]{0, 1, 2, 3};
            byte[] val = new byte[]{4, 5, 6, 7};

            RubiksKK kk = new RubiksKK(table, Slice.of(key));
            long t0 = System.currentTimeMillis();

            // get seqnum, and check existence
            log.info("get key table 1: {}", key);
            RubiksVV[] vvs = rubiks.RPCGet(new RubiksKK[]{kk}, deadline());
            Invariant.assertY(vvs.length == 1);

            // set vv
            vvs[0].present = true;
            vvs[0].val = Slice.of(val);

            log.info("set key table 1: {}, val {}", key, val);
            rubiks.RPCCommit(new RubiksKK[]{kk}, vvs, deadline());

            // seqnum bumped up
            Invariant.assertY(vvs[0].seqnum > 0);

            // check database
            log.info("check key table 1: {}, val {}", key, val);
            vvs = rubiks.RPCGet(new RubiksKK[]{kk}, deadline());
            Invariant.assertY(Slice.eq(vvs[0].val, Slice.of(val)));

            log.info("3 RPC in {} milli sec", System.currentTimeMillis() - t0);
        }
    }

    static Instant deadline() {
        return Instant.now().plusSeconds(1);
    }
}
