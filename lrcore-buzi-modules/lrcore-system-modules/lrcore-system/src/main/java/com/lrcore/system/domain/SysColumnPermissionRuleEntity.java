package com.lrcore.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lrcore.common.core.web.domain.BaseEntity;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * <p>字段权限规则表 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>定义角色/用户对特定字段的权限规则</li>
 *   <li>支持多种权限类型（可见、可编辑、只读、隐藏、加密显示）</li>
 *   <li>支持规则优先级配置</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_column_permission_rule")
@Schema(description = "字段权限规则实体")
public class SysColumnPermissionRuleEntity extends BaseEntity {

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

    @Schema(description = "目标表名")
    @NotBlank(message = "目标表名不能为空")
    @Size(max = 100, message = "目标表名长度不能超过100个字符")
    private String tableName;

    @Schema(description = "目标字段名（NULL表示所有字段）")
    @Size(max = 100, message = "目标字段名长度不能超过100个字符")
    private String columnName;

    @Schema(description = "字段列表（JSON数组格式，当column_name为NULL时使用）")
    private String columnNames;

    @Schema(description = "字段权限类型：1=可见 2=可编辑 3=只读 4=隐藏 5=加密显示")
    private Integer permissionType;

    @Schema(description = "过滤条件（JSON格式，如：{\"role_code\": \"admin\"}）")
    @Size(max = 500, message = "过滤条件长度不能超过500个字符")
    private String filterCondition;

    @Schema(description = "规则优先级（数字越小优先级越高）")
    private Integer priority;

    @Schema(description = "是否默认规则（0=否 1=是）")
    private Integer isDefault;

    @Schema(description = "是否启用（0=否 1=是）")
    private Integer isEnabled;

    @Schema(description = "应用ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long appId;

}
