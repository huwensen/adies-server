package com.xst.aedis;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * @author joker
 */
public final class RedisValue {

    private final static HashMap<String, RedisValue> map = new HashMap();
    private String key;
    private Object value;
    private Long time;
    private String type;

    private RedisValue() {
    }

    public static RedisValue stringValue(String key, Object object) {
        RedisValue redisValue = new RedisValue();
        redisValue.key = key;
        redisValue.value = object.toString();
        redisValue.time = -1L;
        redisValue.type = "string";
        return redisValue;
    }

    public static RedisValue hashValue(String key, Object object) {
        RedisValue redisValue = new RedisValue();
        redisValue.key = key;
        redisValue.value = object;
        redisValue.time = -1L;
        redisValue.type = "hash";
        return redisValue;
    }

    public String getStringValue() {
        return this.value.toString();
    }

    private Long getLongValue() {
        return Long.valueOf(getStringValue());
    }

    public HashMap getHashValue() {
        return (HashMap) this.value;
    }

    public static RedisValue nullValue() {
        return new RedisValue();
    }

    public static byte[] ok() {
        return "+OK\r\n".getBytes();
    }

    public static byte[] one() {
        return ":1\r\n".getBytes();
    }

    public static byte[] number(Object object) {
        return (":" + object + "\r\n").getBytes();
    }

    public static byte[] empty() {
        return "$0\r\n\r\n".getBytes();
    }

    public static byte[] stringByte(String string) {
        return String.format("$%d\r\n%s\r\n", string.length(), string).getBytes();
    }

    public static byte[] zero() {
        return ":0\r\n".getBytes();
    }

    public static byte[] minusOne() {
        return ":-1\r\n".getBytes();
    }

    public static void put(String s, RedisValue stringValue) {
        map.put(s, stringValue);
    }

    public static RedisValue getOrDefault(String s) {
        return getOrDefault(s, RedisValue.nullValue());
    }

    public static RedisValue getOrDefault(String s, RedisValue nullValue) {
        RedisValue orDefault = map.getOrDefault(s, nullValue);
        if (orDefault.time != null && orDefault.time > 0 && orDefault.time < System.currentTimeMillis()) {
            orDefault.value = null;
            orDefault.time = -1L;
            map.remove(orDefault.key);
        }
        return orDefault;
    }

    public static Set<String> keySet() {
        return map.keySet();
    }

    public static byte[] pong() {
        return "+PONG\r\n".getBytes();
    }

    public static byte[] del(String s) {
        RedisValue remove = map.remove(s);
        if (remove == null) {
            return zero();
        }
        return one();
    }

    public static byte[] flushdb() {
        map.clear();
        return ok();
    }

    public static byte[] randomKey() {
        if (map.isEmpty()) {
            return "$-1\r\n".getBytes();
        }
        Set<String> strings = map.keySet();
        String[] objects = (String[]) strings.toArray();
        Random random = new Random(strings.size());
        int i = random.nextInt();
        String object = objects[i];
        return ("$" + object.length() + "\r\n" + object + "\r\n").getBytes();
    }

    public static byte[] dbSize() {
        return (":" + map.size() + "\r\n").getBytes();
    }

    public static byte[] mGet(String[] cmd) {
        int length = cmd.length;
        StringBuilder sb = new StringBuilder();
        sb.append("*" + (length - 1));
        sb.append("\r\n");
        for (int i = 1; i < length; i++) {
            RedisValue redisValue = map.get(cmd[i]);
            if (redisValue == null) {
                sb.append("$-1\r\n");
            } else {
                sb.append("$" + redisValue.getStringValue().length() + "\r\n");
                sb.append(redisValue.value + "\r\n");
            }
        }
        return sb.toString().getBytes();
    }

    public static byte[] mSetNx(String[] cmd) {
        synchronized (map) {
            int length = cmd.length;
            for (int i = 1; i < length - 1; i += 2) {
                RedisValue redisValue = map.get(cmd[i]);
                if (redisValue != null) {
                    return zero();
                }
            }
            for (int i = 1; i < length - 1; i += 2) {
                put(cmd[i], RedisValue.stringValue(cmd[i], cmd[i + 1]));
            }
            return one();
        }
    }

    public byte[] rename(String newKey) {
        if (time > 0 && time < System.currentTimeMillis()) {
            value = null;
            time = -1L;
            map.remove(key);
        }
        if (value == null) {
            return ("-ERR no such key\r\n").getBytes();
        }
        map.remove(key);
        put(newKey, RedisValue.stringValue(newKey, value));
        return ok();
    }

