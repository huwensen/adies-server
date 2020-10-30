package com.xst.aedis;

import java.util.Set;

/**
 * @author joker
 */
public class RedisService {

    public byte[] auth(String[] password) {
        return RedisValue.ok();
    }

    public byte[] ping(String[] cmd) {
        return RedisValue.pong();
    }

    public byte[] quit(String[] cmd) {
        return RedisValue.ok();
    }

    public byte[] del(String[] cmd) {
        return RedisValue.del(cmd[1]);
    }

    public byte[] unlink(String[] cmd) {
        return RedisValue.del(cmd[1]);
    }

    public byte[] flushdb(String[] cmd) {
        return RedisValue.flushdb();
    }

    public byte[] flushall(String[] cmd) {
        return RedisValue.flushdb();
    }

    public byte[] randomkey(String[] cmd) {
        return RedisValue.randomKey();
    }

    public byte[] rename(String[] cmd) {
        RedisValue orDefault = RedisValue.getOrDefault(cmd[1]);
        return orDefault.rename(cmd[2]);
    }

    public byte[] renamenx(String[] cmd) {
        RedisValue orDefault = RedisValue.getOrDefault(cmd[1]);
        return orDefault.renamenx(cmd[2]);
    }

    public byte[] dbsize(String[] cmd) {
        return RedisValue.dbSize();
    }

    public byte[] expireat(String[] cmd) {
        RedisValue orDefault = RedisValue.getOrDefault(cmd[1]);
        return orDefault.expireAt(cmd[1], cmd[2]);
    }

    public byte[] move(String[] cmd) {
        return (":0\r\n").getBytes();
    }

    public byte[] set(String[] kv) {
        RedisValue.put(kv[1], RedisValue.stringValue(kv[1], kv[2]));
        return RedisValue.ok();
    }

    public byte[] get(String[] k) {
        RedisValue redisValue = RedisValue.getOrDefault(k[1], RedisValue.nullValue());
        return redisValue.getRedisValueByte();
    }

    public byte[] getset(String[] cmd) {
        RedisValue redisValue = RedisValue.getOrDefault(cmd[1]);
        return redisValue.getSet(cmd[2]);
    }

    public byte[] mget(String[] cmd) {
        return RedisValue.mGet(cmd);
    }

    //TODO:所有方法都需要优化同步加锁
    public byte[] setnx(String[] cmd) {
        RedisValue orDefault = RedisValue.getOrDefault(cmd[1]);
        return orDefault.setNX(cmd[1], cmd[2]);
    }

    public byte[] msetnx(String[] cmd) {
        return RedisValue.mSetNx(cmd);
    }

    public byte[] setex(String[] cmd) {
        RedisValue.put(cmd[1], RedisValue.stringValue(cmd[1], cmd[3]));
        RedisValue orDefault = RedisValue.getOrDefault(cmd[1]);
        return orDefault.expire(Long.valueOf(cmd[2]));
    }

    public byte[] mset(String[] cmd) {
        int length = cmd.length;
        for (int i = 1; i < length - 1; i += 2) {
            RedisValue.put(cmd[i], RedisValue.stringValue(cmd[i], cmd[i + 1]));
        }
        return RedisValue.ok();
    }

    public byte[] type(String[] k) {
        RedisValue s = RedisValue.getOrDefault(k[1], RedisValue.nullValue());
        return s.getTypeByte();
    }

    public byte[] ttl(String[] k) {
        RedisValue s = RedisValue.getOrDefault(k[1], RedisValue.nullValue());
        return s.getTtlByte();
    }

    public byte[] exists(String[] k) {
        RedisValue s = RedisValue.getOrDefault(k[1], RedisValue.nullValue());
        return s.getExistsByte();

    }

    public byte[] object(String[] k) {
        String key = k[1];
        switch (key) {
            case "idletime":
                return ":0\r\n".getBytes();
            case "refcount":
                return ":1\r\n".getBytes();
            case "encoding":
                return ("$3\r\n" +
                        "raw\r\n").getBytes();
        }
        return ":1\r\n".getBytes();
    }

    public byte[] keys(String[] k) {
        Set<String> strings = RedisValue.keySet();
        String result = "*" + strings.size();
        for (String string : strings) {
            result += "\r\n$" + string.length() + "\r\n" + string;
        }
        result += "\r\n";
        return result.getBytes();

    }

    public byte[] select(String[] kv) {
        //TODO 目前只有单个DB
        return RedisValue.ok();
    }

