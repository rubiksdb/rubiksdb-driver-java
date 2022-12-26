package com.wkk.rubiksdb.client;

import com.wkk.rubiksdb.common.Wire;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import com.wkk.rubiksdb.api.RubiksApi;
import com.wkk.rubiksdb.common.Blob;
import com.wkk.rubiksdb.common.Crc128;
import com.wkk.rubiksdb.common.DerLE;
import com.wkk.rubiksdb.common.Invariant;
import com.wkk.rubiksdb.common.Nbuf;
import com.wkk.rubiksdb.common.Slice;

import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class RubiksPlainCM implements RubiksCM, RubiksApi, AutoCloseable {
    private static final long REVIVE_MS = 40 * 1000;
    private static final long MASK = 0x1F;

    private static final Random RANDOM = new SecureRandom();
    private static final long CLIENT_ID = RANDOM.nextLong();

    private long seedRequestId = RANDOM.nextLong() >>> 32;
    private final ExecutorService pool;

    private final TCPConn[] conn;
    private final long[] hint;
    private final long[] sick;

    private volatile boolean terminated = false;

    public RubiksPlainCM(InetSocketAddress[] candidates) {
        Invariant.assertY(Invariant.pow2(req.length));

        this.pool = Executors.newFixedThreadPool(candidates.length);
        this.conn = new TCPConn[candidates.length];
        this.hint = new long[candidates.length];
        this.sick = new long[candidates.length];

        for (int i = 0; i < candidates.length; ++i) {
            TCPConn conn = new TCPConn(candidates[i], CLIENT_ID, WIRE_MAGIC);

            this.conn[i] = conn;
            this.hint[i] = longify(candidates[i]);

            // start thread for each connection
            this.pool.submit(() -> {
                while (!terminated) {
                    conn.recv(consumer);
                }
            });
        }
    }

    @Override
    public void close() {
        terminated = true;
        pool.shutdownNow();
    }

    @Override
    public void submit(RubiksR rbr, long hint) throws RubiksException {
        synchronized (this) {
            rbr.requestId = nextRequestId();
            rbr.endpoint = pick(hint);
        }
        rbr.submitT0 = Instant.now();

        if (rbr.endpoint < 0) {
            log.error("no connection to rubiks server!!");
            throw RubiksException.of(RUBIKS_EIO,
                    "no connection to rubiks server");
        }

        try {
            conn[rbr.endpoint].submit(rbr);
        } catch (RubiksException exception) {
            noteSick(rbr.endpoint);
            throw exception;
        }

        // prepare to wait for completion
        synchronized (safety(rbr.requestId)) {
            map(rbr.requestId).put(rbr.requestId, rbr);
        }
    }

    @SneakyThrows
    @Override
    public void waitForCompletion(RubiksR rbr) {
        synchronized (safety(rbr.requestId)) {
            while (rbr.respSlice == Slice.ZERO){
                long now = System.currentTimeMillis();
                long allowance = Math.max(0, rbr.deadline.toEpochMilli() - now);
                if (allowance <= 0) {
                    break;
                }
                safety(rbr.requestId).wait(allowance);
            }
            map(rbr.requestId).remove(rbr.requestId);
        }

        if (rbr.respSlice == Slice.ZERO) {
            log.info(
                    "timeout to rubiks server {}",
                    conn[rbr.endpoint].getEndpoint());
            throw RubiksException.of(RUBIKS_TIMEOUT);
        }

        // deserialize rbr.respSlice
        Nbuf[] nbufs = new Nbuf[]{rbr.resp.hdr, rbr.resp.msg};
        Slice[] slices = new Slice[]{null};
        Wire.deserialize(DerLE.of(rbr.respSlice), nbufs, slices);

        int outcome = (int) rbr.resp.get(TAG_OUTCOME);
        if (outcome != RUBIKS_OK) {
            throw RubiksException.of(outcome);
        }

        Slice payload = slices[0];
        if (payload != null) {
            final Crc128 crc = rbr.resp.getCrc128();
            final Crc128 actual =
                    Crc128.update(PAYLOAD_CRC,
                            payload.data, payload.i0, payload.i1);

            if (!Crc128.eq(crc, actual)) {
                throw RubiksException.of(RUBIKS_EIO, "payload crc mismatch");
            }
            rbr.resp.payload = new Blob(crc,
                    slices[0].data, slices[0].i0, slices[0].i1);
        }
    }

    // pickup endpoint by hint of rubiks key
    private int pick(long hint) {
        long now = System.currentTimeMillis();
        int victim = victim(hint, now);

        if (victim == -1) {
            Arrays.fill(sick, 0);
            victim = victim(hint, now);
        }
        return victim;
    }

    private int victim(long hint, long now) {
        long max = 0;
        int victim = -1;

        for (int i = 0; i < sick.length; ++i) {
            if (now > sick[i] + REVIVE_MS) {
                if ((this.hint[i] ^ hint) > max) {
                    max = this.hint[i] ^ hint;
                    victim = i;
                }
            }
        }
        return victim;
    }

    private long nextRequestId() {
        return seedRequestId++;
    }

    private void wakeup(Slice src, long requestId) {
        synchronized (safety(requestId)) {
            RubiksR rbr = map(requestId).getOrDefault(requestId, null);

            if (rbr != null) {
                System.arraycopy(
                        src.data, src.i0,
                        rbr.serialize, 0, src.length());
                rbr.respSlice = Slice.of(rbr.serialize, src.length());
                safety(requestId).notifyAll();
            }
        }
    }

    private synchronized void noteSick(int victim) {
        sick[victim] = System.currentTimeMillis();
    }

    private static long longify(InetSocketAddress ep) {
        byte[] addr = ep.getAddress().getAddress();
        // first 4 bytes with ipv4 big-endian
        return (ep.getPort() & 0xFF)
                | ((long) addr[3] & 0xFF) << 32
                | ((long) addr[2] & 0xFF) << 40
                | ((long) addr[1] & 0xFF) << 48
                | ((long) addr[0] & 0xFF) << 56
                ;
    }

    private final Map<Long, RubiksR>[] req = new Map[] {
            new HashMap<Long, RubiksR>(), new HashMap<Long, RubiksR>(),
            new HashMap<Long, RubiksR>(), new HashMap<Long, RubiksR>(),
            new HashMap<Long, RubiksR>(), new HashMap<Long, RubiksR>(),
            new HashMap<Long, RubiksR>(), new HashMap<Long, RubiksR>(),
            new HashMap<Long, RubiksR>(), new HashMap<Long, RubiksR>(),
            new HashMap<Long, RubiksR>(), new HashMap<Long, RubiksR>(),
            new HashMap<Long, RubiksR>(), new HashMap<Long, RubiksR>(),
            new HashMap<Long, RubiksR>(), new HashMap<Long, RubiksR>(),
            new HashMap<Long, RubiksR>(), new HashMap<Long, RubiksR>(),
            new HashMap<Long, RubiksR>(), new HashMap<Long, RubiksR>(),
            new HashMap<Long, RubiksR>(), new HashMap<Long, RubiksR>(),
            new HashMap<Long, RubiksR>(), new HashMap<Long, RubiksR>(),
            new HashMap<Long, RubiksR>(), new HashMap<Long, RubiksR>(),
            new HashMap<Long, RubiksR>(), new HashMap<Long, RubiksR>(),
            new HashMap<Long, RubiksR>(), new HashMap<Long, RubiksR>(),
            new HashMap<Long, RubiksR>(), new HashMap<Long, RubiksR>(),
    };

    private final Object[] cond = {
            new Object(), new Object(), new Object(), new Object(),
            new Object(), new Object(), new Object(), new Object(),
            new Object(), new Object(), new Object(), new Object(),
            new Object(), new Object(), new Object(), new Object(),
            new Object(), new Object(), new Object(), new Object(),
            new Object(), new Object(), new Object(), new Object(),
            new Object(), new Object(), new Object(), new Object(),
            new Object(), new Object(), new Object(), new Object(),
    };

    private Object safety(long requestId) {
        return cond[(int) (requestId & MASK)];
    }

    private Map<Long, RubiksR> map(long requestId) {
        return req[(int) (requestId & MASK)];
    }

    private final Consumer consumer = (src, hdr) -> {
        final long mask = (1L << TAG_CLIENT_ID) | (1L << TAG_REQUEST_ID);
        if (!hdr.have(mask) ||
                hdr.get(TAG_CLIENT_ID) != CLIENT_ID) {
            return -1;
        }

        wakeup(src, hdr.get(TAG_REQUEST_ID));
        return 0;
    };
}
