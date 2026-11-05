package com.lrcore.system.mapper;

import com.lrcore.system.domain.SysDataPermissionRuleEntity;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
    /**
     * 查询当前数据库的所有基础表名（按表名排序）
     */
    @Select("SELECT table_name FROM information_schema.tables " +
            "WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE' " +
            "ORDER BY table_name")
    List<String> selectDatabaseTables();

    /**
     * 查询指定表的列名（按列序排序）
     *
     * @param tableName 表名（调用前须做白名单校验，防止注入）
     */
    @Select("SELECT column_name FROM information_schema.columns " +
            "WHERE table_schema = DATABASE() AND table_name = #{tableName} " +
            "ORDER BY ordinal_position")
    List<String> selectColumnsByTable(@Param("tableName") String tableName);

    default List<SysDataPermissionRuleEntity> selectValidRules(Long tenantId, Long appId) {
        return selectListByQuery(QueryWrapper.create()
                .where(SysDataPermissionRuleEntity::getTenantId).eq(tenantId)
                .and(SysDataPermissionRuleEntity::getAppId).eq(appId)
                .and(SysDataPermissionRuleEntity::getIsEnabled).eq(1)
                .and(SysDataPermissionRuleEntity::getDeleted).eq(0)
                .orderBy(SysDataPermissionRuleEntity::getPriority).asc());
    }
}
