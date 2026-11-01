package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysNoticeEntity;
import com.lrcore.system.mapper.SysNoticeMapper;
import com.lrcore.system.service.ISysNoticeService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNoticeEntity> implements ISysNoticeService {
}
