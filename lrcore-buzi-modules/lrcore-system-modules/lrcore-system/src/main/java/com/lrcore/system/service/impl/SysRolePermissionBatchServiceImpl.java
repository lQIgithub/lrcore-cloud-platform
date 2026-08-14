package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysRolePermissionBatchEntity;
import com.lrcore.system.mapper.SysRolePermissionBatchMapper;
import com.lrcore.system.service.ISysRolePermissionBatchService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 角色权限批量操作记录 服务类
 * @ClassName: SysRolePermissionBatchServiceImpl
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRolePermissionBatchServiceImpl extends ServiceImpl<SysRolePermissionBatchMapper, SysRolePermissionBatchEntity> implements ISysRolePermissionBatchService {

}
