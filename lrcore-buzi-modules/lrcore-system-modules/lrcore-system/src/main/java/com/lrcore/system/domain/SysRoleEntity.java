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
import java.time.LocalDateTime;

/**
 * <p>角色信息表 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>管理系统中的所有角色信息</li>
 *   <li>支持多租户场景</li>
 *   <li>支持角色类型区分（系统内置/自定义）</li>
 *   <li>支持数据权限范围配置</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_role")
@Schema(description = "角色信息实体")
public class SysRoleEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "角色名称")
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 100, message = "角色名称长度不能超过100个字符")
    private String roleName;

    @Schema(description = "角色编码")
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 64, message = "角色编码长度不能超过64个字符")
    private String roleCode;

    @Schema(description = "所属应用appID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long appId;

    @Schema(description = "角色类型：1=系统内置 2=自定义")
    private Integer roleType;

    @Schema(description = "角色状态（0启用 1禁用）")
    private Integer status;

    @Schema(description = "角色分类")
    @Size(max = 50, message = "角色分类长度不能超过50个字符")
    private String roleCategory;

    @Schema(description = "父角色ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long parentId;

    @Schema(description = "是否继承父角色权限")
    private Integer inheritEnabled;

    @Schema(description = "继承类型")
    private Integer inheritType;

    @Schema(description = "生效时间")
    private LocalDateTime effectiveTime;

    @Schema(description = "失效时间")
    private LocalDateTime expireTime;

    @Schema(description = "状态变更原因")
    @Size(max = 500, message = "状态变更原因长度不能超过500个字符")
    private String statusReason;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "版本号")
    private Integer versionNum;

}
