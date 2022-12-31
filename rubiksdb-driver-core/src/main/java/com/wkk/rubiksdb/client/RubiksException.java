package com.wkk.rubiksdb.client;

import static com.wkk.rubiksdb.api.RubiksApi.RUBIKS_EIO;
import static com.wkk.rubiksdb.api.RubiksApi.RUBIKS_INVAL;
import static com.wkk.rubiksdb.api.RubiksApi.RUBIKS_NONEXT;
import static com.wkk.rubiksdb.api.RubiksApi.RUBIKS_STALE;
import static com.wkk.rubiksdb.api.RubiksApi.RUBIKS_TIMEOUT;

public class RubiksException extends Exception {
    public final int what;

    private RubiksException(int what, String msg) {
        super(msg);
        this.what = what;
    }

    private RubiksException(int what, Throwable cause) {
        super(cause);
        this.what = what;
    }

    public static RubiksException of(int what) {
        return new RubiksException(what, stringify(what));
    }

    public static RubiksException of(int what, String msg) {
        return new RubiksException(what, msg);
    }

    public static RubiksException of(int what, Throwable cause) {
        return new RubiksException(what, cause);
    }

    private static String stringify(int what) {
        switch (what) {
            case RUBIKS_TIMEOUT:    return "RUBIKS_TIMEOUT";
            case RUBIKS_INVAL:      return "RUBIKS_INVAL";
            case RUBIKS_STALE:      return "RUBIKS_STALE";
            case RUBIKS_NONEXT:     return "RUBIKS_NONEXT";
            case RUBIKS_EIO:        return "RUBIKS_EIO";
            default:                return "RUBIKS_UNKNOWN";
        }
    }
}
