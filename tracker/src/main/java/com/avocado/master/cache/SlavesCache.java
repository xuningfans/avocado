package com.avocado.master.cache;

import com.alibaba.fastjson.JSONObject;
import com.avocado.common.dto.HealthMessage;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SlaveCache class
 *
 * @author xuning
 * @date 2019-05-07 10:26
 */
@Slf4j
@Component
public class SlavesCache {

    private Cache cache;

    @Autowired
    public SlavesCache(CacheManager cacheManager) {
        cache = cacheManager.getCache("slaveCache");
    }

    public void put(String key, String value) {
        cache.put(key, value);
        Ehcache nativeCache = ((EhCacheCache) cache).getNativeCache();
        List keys = nativeCache.getKeys();
        Map<Object, Element> all = nativeCache.getAll(keys);
        all.forEach((key1, value1) -> {
            System.err.println(key1);
            System.err.println(value1.getObjectValue());
        });
    }

    public String get(String key) {
        return cache.get(key, String.class);
    }

    public Map<String, HealthMessage> getAll() {
        Ehcache nativeCache = ((EhCacheCache) cache).getNativeCache();
        List keys = nativeCache.getKeys();
        return nativeCache.getAll(keys)
                .entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(),
                        e -> JSONObject.parseObject(e.getValue().toString(), HealthMessage.class)
                ));
    }

    public int getSize() {
        Ehcache nativeCache = ((EhCacheCache) cache).getNativeCache();
        return nativeCache.getSize();
    }

}
