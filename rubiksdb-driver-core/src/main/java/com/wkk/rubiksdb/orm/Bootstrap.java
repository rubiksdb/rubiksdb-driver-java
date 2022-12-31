package com.wkk.rubiksdb.orm;

import com.wkk.rubiksdb.api.RubiksApi;
import com.wkk.rubiksdb.api.RubiksKK;
import com.wkk.rubiksdb.api.RubiksVV;
import com.wkk.rubiksdb.client.RubiksException;
import com.wkk.rubiksdb.common.Invariant;
import com.wkk.rubiksdb.common.SerBE;
import com.wkk.rubiksdb.common.Slice;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class Bootstrap {
    private static final Map<String, Map<Long, List<String>>> INDEX = new HashMap<>();

    public static void register(Class<? extends Entity> cls) {
        log.info("register {}", cls);
        long table = cls.getAnnotation(Table.class).id();
        String cname = cls.getName();

        Map<Long, List<String>> old = INDEX.put(cname, new HashMap<>());
        Invariant.assertY(old == null); // no duplication

        for (Field field : cls.getDeclaredFields()) {
            Primary primary = field.getAnnotation(Primary.class);
            Index index = field.getAnnotation(Index.class);
            String fname = field.getName();

            if (primary != null) {
                INDEX.get(cname).putIfAbsent(table, new ArrayList<>());
                INDEX.get(cname).get(table).add(fname);
                log.info("{} pk {}", table, fname);
            }

            if (index != null) {
                INDEX.get(cname).putIfAbsent(index.id(), new ArrayList<>());
                INDEX.get(cname).get(index.id()).add(fname);
                log.info("{} index {}", index.id(), fname);;
            }
        }
    }

    public static long tableOf(Entity entity) {
        return entity.getClass().getAnnotation(Table.class).id();
    }

    public static Slice keyOf(Entity entity, long index) throws RubiksException {
        Class<? extends Entity> cls = entity.getClass();
        SerBE ser = SerBE.of(new byte[RubiksApi.MAX_PAIR_SIZE]);

        try {
            for (String name : INDEX.get(cls.getName()).get(index)) {
                Field field = cls.getDeclaredField(name);
                field.setAccessible(true);

                Object obj = field.get(entity);
                switch (obj.getClass().getName()) {
                    case "java.lang.Byte":
                        ser.put1((long) (Byte) obj & 0xFF);
                        break;

                    case "java.lang.Short":
                        ser.put2((long) (Short) obj & 0xFF);
                        break;

                    case "java.lang.Long":
                        ser.put8((long) obj);
                        break;

                    case "java.lang.String":
                        ser.put((String) obj);
                        break;

                    default:
                        log.error("unsupported index type: {}", obj.getClass().getName());
                        throw RubiksException.of(RubiksApi.RUBIKS_INVAL, cls.getName());
                }
            }
            return ser.mark();
        } catch (Exception exception) {
            throw RubiksException.of(RubiksApi.RUBIKS_INVAL, exception);
        }
    }

    public static RubiksKK primaryKKOf(Entity entity) throws RubiksException {
        long table = tableOf(entity);
        return new RubiksKK(table, keyOf(entity, table));
    }

    public static void indexKKOf(Entity entity,
                                 List<RubiksKK> result) throws RubiksException {
        String name = entity.getClass().getName();
        long table = tableOf(entity);

        for (long index : INDEX.get(name).keySet()) {
            if (index != table) {
                result.add(new RubiksKK(index, keyOf(entity, index)));
            }
        }
    }

    public static RubiksKK indexKKOf(Entity entity, long index) throws RubiksException {
        String name = entity.getClass().getName();
        long table = tableOf(entity);

        Invariant.assertY(index != table);
        Invariant.assertY(INDEX.get(name).containsKey(index));

        return new RubiksKK(index, keyOf(entity, index));
    }

    public static void indexVVOf(Entity entity, List<RubiksVV> result) {
        String name = entity.getClass().getName();
        long table = tableOf(entity);

        for (long index : INDEX.get(name).keySet()) {
            if (index != table) {
                // take entity present bit, seqnum unchecked as index
                result.add(new RubiksVV(entity.present, RubiksApi.SEQNUM_INF, Slice.ZERO));
            }
        }
    }
}
