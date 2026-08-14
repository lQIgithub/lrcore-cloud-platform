package com.lrcore.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lrcore.common.core.web.domain.BaseEntity;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>权限继承关系表 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>管理权限之间的继承关系</li>
 *   <li>支持多种继承类型（完全、部分、覆盖、互斥）</li>
 *   <li>支持继承比例配置</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_permission_inheritance")
@Schema(description = "权限继承关系实体")
public class SysPermissionInheritanceEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "父权限ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long parentPermissionId;

    @Schema(description = "子权限ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long childPermissionId;

    @Schema(description = "继承类型：1=完全 2=部分 3=覆盖 4=互斥")
    private Integer inheritanceType;

    @Schema(description = "继承比例")
    private BigDecimal inheritanceRatio;

    @Schema(description = "是否允许覆盖")
    private Integer overrideEnabled;

    @Schema(description = "优先级")
    private Integer priority;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "生效开始时间")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "生效结束时间")
    private LocalDateTime endTime;

    @Schema(description = "状态：0=有效 1=无效 2=过期")
    private Integer status;

    @Schema(description = "应用ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long appId;

}
