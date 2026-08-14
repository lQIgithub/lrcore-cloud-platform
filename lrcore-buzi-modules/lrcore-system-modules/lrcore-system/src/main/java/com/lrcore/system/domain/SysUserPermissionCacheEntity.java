package com.lrcore.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lrcore.common.core.web.domain.BaseEntity;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>用户权限缓存表 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>缓存用户权限信息</li>
 *   <li>支持多种授予方式（直接、角色、部门）</li>
 *   <li>支持缓存版本和过期时间管理</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_user_permission_cache")
@Schema(description = "用户权限缓存实体")
public class SysUserPermissionCacheEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;

    @Schema(description = "权限编码")
    private String permissionCode;

    @Schema(description = "权限ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long permissionId;

    @Schema(description = "权限类型：1=菜单 2=按钮 3=接口")
    private Integer permissionType;

    @Schema(description = "数据权限范围")
    private Integer dataScope;

    @Schema(description = "授予方式：1=直接 2=角色 3=部门")
    private Integer grantType;

    @Schema(description = "角色ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long roleId;

    @Schema(description = "部门ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long deptId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "缓存版本号")
    private Integer cacheVersion;

    @Schema(description = "是否有效")
    private Integer isValid;

    @Schema(description = "应用ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long appId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "最后验证时间")
    private LocalDateTime lastVerifyTime;

    @Schema(description = "验证次数")
    private Integer verifyCount;

}
