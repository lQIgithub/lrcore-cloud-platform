package com.lrcore.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lrcore.common.core.web.domain.BaseEntity;
import com.lrcore.system.enums.DataRuleTypeEnum;
import com.lrcore.system.enums.DataScopeEnum;
import com.lrcore.system.enums.PermOperatorEnum;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * <p>数据权限规则表 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>定义数据权限规则</li>
 *   <li>支持行级和字段级权限控制</li>
 *   <li>支持自定义SQL权限规则</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_data_permission_rule")
@Schema(description = "数据权限规则实体")
public class SysDataPermissionRuleEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "规则名称")
    @NotBlank(message = "规则名称不能为空")
    @Size(max = 100, message = "规则名称长度不能超过100个字符")
    private String ruleName;

    @Schema(description = "规则编码")
    @NotBlank(message = "规则编码不能为空")
    @Size(max = 64, message = "规则编码长度不能超过64个字符")
    private String ruleCode;

    @Schema(description = "规则类型：1=行级 2=字段级 3=数据范围 4=自定义SQL")
    @Column("rule_type")
    private DataRuleTypeEnum ruleTypeEnum;

    @Schema(description = "目标表名")
    @Size(max = 100, message = "目标表名长度不能超过100个字符")
    private String targetTable;

    @Schema(description = "目标字段名")
    @Size(max = 100, message = "目标字段名长度不能超过100个字符")
    private String targetField;

    @Schema(description = "权限字段")
    @Size(max = 100, message = "权限字段长度不能超过100个字符")
    private String permissionColumn;

    @Schema(description = "权限操作符：1== 2=!= 3=IN 4=NOT IN 5=LIKE 6=BETWEEN")
    @Column("perm_operator")
    private PermOperatorEnum permOperatorEnum;

    @Schema(description = "权限值")
    @Size(max = 500, message = "权限值长度不能超过500个字符")
    private String permissionValue;

    @Schema(description = "过滤条件")
    private String filterCondition;

    @Schema(description = "数据范围：1=全部 2=本企业 3=本部门 4=本部门及下级 5=本人 6=自定义")
    @Column("data_scope")
    private DataScopeEnum dataScopeEnum;

    @Schema(description = "规则优先级")
    private Integer priority;

    @Schema(description = "是否为默认规则")
    private Integer isDefault;

    @Schema(description = "是否启用")
    private Integer isEnabled;

    @Schema(description = "应用ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long appId;

}
