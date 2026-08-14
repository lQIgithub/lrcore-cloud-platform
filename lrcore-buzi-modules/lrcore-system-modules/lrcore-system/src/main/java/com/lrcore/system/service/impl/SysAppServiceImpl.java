package com.lrcore.system.service.impl;


import com.lrcore.system.domain.SysAppEntity;
import com.lrcore.system.mapper.SysAppMapper;
import com.lrcore.system.service.ISysAppService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 应用系统 服务类
 * @ClassName: SysAppServiceImpl
 * @Author: Qi Liu
 * @Date: 2026/5/16 10:48
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysAppServiceImpl extends ServiceImpl<SysAppMapper, SysAppEntity> implements ISysAppService {

}
