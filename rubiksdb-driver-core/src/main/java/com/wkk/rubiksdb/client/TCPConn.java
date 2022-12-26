package com.wkk.rubiksdb.client;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import com.wkk.rubiksdb.api.RubiksApi;
import com.wkk.rubiksdb.common.Wire;
import com.wkk.rubiksdb.common.DerLE;
import com.wkk.rubiksdb.common.Nbuf;
import com.wkk.rubiksdb.common.SerLE;
import com.wkk.rubiksdb.common.Slice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

@Slf4j
public class TCPConn {
    private static final int TIMEOUT_MS = 200;
    private static final long MARGIN_MS = 2;

    private final InetSocketAddress endpoint;
    private final Socket sock = new Socket();

    private final Nbuf hdr = new Nbuf();
    private final byte[] recv = new byte[RubiksApi.SERIALIZE_SIZE];

    private final long clientId;
    private final long wireMagic;

    private int remain = 0;

    public TCPConn(InetSocketAddress endpoint, long clientId, long wireMagic) {
        this.endpoint = endpoint;
        this.clientId = clientId;
        this.wireMagic = wireMagic;
    }

    public InetSocketAddress getEndpoint() {
        return endpoint;
    }

    public synchronized void submit(RubiksR rbr) throws RubiksException {
        try {
            liveness();

            // ensure 2ms margin for RTT
            final long now = System.currentTimeMillis();
            final long deadline = rbr.deadline.toEpochMilli() - MARGIN_MS;
            if (now > deadline) {
                throw RubiksException.of(RubiksApi.RUBIKS_TIMEOUT);
            }
            rbr.putHdr(clientId, (deadline - now) * 1000);

            Slice slice = Wire.serialize(
                    SerLE.of(rbr.serialize),
                    RubiksApi.WIRE_MAGIC,
                    new Nbuf[]{rbr.req.hdr, rbr.req.msg},
                    new Slice[]{Slice.of(rbr.req.payload)});
            OutputStream os = sock.getOutputStream();

            os.write(slice.data, slice.i0, slice.length());
            os.flush();
        } catch (IOException exception) {
            log.info(
                    "can't send request to {}, exception: {}",
                    endpoint, exception.getMessage(), exception);
            throw RubiksException.of(RubiksApi.RUBIKS_EIO);
        }
    }

    public void recv(Consumer resp) {
        if (!safety()) {
            return;
        }

        try {
            InputStream is = sock.getInputStream();
            int produced = is.read(recv, remain, recv.length - remain);

            if (produced == 0) {
                throw new IOException("peer closed connection");
            }

            remain += produced;
            hdr.reset();

            int consumable = Wire.consumable(
                    DerLE.of(recv, remain), wireMagic, hdr);
            if (consumable < 0) {
                throw new IOException("malformed message from " + endpoint);
            }

            if (resp.accept(Slice.of(recv, consumable), hdr) != 0) {
                throw new IOException("malformed message from " + endpoint);
            }
            System.arraycopy(
                    recv, consumable,
                    recv, 0, remain - consumable);
            remain -= consumable;
        } catch (SocketTimeoutException exception) {
            ; // due to setSoTimeout
        } catch (IOException exception) {
            log.info("reset connection: {}", exception.getMessage());
            reset();
        }
    }

    private void liveness() throws IOException {
        if (!sock.isConnected()) {
            sock.connect(endpoint, TIMEOUT_MS);

            sock.setTcpNoDelay(true);
            sock.setSoTimeout(TIMEOUT_MS);  // timeout read call on InputStream

            // wake up the recv routine
            notify();
        }
    }

    private synchronized boolean safety() {
        if (!sock.isConnected()) {
            synchronized (this) {
                try {
                    wait(100);
                } catch (InterruptedException exception) {
                    log.debug("interrupted while waiting for connection");
                }
                return sock.isConnected();
            }
        }
        return true;
    }

    private synchronized void reset() {
        try {
            remain = 0;
            sock.close();
        } catch (IOException exception) {
            log.debug("can't reset connection", exception);
        }
    }
}