    public byte[] expire(String[] k) {
        RedisValue s = RedisValue.getOrDefault(k[1], RedisValue.nullValue());
        return s.expire(Long.valueOf(k[2]));
    }

    public byte[] config(String[] k) {
        String s = "*2\r\n" +
                "$9\r\n" +
                "databases\r\n" +
                "$2\r\n" +
                "16";

        return (s + "\r\n").getBytes();
    }

    public byte[] info(String[] strings) {
        String s = "# Server\r\n" +
                "redis_version:5.0.3\r\n" +
                "redis_git_sha1:00000000\r\n" +
                "redis_git_dirty:0\r\n" +
                "redis_build_id:1b271fe49834c463\r\n" +
                "redis_mode:standalone\r\n" +
                "os:Linux 4.19.57-v7l+ armv7l\r\n" +
                "arch_bits:32\r\n" +
                "multiplexing_api:epoll\r\n" +
                "atomicvar_api:atomic-builtin\r\n" +
                "gcc_version:8.3.0\r\n" +
                "process_id:500\r\n" +
                "run_id:46a1d7a2e3c2dd982f215c50d9c5447eb355915f\r\n" +
                "tcp_port:6379\r\n" +
                "uptime_in_seconds:428744\r\n" +
                "uptime_in_days:4\r\n" +
                "hz:10\r\n" +
                "configured_hz:10\r\n" +
                "lru_clock:8406474\r\n" +
                "executable:/usr/bin/redis-server\r\n" +
                "config_file:/etc/redis/redis.conf\r\n" +
                "\r\n" +
                "# Clients\r\n" +
                "connected_clients:1\r\n" +
                "client_recent_max_input_buffer:2\r\n" +
                "client_recent_max_output_buffer:0\r\n" +
                "blocked_clients:0\r\n" +
                "\r\n" +
                "# Memory\r\n" +
                "used_memory:714872\r\n" +
                "used_memory_human:698.12K\r\n" +
                "used_memory_rss:4116480\r\n" +
                "used_memory_rss_human:3.93M\r\n" +
                "used_memory_peak:714872\r\n" +
                "used_memory_peak_human:698.12K\r\n" +
                "used_memory_peak_perc:100.00%\r\n" +
                "used_memory_overhead:699394\r\n" +
                "used_memory_startup:649680\r\n" +
                "used_memory_dataset:15478\r\n" +
                "used_memory_dataset_perc:23.74%\r\n" +
                "allocator_allocated:1497424\r\n" +
                "allocator_active:9699328\r\n" +
                "allocator_resident:11272192\r\n" +
                "total_system_memory:4095782912\r\n" +
                "total_system_memory_human:3.81G\r\n" +
                "used_memory_lua:37888\r\n" +
                "used_memory_lua_human:37.00K\r\n" +
                "used_memory_scripts:0\r\n" +
                "used_memory_scripts_human:0B\r\n" +
                "number_of_cached_scripts:0\r\n" +
                "maxmemory:3221225472\r\n" +
                "maxmemory_human:3.00G\r\n" +
                "maxmemory_policy:noeviction\r\n" +
                "allocator_frag_ratio:6.48\r\n" +
                "allocator_frag_bytes:8201904\r\n" +
                "allocator_rss_ratio:1.16\r\n" +
                "allocator_rss_bytes:1572864\r\n" +
                "rss_overhead_ratio:0.37\r\n" +
                "rss_overhead_bytes:-7155712\r\n" +
                "mem_fragmentation_ratio:6.12\r\n" +
                "mem_fragmentation_bytes:3443608\r\n" +
                "mem_not_counted_for_evict:0\r\n" +
                "mem_replication_backlog:0\r\n" +
                "mem_clients_slaves:0\r\n" +
                "mem_clients_normal:49574\r\n" +
                "mem_aof_buffer:0\r\n" +
                "mem_allocator:jemalloc-5.1.0\r\n" +
                "active_defrag_running:0\r\n" +
                "lazyfree_pending_objects:0\r\n" +
                "\r\n" +
                "# Persistence\r\n" +
                "loading:0\r\n" +
                "rdb_changes_since_last_save:0\r\n" +
                "rdb_bgsave_in_progress:0\r\n" +
                "rdb_last_save_time:1601813250\r\n" +
                "rdb_last_bgsave_status:ok\r\n" +
                "rdb_last_bgsave_time_sec:-1\r\n" +
                "rdb_current_bgsave_time_sec:-1\r\n" +
                "rdb_last_cow_size:0\r\n" +
                "aof_enabled:0\r\n" +
                "aof_rewrite_in_progress:0\r\n" +
                "aof_rewrite_scheduled:0\r\n" +
                "aof_last_rewrite_time_sec:-1\r\n" +
                "aof_current_rewrite_time_sec:-1\r\n" +
                "aof_last_bgrewrite_status:ok\r\n" +
                "aof_last_write_status:ok\r\n" +
                "aof_last_cow_size:0\r\n" +
                "\r\n" +
                "# Stats\r\n" +
                "total_connections_received:1\r\n" +
                "total_commands_processed:10\r\n" +
                "instantaneous_ops_per_sec:0\r\n" +
                "total_net_input_bytes:325\r\n" +
                "total_net_output_bytes:5266\r\n" +
                "instantaneous_input_kbps:0.00\r\n" +
                "instantaneous_output_kbps:0.00\r\n" +
                "rejected_connections:0\r\n" +
                "sync_full:0\r\n" +
                "sync_partial_ok:0\r\n" +
                "sync_partial_err:0\r\n" +
                "expired_keys:0\r\n" +
                "expired_stale_perc:0.00\r\n" +
                "expired_time_cap_reached_count:0\r\n" +
                "evicted_keys:0\r\n" +
                "keyspace_hits:0\r\n" +
                "keyspace_misses:0\r\n" +
                "pubsub_channels:0\r\n" +
                "pubsub_patterns:0\r\n" +
                "latest_fork_usec:0\r\n" +
                "migrate_cached_sockets:0\r\n" +
                "slave_expires_tracked_keys:0\r\n" +
                "active_defrag_hits:0\r\n" +
                "active_defrag_misses:0\r\n" +
                "active_defrag_key_hits:0\r\n" +
                "active_defrag_key_misses:0\r\n" +
                "\r\n" +
                "# Replication\r\n" +
                "role:master\r\n" +
                "connected_slaves:0\r\n" +
                "master_replid:2b4d291ce6243535f662c43c22b836e60c78b447\r\n" +
                "master_replid2:0000000000000000000000000000000000000000\r\n" +
                "master_repl_offset:0\r\n" +
                "second_repl_offset:-1\r\n" +
                "repl_backlog_active:0\r\n" +
                "repl_backlog_size:1048576\r\n" +
                "repl_backlog_first_byte_offset:0\r\n" +
                "repl_backlog_histlen:0\r\n" +
                "\r\n" +
                "# CPU\r\n" +
                "used_cpu_sys:10.676799\r\n" +
                "used_cpu_user:6.807430\r\n" +
                "used_cpu_sys_children:0.000000\r\n" +
                "used_cpu_user_children:0.000000\r\n" +
                "\r\n" +
                "# Cluster\r\n" +
                "cluster_enabled:0\r\n" +
                "\r\n" +
                "# Keyspace\r\n" +
                "db0:keys=3,expires=0,avg_ttl=0";
        return ("$" + s.length() + "\r\n" + s + "\r\n").getBytes();
    }

