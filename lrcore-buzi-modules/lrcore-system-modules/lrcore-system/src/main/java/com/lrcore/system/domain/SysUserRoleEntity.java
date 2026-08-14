package com.lrcore.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lrcore.common.core.web.domain.BaseEntity;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * <p>用户-角色关联表 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>管理用户与角色的关联关系</li>
 *   <li>支持多租户场景</li>
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
@Table(value = "sys_user_role")
@Schema(description = "用户-角色关联实体")
public class SysUserRoleEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;

    @Schema(description = "角色ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long roleId;

    @Schema(description = "关联状态（0有效 1无效）")
    private Integer relationStatus;

    @Schema(description = "数据权限范围")
    private Integer dataScope;

    @Schema(description = "数据权限类型")
    private Integer dataScopeType;

    @Schema(description = "自定义数据范围")
    private String customDataScope;

    @Schema(description = "字段级权限")
    private String fieldPermissions;

    @Schema(description = "行级权限")
    private String rowPermissions;

    @Schema(description = "用户角色字段级权限配置（JSON格式）")
    private String columnPermissions;

}
