package com.wkk.rubiksdb.common;

// with polynomial P = x^128 + x^7 + x^2 + x + 1

import lombok.AllArgsConstructor;

import static java.lang.String.format;

@AllArgsConstructor
public class Crc128 {
    public long v0, v1;

    public static Crc128 of(long v0, long v1) {
        return new Crc128(v0, v1);
    }

    @Override
    public String toString() {
        return format("crc128(0x%x,0x%x)", v0, v1);
    }

    public static Crc128 update(Crc128 crc0, byte[] src, int i0, int i1) {
        Crc128 crc = of(crc0.v0, crc0.v1);

        while (i0 + 8 <= i1) {
            long u =  (((long)src[i0] & 0xFF))
                    + (((long)src[i0 + 1] & 0xFF) << 8)
                    + (((long)src[i0 + 2] & 0xFF) << 16)
                    + (((long)src[i0 + 3] & 0xFF) << 24)
                    + (((long)src[i0 + 4] & 0xFF) << 32)
                    + (((long)src[i0 + 5] & 0xFF) << 40)
                    + (((long)src[i0 + 6] & 0xFF) << 48)
                    + (((long)src[i0 + 7] & 0xFF) << 56)
                    ;
            upd(crc, u, 64);
            i0 += 8;
        }

        while (i0 < i1) {
            upd(crc, src[i0++] & 0xFF, 8);
        }
        return crc;
    }

    private static void upd(Crc128 crc, long u, int n) {
        /* crc := uint128.T{U0: crc0.U0, U1: crc0.U1 ^ u} */
        long crc0 = crc.v0, crc1 = crc.v1 ^ u;

        /* l := crc.Shl(128 - n) */
        long l0 = shl_0(crc0, 128 - n);
        long l1 = shl_1(crc0, crc1, 128 - n);

        /* crc = crc.Shr(n) */
        long t0 = crc0;
        long t1 = crc1;
        crc0 = shr_0(t0, t1, n);
        crc1 = shr_1(t1, n);

        /* crc = crc.Xor(l.Xor(l.Shr(1))) */
        /* crc = crc.Xor(l.Shr(2).Xor(l.Shr(7))) */
        crc0 ^= l0
                ^ shr_0(l0, l1, 1)
                ^ shr_0(l0, l1, 2)
                ^ shr_0(l0, l1, 7);
        crc1 ^= l1
                ^ shr_1(l1, 1)
                ^ shr_1(l1, 2)
                ^ shr_1(l1, 7);

        crc.v0 = crc0;
        crc.v1 = crc1;
    }

    public static boolean eq(Crc128 aa, Crc128 bb) {
        return aa.v0 == bb.v0 && aa.v1 == bb.v1;
    }

    private static long shl_0(long x0, int n) {
        if (n >= 64) {
            return 0;
        } else if (n >= 0) {
            return x0 << n;
        } else {
            throw new IllegalArgumentException("shift by n < 0");
        }
    }

    private static long shl_1(long x0, long x1, int n) {
        if (n >= 128) {
            return 0;
        } else if (n >= 64) {
            return x0 << (n - 64);
        } else if (n > 0) {
            return (x1 << n) | (x0 >>> (64 - n));
        } else if (n == 0) {
            return x1;
        } else {
            throw new IllegalArgumentException("shift by n < 0");
        }
    }

    private static long shr_0(long x0, long x1, int n) {
        if (n >= 128) {
            return 0;
        } else if (n >= 64) {
            return x1 >>> (n - 64);
        } else if (n > 0) {
            return (x0 >>> n) | (x1 << (64 - n));
        } else if (n == 0) {
            return x0;
        } else {
            throw new IllegalArgumentException("shift by n < 0");
        }
    }

    private static long shr_1(long x1, int n) {
        if (n >= 64) {
            return 0;
        } else if (n >= 0) {
            return x1 >>> n;
        } else {
            throw new IllegalArgumentException("shift by n < 0");
        }
    }
}
