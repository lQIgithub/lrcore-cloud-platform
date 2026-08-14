package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysPermissionGroupEntity;
import com.lrcore.system.mapper.SysPermissionGroupMapper;
import com.lrcore.system.service.ISysPermissionGroupService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 权限组 服务类
 * @ClassName: SysPermissionGroupServiceImpl
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPermissionGroupServiceImpl extends ServiceImpl<SysPermissionGroupMapper, SysPermissionGroupEntity> implements ISysPermissionGroupService {

}
