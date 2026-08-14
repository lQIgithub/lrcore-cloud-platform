package com.lrcore.system.service.impl;

import com.lrcore.common.redis.service.RedisService;
import com.lrcore.system.domain.SysConfigEntity;
import com.lrcore.system.mapper.SysConfigMapper;
import com.lrcore.system.service.ISysConfigService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

}
