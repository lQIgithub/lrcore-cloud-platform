package com.lrcore.system.provider;

import com.lrcore.common.core.utils.SecurityUtils;
import com.lrcore.common.core.web.domain.login.LoginUser;
import com.lrcore.common.core.web.domain.perm.ColumnPermissionRule;
import com.lrcore.common.core.web.domain.perm.DataPermissionRule;
import com.lrcore.common.core.web.domain.perm.PermissionRule;
import com.lrcore.common.datascope.provider.DataScopeRuleProvider;
import com.lrcore.system.domain.SysColumnPermissionRuleEntity;
import com.lrcore.system.domain.SysDataPermissionRuleEntity;
import com.lrcore.system.service.ISysColumnPermissionRuleService;
import com.lrcore.system.service.ISysDataPermissionRuleService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <>类模块说明</p>
 *
 * @Describe: 系统数据权限是否生效最关键的位置
 * 扩展数据权限权限加载数据数据表规则信息
 * @ClassName: DataScopeRuleProvider
 * @Author: Qi Liu
 * @Date: 2026/6/17 00:27
 * @Version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SysDataScopeRuleProvider implements DataScopeRuleProvider {
    private final ISysColumnPermissionRuleService sysColumnPermissionRuleService;
    private final ISysDataPermissionRuleService sysDataPermissionRuleService;

    @Override
    public PermissionRule queryRule() {
        // 1. 获取当前登录用户
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            log.warn("未获取到登录用户，返回空权限规则");
            return PermissionRule.builder()
                    .dataPermissionRules(Collections.emptyList())
                    .columnPermissionRules(Collections.emptyList())
                    .build();
        }

        Long tenantId = loginUser.getTenantId();
        Long userId = loginUser.getUserId();
        log.info("加载数据权限规则: tenantId={}, userId={}", tenantId, userId);

        // 2. 查询数据权限规则（行级/数据范围/自定义SQL）
        List<DataPermissionRule> dataPermissionRules = loadDataPermissionRules(tenantId);

        // 3. 查询字段级权限规则
        List<ColumnPermissionRule> columnPermissionRules = loadColumnPermissionRules(tenantId);

        return PermissionRule.builder()
                .dataPermissionRules(dataPermissionRules)
                .columnPermissionRules(columnPermissionRules)
                .build();
    }

    /**
     * 加载数据权限规则（行级/数据范围/自定义SQL）
     *
     * @param tenantId 租户ID
     * @return 数据权限规则列表
     */
    private List<DataPermissionRule> loadDataPermissionRules(Long tenantId) {
        if (tenantId == null) {
            log.warn("租户ID为空，跳过数据权限规则加载");
            return Collections.emptyList();
        }
        List<SysDataPermissionRuleEntity> entities = sysDataPermissionRuleService.listValidRules(tenantId, null);
        if (CollectionUtils.isEmpty(entities)) {
            log.info("租户{}无有效数据权限规则", tenantId);
            return Collections.emptyList();
        }
        log.info("查询到数据权限规则{}条", entities.size());
        return entities.stream()
                .map(this::convertToDataPermissionRule)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 加载字段级权限规则
     *
     * @param tenantId 租户ID
     * @return 字段权限规则列表
     */
    private List<ColumnPermissionRule> loadColumnPermissionRules(Long tenantId) {
        if (tenantId == null) {
            log.warn("租户ID为空，跳过字段权限规则加载");
            return Collections.emptyList();
        }
        List<SysColumnPermissionRuleEntity> entities = sysColumnPermissionRuleService.list(
                QueryWrapper.create()
                        .where(SysColumnPermissionRuleEntity::getTenantId).eq(tenantId)
                        .and(SysColumnPermissionRuleEntity::getIsEnabled).eq(1)
                        .and(SysColumnPermissionRuleEntity::getDeleted).eq(0)
                        .orderBy(SysColumnPermissionRuleEntity::getPriority).asc());
        if (CollectionUtils.isEmpty(entities)) {
            log.info("租户{}无有效字段权限规则", tenantId);
            return Collections.emptyList();
        }
        log.info("查询到字段权限规则{}条", entities.size());
        return entities.stream()
                .map(this::convertToColumnPermissionRule)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 将 SysDataPermissionRuleEntity 转换为 DataPermissionRule
     */
    private DataPermissionRule convertToDataPermissionRule(SysDataPermissionRuleEntity entity) {
        if (entity == null) {
            return null;
        }
        return DataPermissionRule.builder()
                .ruleName(entity.getRuleName())
                .ruleCode(entity.getRuleCode())
                .ruleTypeEnum(convertDataRuleType(entity.getRuleTypeEnum()))
                .targetTable(entity.getTargetTable())
                .targetField(entity.getTargetField())
                .permissionColumn(entity.getPermissionColumn())
                .permOperatorEnum(convertPermOperator(entity.getPermOperatorEnum()))
                .permissionValue(entity.getPermissionValue())
                .filterCondition(entity.getFilterCondition())
                .dataScopeEnum(convertDataScope(entity.getDataScopeEnum()))
                .priority(entity.getPriority())
                .isDefault(entity.getIsDefault())
                .isEnabled(entity.getIsEnabled())
                .build();
    }

    /**
     * 将 SysColumnPermissionRuleEntity 转换为 ColumnPermissionRule
     */
    private ColumnPermissionRule convertToColumnPermissionRule(SysColumnPermissionRuleEntity entity) {
        if (entity == null) {
            return null;
        }
        return ColumnPermissionRule.builder()
                .ruleName(entity.getRuleName())
                .ruleCode(entity.getRuleCode())
                .tableName(entity.getTableName())
                .columnName(entity.getColumnName())
                .columnNames(entity.getColumnNames())
                .permissionType(entity.getPermissionType())
                .filterCondition(entity.getFilterCondition())
                .priority(entity.getPriority())
                .isDefault(entity.getIsDefault())
                .isEnabled(entity.getIsEnabled())
                .build();
    }

    /**
     * 转换数据规则类型枚举（system模块 → common-core）
     */
    private com.lrcore.common.core.enums.perm.DataRuleTypeEnum convertDataRuleType(
            com.lrcore.system.enums.DataRuleTypeEnum systemEnum) {
        if (systemEnum == null) {
            return null;
        }
        return com.lrcore.common.core.enums.perm.DataRuleTypeEnum.getDataRuleTypeEnumByCode(systemEnum.getCode());
    }

    /**
     * 转换权限操作符枚举（system模块 → common-core）
     */
    private com.lrcore.common.core.enums.perm.PermOperatorEnum convertPermOperator(
            com.lrcore.system.enums.PermOperatorEnum systemEnum) {
        if (systemEnum == null) {
            return null;
        }
        try {
            return com.lrcore.common.core.enums.perm.PermOperatorEnum.valueOf(systemEnum.name());
        } catch (IllegalArgumentException e) {
            log.warn("无法转换权限操作符枚举: {}", systemEnum.name());
            return null;
        }
    }

    /**
     * 转换数据范围枚举（system模块 → common-core）
     */
    private com.lrcore.common.core.enums.perm.DataScopeEnum convertDataScope(
            com.lrcore.system.enums.DataScopeEnum systemEnum) {
        if (systemEnum == null) {
            return null;
        }
        try {
            return com.lrcore.common.core.enums.perm.DataScopeEnum.valueOf(systemEnum.name());
        } catch (IllegalArgumentException e) {
            log.warn("无法转换数据范围枚举: {}", systemEnum.name());
            return null;
        }
    }
}
