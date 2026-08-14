package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysRoleColumnPermissionEntity;
import com.lrcore.system.mapper.SysRoleColumnPermissionMapper;
import com.lrcore.system.service.ISysRoleColumnPermissionService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 角色字段权限关联 服务类
 * @ClassName: SysRoleColumnPermissionServiceImpl
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleColumnPermissionServiceImpl extends ServiceImpl<SysRoleColumnPermissionMapper, SysRoleColumnPermissionEntity> implements ISysRoleColumnPermissionService {

}
