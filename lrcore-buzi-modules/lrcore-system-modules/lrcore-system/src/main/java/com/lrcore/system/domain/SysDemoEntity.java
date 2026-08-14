package com.lrcore.system.domain;

import com.lrcore.common.core.web.domain.BaseEntity;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * <p>演示信息表 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>用于测试和演示功能</li>
 *   <li>支持多租户场景</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_demo")
@Schema(description = "演示信息实体")
public class SysDemoEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "演示名称")
    @NotBlank(message = "演示名称不能为空")
    @Size(max = 100, message = "演示名称长度不能超过100个字符")
    private String demoName;

    @Schema(description = "演示描述")
    private String demoDesc;

    @Schema(description = "状态（0启用 1禁用）")
    private Integer status;

}
