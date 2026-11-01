package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysNoticeReadEntity;
import com.lrcore.system.mapper.SysNoticeReadMapper;
import com.lrcore.system.service.ISysNoticeReadService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SysNoticeReadServiceImpl extends ServiceImpl<SysNoticeReadMapper, SysNoticeReadEntity> implements ISysNoticeReadService {
}
