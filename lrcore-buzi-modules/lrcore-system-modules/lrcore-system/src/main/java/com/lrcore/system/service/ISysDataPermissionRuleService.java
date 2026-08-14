package com.lrcore.system.service;

import com.lrcore.system.domain.SysDataPermissionRuleEntity;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 数据权限规则表 服务层接口
 * <p>
 * 提供数据权限规则的完整CRUD操作和业务查询方法。
 * </p>
 *
 * @Describe 数据权限规则表 服务层接口
 * @ClassName ISysDataPermissionRuleService
 * @Author Qi Liu
 * @Date 2026/6/10
 * @Version 1.0
 */
public interface ISysDataPermissionRuleService extends IService<SysDataPermissionRuleEntity> {

    /**
     * 根据规则编码查询规则
     *
     * @param ruleCode 规则编码
     * @return 规则实体，不存在返回null
     */
    SysDataPermissionRuleEntity getByRuleCode(String ruleCode);

    /**
     * 查询指定租户下的所有有效规则
     *
     * @param tenantId 租户ID
     * @return 有效规则列表（按优先级升序）
     */
    List<SysDataPermissionRuleEntity> listValidRulesByTenant(Long tenantId);

    /**
     * 查询指定租户和应用下的所有有效规则
     *
     * @param tenantId 租户ID
     * @param appId    应用ID
     * @return 有效规则列表（按优先级升序）
     */
    List<SysDataPermissionRuleEntity> listValidRules(Long tenantId, Long appId);

    /**
     * 查询指定表的所有行级权限规则
     *
     * @param tenantId    租户ID
     * @param targetTable 目标表名
     * @return 行级权限规则列表
     */
    List<SysDataPermissionRuleEntity> listRowRulesByTable(Long tenantId, String targetTable);

    /**
     * 创建数据权限规则
     * <p>
     * 会校验规则编码唯一性
     * </p>
     *
     * @param entity 规则实体
     * @return 创建成功返回true
     */
    boolean createRule(SysDataPermissionRuleEntity entity);

    /**
     * 更新数据权限规则
     *
     * @param entity 规则实体
     * @return 更新成功返回true
     */
    boolean updateRule(SysDataPermissionRuleEntity entity);

    /**
     * 启用规则
     *
     * @param ruleId 规则ID
     * @return 启用成功返回true
     */
    boolean enableRule(Long ruleId);

    /**
     * 禁用规则
     *
     * @param ruleId 规则ID
     * @return 禁用成功返回true
     */
    boolean disableRule(Long ruleId);

    /**
     * 删除规则（逻辑删除）
     *
     * @param ruleId 规则ID
     * @return 删除成功返回true
     */
    boolean deleteRule(Long ruleId);

    /**
     * 批量删除规则
     *
     * @param ruleIds 规则ID列表
     * @return 删除成功返回true
     */
    boolean batchDeleteRules(List<Long> ruleIds);

    /**
     * 检查规则编码是否存在
     *
     * @param ruleCode 规则编码
     * @return 存在返回true
     */
    boolean existsByRuleCode(String ruleCode);

    /**
     * 检查规则编码是否存在（排除指定ID）
     *
     * @param ruleCode  规则编码
     * @param excludeId 排除的规则ID
     * @return 存在返回true
     */
    boolean existsByRuleCodeExclude(String ruleCode, Long excludeId);
}
