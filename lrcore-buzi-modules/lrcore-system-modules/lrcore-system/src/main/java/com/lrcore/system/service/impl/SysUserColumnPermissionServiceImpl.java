package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysUserColumnPermissionEntity;
import com.lrcore.system.mapper.SysUserColumnPermissionMapper;
import com.lrcore.system.service.ISysUserColumnPermissionService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 用户字段权限覆盖 服务类
 * @ClassName: SysUserColumnPermissionServiceImpl
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserColumnPermissionServiceImpl extends ServiceImpl<SysUserColumnPermissionMapper, SysUserColumnPermissionEntity> implements ISysUserColumnPermissionService {

}
