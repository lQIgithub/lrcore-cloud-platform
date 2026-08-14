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
 * <p>用户字段权限覆盖表 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>为特定用户设置字段级权限（覆盖角色权限）</li>
 *   <li>支持权限覆盖和继承配置</li>
 *   <li>支持生效时间配置</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_user_column_permission")
@Schema(description = "用户字段权限覆盖实体")
public class SysUserColumnPermissionEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;

    @Schema(description = "数据表名称")
    private String tableName;

    @Schema(description = "字段名称")
    private String columnName;

    @Schema(description = "字段权限类型：1=可见 2=可编辑 3=只读 4=隐藏 5=加密显示")
    private Integer permissionType;

    @Schema(description = "是否覆盖角色权限（0=继承 1=覆盖）")
    private Integer isOverride;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "生效时间")
    private LocalDateTime effectiveTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "失效时间")
    private LocalDateTime expireTime;

    @Schema(description = "状态（0=有效 1=无效）")
    private Integer status;

    @Schema(description = "应用ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long appId;

}
