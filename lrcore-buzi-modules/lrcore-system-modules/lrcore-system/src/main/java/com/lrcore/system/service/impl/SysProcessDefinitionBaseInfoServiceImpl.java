package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysProcessDefinitionBaseInfoEntity;
import com.lrcore.system.mapper.SysProcessDefinitionBaseInfoMapper;
import com.lrcore.system.service.ISysProcessDefinitionBaseInfoService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 流程定义基础信息 服务层实现
 * @ClassName: SysProcessDefinitionBaseInfoServiceImpl
 * @Author: lrcore
 * @Date: 2026/08/13
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysProcessDefinitionBaseInfoServiceImpl extends ServiceImpl<SysProcessDefinitionBaseInfoMapper, SysProcessDefinitionBaseInfoEntity> implements ISysProcessDefinitionBaseInfoService {

}
