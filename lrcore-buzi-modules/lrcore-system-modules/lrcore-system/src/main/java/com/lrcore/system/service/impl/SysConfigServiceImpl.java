package com.lrcore.system.service.impl;

import com.lrcore.common.redis.service.RedisService;
import com.lrcore.system.domain.SysConfigEntity;
import com.lrcore.system.domain.apt.SysConfigAPT;
import com.lrcore.system.mapper.SysConfigMapper;
import com.lrcore.system.service.ISysConfigService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * 参数配置 服务层实现
 *
 * @author lrcore
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfigEntity> implements ISysConfigService {
    private final RedisService redisService;

    private static final String CONFIG_CACHE_KEY = "sys:config:";

    @Override
    public SysConfigEntity getByConfigKey(String configKey) {
        String cacheKey = CONFIG_CACHE_KEY + configKey;
        SysConfigEntity cachedConfig = redisService.getCacheObject(cacheKey);
        if (cachedConfig != null) {
            return cachedConfig;
        }

        SysConfigEntity config = getOne(QueryWrapper.create()
                .where(SysConfigAPT.SYS_CONFIG.CONFIG_KEY.eq(configKey))
                .and(SysConfigAPT.SYS_CONFIG.DELETED.eq(0)));
        if (config != null) {
            redisService.setCacheObject(cacheKey, config);
        }
        return config;
    }

    @Override
    public boolean refreshCache() {
        Collection<String> keys = redisService.keys(CONFIG_CACHE_KEY + "*");
        if (keys == null || keys.isEmpty()) {
            return true;
        }
        return redisService.deleteObject(keys);
    }
}
