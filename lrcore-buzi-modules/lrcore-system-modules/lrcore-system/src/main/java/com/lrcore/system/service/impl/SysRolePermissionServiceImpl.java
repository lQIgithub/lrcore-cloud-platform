package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysRolePermissionEntity;
import com.lrcore.system.mapper.SysRolePermissionMapper;
import com.lrcore.system.service.ISysRolePermissionService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 角色-权限关联 服务类
 * @ClassName: SysRolePermissionServiceImpl
 * @Author: Qi Liu
 * @Date: 2026/5/16 10:50
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermissionEntity> implements ISysRolePermissionService {

}
