package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysRoleInheritanceEntity;
import com.lrcore.system.mapper.SysRoleInheritanceMapper;
import com.lrcore.system.service.ISysRoleInheritanceService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 角色继承关系 服务类
 * @ClassName: SysRoleInheritanceServiceImpl
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleInheritanceServiceImpl extends ServiceImpl<SysRoleInheritanceMapper, SysRoleInheritanceEntity> implements ISysRoleInheritanceService {

}
