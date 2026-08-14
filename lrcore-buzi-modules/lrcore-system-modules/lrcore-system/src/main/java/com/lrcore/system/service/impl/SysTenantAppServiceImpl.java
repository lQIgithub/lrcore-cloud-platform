package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysTenantAppEntity;
import com.lrcore.system.mapper.SysTenantAppMapper;
import com.lrcore.system.service.ISysTenantAppService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 租户-应用app关联 服务类
 * @ClassName: SysTenantAppServiceImpl
 * @Author: Qi Liu
 * @Date: 2026/5/16 10:50
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysTenantAppServiceImpl extends ServiceImpl<SysTenantAppMapper, SysTenantAppEntity> implements ISysTenantAppService {

}
