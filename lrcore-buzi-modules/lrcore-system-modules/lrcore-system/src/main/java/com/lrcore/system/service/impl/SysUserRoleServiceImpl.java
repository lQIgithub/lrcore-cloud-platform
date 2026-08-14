package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysUserRoleEntity;
import com.lrcore.system.mapper.SysUserRoleMapper;
import com.lrcore.system.service.ISysUserRoleService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 用户-角色关联 服务类
 * @ClassName: SysUserRoleServiceImpl
 * @Author: Qi Liu
 * @Date: 2026/5/16 10:51
 * @Version: 1.0
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRoleEntity> implements ISysUserRoleService {

}
