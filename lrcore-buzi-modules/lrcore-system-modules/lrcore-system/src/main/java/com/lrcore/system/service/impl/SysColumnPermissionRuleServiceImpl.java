package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysColumnPermissionRuleEntity;
import com.lrcore.system.mapper.SysColumnPermissionRuleMapper;
import com.lrcore.system.service.ISysColumnPermissionRuleService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 字段权限规则表 服务层
 * @ClassName: SysColumnPermissionRuleServiceImpl
 * @Author: Qi Liu
 * @Date: 2026/6/9 22:20
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysColumnPermissionRuleServiceImpl extends ServiceImpl<SysColumnPermissionRuleMapper, SysColumnPermissionRuleEntity> implements ISysColumnPermissionRuleService {

}
