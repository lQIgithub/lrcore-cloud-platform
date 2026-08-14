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
 * <p>角色继承关系表 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>管理角色之间的继承关系</li>
 *   <li>支持多种继承类型（完全继承、部分继承、覆盖继承）</li>
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
@Table(value = "sys_role_inheritance")
@Schema(description = "角色继承关系实体")
public class SysRoleInheritanceEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "父角色ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long parentRoleId;

    @Schema(description = "子角色ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long childRoleId;

    @Schema(description = "继承类型：1=完全继承 2=部分继承 3=覆盖继承")
    private Integer inheritType;

    @Schema(description = "继承比例（百分比）")
    private BigDecimal inheritRatio;

    @Schema(description = "优先级")
    private Integer priority;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "生效时间")
    private LocalDateTime effectiveTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "失效时间")
    private LocalDateTime expireTime;

    @Schema(description = "状态：0=有效 1=无效")
    private Integer status;

}
