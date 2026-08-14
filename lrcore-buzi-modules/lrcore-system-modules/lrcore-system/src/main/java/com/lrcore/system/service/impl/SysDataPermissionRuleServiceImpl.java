package com.lrcore.system.service.impl;

import com.lrcore.common.datascope.exception.DataScopeException;
import com.lrcore.system.domain.SysDataPermissionRuleEntity;
import com.lrcore.system.enums.DataRuleTypeEnum;
import com.lrcore.system.mapper.SysDataPermissionRuleMapper;
import com.lrcore.system.service.ISysDataPermissionRuleService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 数据权限规则表 服务层实现
 * <p>
 * 提供数据权限规则的完整CRUD操作和业务查询方法实现。
 * </p>
 *
 * @Describe 数据权限规则表 服务层实现
 * @ClassName SysDataPermissionRuleServiceImpl
 * @Author Qi Liu
 * @Date 2026/6/10
 * @Version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDataPermissionRuleServiceImpl extends ServiceImpl<SysDataPermissionRuleMapper, SysDataPermissionRuleEntity> implements ISysDataPermissionRuleService {

    @Override
    public SysDataPermissionRuleEntity getByRuleCode(String ruleCode) {
        if (ruleCode == null || ruleCode.isEmpty()) {
            return null;
        }
        return getMapper().selectOneByQuery(QueryWrapper.create()
                .where(SysDataPermissionRuleEntity::getRuleCode).eq(ruleCode)
                .and(SysDataPermissionRuleEntity::getDeleted).eq(0));
    }

    @Override
    public List<SysDataPermissionRuleEntity> listValidRulesByTenant(Long tenantId) {
        if (tenantId == null) {
            log.warn("租户ID为空，返回空规则列表");
            return List.of();
        }
        return getMapper().selectValidRulesByTenant(tenantId);
    }

    @Override
    public List<SysDataPermissionRuleEntity> listValidRules(Long tenantId, Long appId) {
        if (tenantId == null) {
            log.warn("租户ID为空，返回空规则列表");
            return List.of();
        }
        return getMapper().selectValidRules(tenantId, appId);
    }

    @Override
    public List<SysDataPermissionRuleEntity> listRowRulesByTable(Long tenantId, String targetTable) {
        if (tenantId == null || targetTable == null || targetTable.isEmpty()) {
            log.warn("租户ID或目标表名为空，返回空规则列表");
            return List.of();
        }
        return getMapper().selectListByQuery(QueryWrapper.create()
                .where(SysDataPermissionRuleEntity::getTenantId).eq(tenantId)
                .and(SysDataPermissionRuleEntity::getTargetTable).eq(targetTable)
                .and(SysDataPermissionRuleEntity::getRuleTypeEnum).eq(DataRuleTypeEnum.ROW)
                .and(SysDataPermissionRuleEntity::getIsEnabled).eq(1)
                .and(SysDataPermissionRuleEntity::getDeleted).eq(0)
                .orderBy(SysDataPermissionRuleEntity::getPriority).asc());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRule(SysDataPermissionRuleEntity entity) {
        // 1. 校验规则编码唯一性
        if (existsByRuleCode(entity.getRuleCode())) {
            throw DataScopeException.ruleCodeDuplicate(entity.getRuleCode());
        }

        // 2. 设置默认值
        if (entity.getIsEnabled() == null) {
            entity.setIsEnabled(1);
        }
        if (entity.getPriority() == null) {
            entity.setPriority(100);
        }
        if (entity.getDeleted() == null) {
            entity.setDeleted(0);
        }

        // 3. 保存规则
        int result = getMapper().insert(entity);
        log.info("创建数据权限规则成功，规则编码: {}, 规则名称: {}", entity.getRuleCode(), entity.getRuleName());
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRule(SysDataPermissionRuleEntity entity) {
        // 1. 检查规则是否存在
        SysDataPermissionRuleEntity existing = getById(entity.getId());
        if (existing == null) {
            throw DataScopeException.ruleNotFound(entity.getId());
        }

        // 2. 校验规则编码唯一性（排除自身）
        if (!existing.getRuleCode().equals(entity.getRuleCode())
                && existsByRuleCodeExclude(entity.getRuleCode(), entity.getId())) {
            throw DataScopeException.ruleCodeDuplicate(entity.getRuleCode());
        }

        // 3. 更新规则
        int result = getMapper().update(entity);
        log.info("更新数据权限规则成功，规则ID: {}, 规则编码: {}", entity.getId(), entity.getRuleCode());
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableRule(Long ruleId) {
        SysDataPermissionRuleEntity entity = getById(ruleId);
        if (entity == null) {
            throw DataScopeException.ruleNotFound(ruleId);
        }

        entity.setIsEnabled(1);
        int result = getMapper().update(entity);
        log.info("启用数据权限规则成功，规则ID: {}", ruleId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableRule(Long ruleId) {
        SysDataPermissionRuleEntity entity = getById(ruleId);
        if (entity == null) {
            throw DataScopeException.ruleNotFound(ruleId);
        }

        entity.setIsEnabled(0);
        int result = getMapper().update(entity);
        log.info("禁用数据权限规则成功，规则ID: {}", ruleId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRule(Long ruleId) {
        SysDataPermissionRuleEntity entity = getById(ruleId);
        if (entity == null) {
            throw DataScopeException.ruleNotFound(ruleId);
        }

        // 逻辑删除
        entity.setDeleted(1);
        int result = getMapper().update(entity);
        log.info("删除数据权限规则成功，规则ID: {}", ruleId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteRules(List<Long> ruleIds) {
        if (ruleIds == null || ruleIds.isEmpty()) {
            log.warn("规则ID列表为空，跳过批量删除");
            return false;
        }

        // 批量逻辑删除 - 使用循环逐个更新
        int count = 0;
        for (Long ruleId : ruleIds) {
            SysDataPermissionRuleEntity entity = new SysDataPermissionRuleEntity();
            entity.setId(ruleId);
            entity.setDeleted(1);
            if (getMapper().update(entity) > 0) {
                count++;
            }
        }

        log.info("批量删除数据权限规则成功，删除数量: {}", count);
        return count > 0;
    }

    @Override
    public boolean existsByRuleCode(String ruleCode) {
        if (ruleCode == null || ruleCode.isEmpty()) {
            return false;
        }
        return getMapper().selectCountByQuery(QueryWrapper.create()
                .where(SysDataPermissionRuleEntity::getRuleCode).eq(ruleCode)
                .and(SysDataPermissionRuleEntity::getDeleted).eq(0)) > 0;
    }

    @Override
    public boolean existsByRuleCodeExclude(String ruleCode, Long excludeId) {
        if (ruleCode == null || ruleCode.isEmpty()) {
            return false;
        }
        return getMapper().selectCountByQuery(QueryWrapper.create()
                .where(SysDataPermissionRuleEntity::getRuleCode).eq(ruleCode)
                .and(SysDataPermissionRuleEntity::getId).ne(excludeId)
                .and(SysDataPermissionRuleEntity::getDeleted).eq(0)) > 0;
    }
}
