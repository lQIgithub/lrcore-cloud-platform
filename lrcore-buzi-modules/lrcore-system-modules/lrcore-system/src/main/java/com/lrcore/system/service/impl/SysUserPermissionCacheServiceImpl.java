package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysUserPermissionCacheEntity;
import com.lrcore.system.mapper.SysUserPermissionCacheMapper;
import com.lrcore.system.service.ISysUserPermissionCacheService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 用户权限缓存 服务类
 * @ClassName: SysUserPermissionCacheServiceImpl
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserPermissionCacheServiceImpl extends ServiceImpl<SysUserPermissionCacheMapper, SysUserPermissionCacheEntity> implements ISysUserPermissionCacheService {

}
