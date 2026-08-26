package com.lrcore.system.mapper;

import com.lrcore.system.domain.SysDataPermissionRuleEntity;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 数据权限规则表 映射层
 * <p>
 * 提供数据权限规则的数据库访问方法。
 * </p>
 *
 * @Describe 数据权限规则表 映射层
 * @ClassName SysDataPermissionRuleMapper
 * @Author Qi Liu
 * @Date 2026/6/10
 * @Version 1.0
 */
@Mapper
public interface SysDataPermissionRuleMapper extends BaseMapper<SysDataPermissionRuleEntity> {

    /**
     * 根据租户ID查询有效规则
     *
     * @param tenantId 租户ID
     * @return 规则列表
     */
    default List<SysDataPermissionRuleEntity> selectValidRulesByTenant(Long tenantId) {
        return selectListByQuery(QueryWrapper.create()
                .where(SysDataPermissionRuleEntity::getTenantId).eq(tenantId)
                .and(SysDataPermissionRuleEntity::getIsEnabled).eq(1)
                .and(SysDataPermissionRuleEntity::getDeleted).eq(0)
                .orderBy(SysDataPermissionRuleEntity::getPriority).asc());
    }

    /**
     * 根据租户ID和应用ID查询有效规则
     *
     * @param tenantId 租户ID
     * @param appId    应用ID
     * @return 规则列表
     */
    default List<SysDataPermissionRuleEntity> selectValidRules(Long tenantId, Long appId) {
        return selectListByQuery(QueryWrapper.create()
                .where(SysDataPermissionRuleEntity::getTenantId).eq(tenantId)
                .and(SysDataPermissionRuleEntity::getAppId).eq(appId)
                .and(SysDataPermissionRuleEntity::getIsEnabled).eq(1)
                .and(SysDataPermissionRuleEntity::getDeleted).eq(0)
                .orderBy(SysDataPermissionRuleEntity::getPriority).asc());
    }
}
