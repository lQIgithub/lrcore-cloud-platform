package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysDictTypeEntity;
import com.lrcore.system.mapper.SysDictTypeMapper;
import com.lrcore.system.service.ISysDictTypeService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * <p>类模块说明</p>
 *
 * @Describe: 字典 业务层处理
 * @ClassName: SysDictTypeServiceImpl
 * @Author: Qi Liu
 * @Date: 2026/3/26 14:58
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictTypeEntity> implements ISysDictTypeService {
}
