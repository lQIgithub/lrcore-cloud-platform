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
 * <p>权限组表 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>管理权限组信息</li>
 *   <li>支持分组层级结构</li>
 *   <li>支持多种分组类型（功能、业务、系统）</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_permission_group")
@Schema(description = "权限组实体")
public class SysPermissionGroupEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "权限组名称")
    @NotBlank(message = "权限组名称不能为空")
    @Size(max = 100, message = "权限组名称长度不能超过100个字符")
    private String groupName;

    @Schema(description = "权限组编码")
    @NotBlank(message = "权限组编码不能为空")
    @Size(max = 64, message = "权限组编码长度不能超过64个字符")
    private String groupCode;

    @Schema(description = "权限组描述")
    @Size(max = 500, message = "权限组描述长度不能超过500个字符")
    private String groupDesc;

    @Schema(description = "分组类型：1=功能 2=业务 3=系统")
    private Integer groupType;

    @Schema(description = "父分组ID")
    private String parentGroupId;

    @Schema(description = "分组层级")
    private Integer groupLevel;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "是否系统预置")
    private Integer isSystem;

    @Schema(description = "状态：0=启用 1=禁用")
    private Integer status;

    @Schema(description = "应用ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long appId;

}
