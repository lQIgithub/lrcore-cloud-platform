package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysRoleGroupEntity;
import com.lrcore.system.mapper.SysRoleGroupMapper;
import com.lrcore.system.service.ISysRoleGroupService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 角色分组 服务类
 * @ClassName: SysRoleGroupServiceImpl
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleGroupServiceImpl extends ServiceImpl<SysRoleGroupMapper, SysRoleGroupEntity> implements ISysRoleGroupService {

}
