package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysPermissionAuditLogEntity;
import com.lrcore.system.mapper.SysPermissionAuditLogMapper;
import com.lrcore.system.service.ISysPermissionAuditLogService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 权限审计日志 服务类
 * @ClassName: SysPermissionAuditLogServiceImpl
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPermissionAuditLogServiceImpl extends ServiceImpl<SysPermissionAuditLogMapper, SysPermissionAuditLogEntity> implements ISysPermissionAuditLogService {


}
