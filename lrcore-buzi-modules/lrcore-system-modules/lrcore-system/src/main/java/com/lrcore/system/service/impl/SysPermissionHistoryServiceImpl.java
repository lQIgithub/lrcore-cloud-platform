package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysPermissionHistoryEntity;
import com.lrcore.system.mapper.SysPermissionHistoryMapper;
import com.lrcore.system.service.ISysPermissionHistoryService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 权限变更历史表 服务类
 *
 * @author lrcore
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPermissionHistoryServiceImpl extends ServiceImpl<SysPermissionHistoryMapper, SysPermissionHistoryEntity> implements ISysPermissionHistoryService {


}
