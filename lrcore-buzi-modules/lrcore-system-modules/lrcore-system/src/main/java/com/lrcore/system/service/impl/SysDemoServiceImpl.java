package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysDemoEntity;
import com.lrcore.system.mapper.SysDemoMapper;
import com.lrcore.system.service.ISysDemoService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 演示信息 服务类
 * @ClassName: SysDemoServiceImpl
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDemoServiceImpl extends ServiceImpl<SysDemoMapper, SysDemoEntity> implements ISysDemoService {


}
