package com.lrcore.rule.service.impl;

import com.lrcore.rule.domain.SysBizRuleConfigEntity;
import com.lrcore.rule.mapper.SysBizRuleConfigMapper;
import com.lrcore.rule.service.ISysBizRuleService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 系统业务规则配置表 服务类
 * @ClassName: SysBizRuleServiceImpl
 * @Author: Qi Liu
 * @Date: 2026/8/4 13:29
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysBizRuleServiceImpl extends ServiceImpl<SysBizRuleConfigMapper, SysBizRuleConfigEntity> implements ISysBizRuleService {
    
}
