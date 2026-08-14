package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysRoleGroupMemberEntity;
import com.lrcore.system.mapper.SysRoleGroupMemberMapper;
import com.lrcore.system.service.ISysRoleGroupMemberService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 角色-分组关联 服务类
 * @ClassName: SysRoleGroupMemberServiceImpl
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleGroupMemberServiceImpl extends ServiceImpl<SysRoleGroupMemberMapper, SysRoleGroupMemberEntity> implements ISysRoleGroupMemberService {

}
