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
 * <p>系统租户表 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>管理系统中的所有租户信息</li>
 *   <li>支持多租户隔离</li>
 *   <li>管理租户的状态和配置</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_tenant")
@Schema(description = "系统租户实体")
public class SysTenantEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "租户名称")
    @NotBlank(message = "租户名称不能为空")
    @Size(max = 100, message = "租户名称长度不能超过100个字符")
    private String tenantName;

    @Schema(description = "租户编码")
    @NotBlank(message = "租户编码不能为空")
    @Size(max = 64, message = "租户编码长度不能超过64个字符")
    private String tenantCode;

    @Schema(description = "租户描述")
    @Size(max = 500, message = "租户描述长度不能超过500个字符")
    private String tenantDesc;

    @Schema(description = "租户状态（0启用 1禁用）")
    private Integer status;

}
