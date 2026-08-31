package com.lrcore.system.service.impl;

import com.lrcore.common.core.utils.FunStrUtils;
import com.lrcore.common.flowable.enums.ProcessDefinitionStatus;
import com.lrcore.system.domain.SysProcessDefinitionBaseInfoEntity;
import com.lrcore.system.domain.apt.SysProcessDefinitionBaseInfoAPT;
import com.lrcore.system.mapper.SysProcessDefinitionBaseInfoMapper;
import com.lrcore.system.service.ISysProcessDefinitionBaseInfoService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    @Override
    public List<SysProcessDefinitionBaseInfoEntity> listDefinitions(String keyword, ProcessDefinitionStatus status) {
        SysProcessDefinitionBaseInfoAPT table = SysProcessDefinitionBaseInfoAPT.SYS_PROCESS_DEFINITION_BASE_INFO;
        // 只取列表展示所需列，排除 graph_data/bpmn_xml 大字段
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(table.ID, table.KEY, table.NAME, table.DESCRIPTION, table.CATEGORY,
                        table.VERSION, table.STATUS, table.ACT_RE_PROCDEF_ID,
                        table.CREATE_TIME, table.UPDATE_TIME)
                .from(table)
                .where(table.DELETED.eq(0));
        if (FunStrUtils.hasText(keyword)) {
            queryWrapper.and(table.NAME.like(keyword)
                    .or(table.KEY.like(keyword))
                    .or(table.DESCRIPTION.like(keyword)));
        }
        if (status != null) {
            queryWrapper.and(table.STATUS.eq(status));
        }
        // 更新时间倒序，id 兜底保证顺序稳定
        queryWrapper.orderBy(table.UPDATE_TIME.desc(), table.ID.desc());
        return mapper.selectListByQueryAs(queryWrapper, SysProcessDefinitionBaseInfoEntity.class);
    }

    @Override
    public int logicDeleteById(Long id) {
        SysProcessDefinitionBaseInfoAPT table = SysProcessDefinitionBaseInfoAPT.SYS_PROCESS_DEFINITION_BASE_INFO;
        SysProcessDefinitionBaseInfoEntity update = new SysProcessDefinitionBaseInfoEntity();
        update.setDeleted(1);
        update.setUpdateTime(LocalDateTime.now());
        return mapper.updateByQuery(update, QueryWrapper.create()
                .where(table.ID.eq(id))
                .and(table.DELETED.eq(0)));
    }

    @Override
    public void markDeployed(Long id, String actReProcdefId) {
        SysProcessDefinitionBaseInfoEntity update = new SysProcessDefinitionBaseInfoEntity();
        update.setId(id);
        update.setActReProcdefId(actReProcdefId);
        update.setStatus(ProcessDefinitionStatus.deployed);
        update.setUpdateTime(LocalDateTime.now());
        updateById(update);
    }

}