    public byte[] renamenx(String newKey) {
        RedisValue.getOrDefault(newKey).getRedisValueByte();
        if (map.containsKey(newKey)) {
            return zero();
        }
        return rename(newKey);
    }

    public byte[] getRedisValueByte() {
        if (time != null && time > 0 && time < System.currentTimeMillis()) {
            value = null;
            time = -1L;
            map.remove(key);
        }
        if (value == null) {
            return minusOne();
        }
        return ("$" + getValueLength() + "\r\n" + value + "\r\n").getBytes();
    }

    public int getValueLength() {
        return getStringValue().length();
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public byte[] getTypeByte() {
        return ("+" + type + "\r\n").getBytes();
    }

    public byte[] getTtlByte() {
        if (time == -1) {
            return minusOne();
        } else {
            long l = time - System.currentTimeMillis();

            return (":" + l / 1000 + "\r\n").getBytes();
        }
    }

    public byte[] getExistsByte() {
        if (value == null) {
            return minusOne();
        }
        return one();
    }

    public byte[] expire(long s) {
        if (this.value == null) {
            return minusOne();
        }
        this.time = System.currentTimeMillis() + s * 1000;
        return one();
    }


    public byte[] expireAt(String key, String time) {
        if (this.value == null) {
            return minusOne();
        }
        this.time = Long.valueOf(time);
        return one();

    }

    public byte[] getSet(String value) {
        if (this.value == null) {
            return minusOne();
        }

        byte[] bytes = ("$" + getValueLength() + "\r\n" + this.value + "\r\n").getBytes();
        this.value = value;
        return bytes;
    }

    public byte[] setNX(String key, String value) {
        if (this.value != null) {
            return zero();
        }
        RedisValue redisValue = RedisValue.stringValue(key, value);
        put(key, redisValue);
        return one();
    }

    public byte[] decrBy(String key, Long valueOf) {
        if (this.value == null) {
            RedisValue redisValue = RedisValue.stringValue(key, -valueOf);
            put(key, redisValue);
            return number(-valueOf);
        }
        long l = getLongValue() - valueOf;
        this.value = (l) + "";
        return number(l);
    }

    public byte[] decr(String s) {
        if (this.value == null) {
            return minusOne();
        }
        try {
            this.value = (getLongValue() - 1);
            return number(this.value);
        } catch (Exception e) {
            return ("-ERR value is not an integer or out of range\r\n").getBytes();
        }

    }

    public byte[] incrBy(String key, Long valueOf) {
        if (this.value == null) {
            RedisValue redisValue = RedisValue.stringValue(key, valueOf);
            put(key, redisValue);
            return number(-valueOf);
        }
        long l = getLongValue() + valueOf;
        this.value = (l) + "";
        return number(l);
    }

    public byte[] incr(String s) {
        if (this.value == null) {
            return minusOne();
        }
        try {
            this.value = (getLongValue() + 1);
            return number(this.value);
        } catch (Exception e) {
            return ("-ERR value is not an integer or out of range\r\n").getBytes();
        }

    }

    public byte[] append(String key, String value) {
        if (this.value == null) {
            RedisValue redisValue = RedisValue.stringValue(key, value);
            put(key, redisValue);
            return number(value.length());
        }
        this.value = this.value + value;
        return number(getStringValue().length());

    }

    public byte[] substr(int beginIndex, int endIndex) {
        if (this.value == null) {
            return empty();
        }
        try {
            String string = getStringValue().substring(beginIndex, endIndex);
            return stringByte(string);
        } catch (Exception e) {
            return empty();
        }

    }

    public byte[] hSet(String key, String field, Object value) {
        if (this.value == null) {
            HashMap<String, Object> map = new HashMap<>();
            map.put(field, value);
            RedisValue redisValue = hashValue(key, map);
            put(key, redisValue);
            return ok();
        }
        HashMap hashValue = getHashValue();
        boolean b = hashValue.containsKey(field);
        hashValue.put(field, value);
        if (b) {
            return zero();
        } else {
            return one();
        }
    }

    public byte[] hGet(String field) {
        if (this.value == null) {
            return minusOne();
        }
        HashMap hashValue = getHashValue();
        Object o = hashValue.get(field);
        if(Objects.isNull(o)){
            return minusOne();
        }
        return stringByte(o.toString());
    }
}
