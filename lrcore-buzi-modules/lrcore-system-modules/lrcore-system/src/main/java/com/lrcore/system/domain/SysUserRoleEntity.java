package com.lrcore.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

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
@ToString
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_user_role")
@Schema(description = "用户-角色关联实体")
public class SysUserRoleEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "租户ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column("tenant_id")
    private Long tenantId;

    @Schema(description = "创建者ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column("create_user_id")
    private Long createUserId;

    @Schema(description = "更新者ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column("update_user_id")
    private Long updateUserId;

    @Schema(description = "创建时间")
    @Column("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @Column("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    @Schema(description = "备注")
    @Column("remark")
    private String remark;

    @Schema(description = "删除标志（0未删除 1已删除）")
    @Column("deleted")
    private Integer deleted;

    @Schema(description = "用户ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Id
    private Long userId;

    @Schema(description = "角色ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Id
    private Long roleId;

    @Schema(description = "关联状态（0有效 1无效）")
    private Integer relationStatus;

    @Schema(description = "是否内置（0否 1是）")
    @Column("build_in")
    private Integer buildIn;

}