    public byte[] decrby(String[] cmd) {
        RedisValue redisValue = RedisValue.getOrDefault(cmd[1]);
        return redisValue.decrBy(cmd[1], Long.valueOf(cmd[2]));
    }

    public byte[] decr(String[] cmd) {
        RedisValue redisValue = RedisValue.getOrDefault(cmd[1]);
        return redisValue.decr(cmd[1]);
    }

    public byte[] incrBy(String[] cmd) {
        RedisValue redisValue = RedisValue.getOrDefault(cmd[1]);
        return redisValue.incrBy(cmd[1], Long.valueOf(cmd[2]));
    }

    public byte[] incr(String[] cmd) {
        RedisValue redisValue = RedisValue.getOrDefault(cmd[1]);
        return redisValue.incr(cmd[1]);
    }

    public byte[] append(String[] cmd) {
        RedisValue redisValue = RedisValue.getOrDefault(cmd[1]);
        return redisValue.append(cmd[1], cmd[2]);
    }
    public byte[] substr(String[] cmd) {
        RedisValue redisValue = RedisValue.getOrDefault(cmd[1]);
        return redisValue.substr(Integer.valueOf(cmd[2]), Integer.valueOf(cmd[3]));
    }

    public byte[] hset(String[] cmd){
        RedisValue redisValue=RedisValue.getOrDefault(cmd[1]);
        return redisValue.hSet(cmd[1],cmd[2],cmd[3]);
    }

    public byte[] hget(String[] cmd){
        RedisValue redisValue=RedisValue.getOrDefault(cmd[1]);
        return redisValue.hGet(cmd[2]);
    }
}
