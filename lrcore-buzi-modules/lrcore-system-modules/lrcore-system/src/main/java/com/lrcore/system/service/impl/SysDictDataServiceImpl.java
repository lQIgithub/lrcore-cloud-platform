package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysDictDataEntity;
import com.lrcore.system.mapper.SysDictDataMapper;
import com.lrcore.system.service.ISysDictDataService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 字典 业务层处理
 *
 * @author lrcore
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictDataEntity> implements ISysDictDataService {
}
