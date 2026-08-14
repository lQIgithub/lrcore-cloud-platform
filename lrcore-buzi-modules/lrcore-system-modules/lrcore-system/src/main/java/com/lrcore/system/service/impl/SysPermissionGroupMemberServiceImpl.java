package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysPermissionGroupMemberEntity;
import com.lrcore.system.mapper.SysPermissionGroupMemberMapper;
import com.lrcore.system.service.ISysPermissionGroupMemberService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 权限组成员关系 服务类
 * @ClassName: SysPermissionGroupMemberServiceImpl
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPermissionGroupMemberServiceImpl extends ServiceImpl<SysPermissionGroupMemberMapper, SysPermissionGroupMemberEntity> implements ISysPermissionGroupMemberService {

}
