package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysColumnMetadataEntity;
import com.lrcore.system.mapper.SysColumnMetadataMapper;
import com.lrcore.system.service.ISysColumnMetadataService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 字段元数据表 服务层
 * @ClassName: SysColumnMetadataServiceImpl
 * @Author: Qi Liu
 * @Date: 2026/6/9 22:20
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysColumnMetadataServiceImpl extends ServiceImpl<SysColumnMetadataMapper, SysColumnMetadataEntity> implements ISysColumnMetadataService {

}
