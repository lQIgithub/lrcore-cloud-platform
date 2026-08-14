package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysTenantEntity;
import com.lrcore.system.mapper.SysTenantMapper;
import com.lrcore.system.service.ISysTenantService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 系统租户 服务类
 * @ClassName: SysTenantServiceImpl
 * @Author: Qi Liu
 * @Date: 2026/5/16 10:50
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenantEntity> implements ISysTenantService {

}
