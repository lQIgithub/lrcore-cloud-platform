package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysPermissionInheritanceEntity;
import com.lrcore.system.mapper.SysPermissionInheritanceMapper;
import com.lrcore.system.service.ISysPermissionInheritanceService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 权限继承关系表 服务类
 *
 * @author lrcore
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPermissionInheritanceServiceImpl extends ServiceImpl<SysPermissionInheritanceMapper, SysPermissionInheritanceEntity> implements ISysPermissionInheritanceService {

}
