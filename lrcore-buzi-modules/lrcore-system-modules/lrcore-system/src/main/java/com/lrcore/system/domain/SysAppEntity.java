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
 * <p>应用系统表 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>管理系统中的所有应用信息</li>
 *   <li>支持多租户场景的应用管理</li>
 *   <li>配置应用的访问地址和图标</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_app")
@Schema(description = "应用系统实体")
public class SysAppEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "应用名称")
    @NotBlank(message = "应用名称不能为空")
    @Size(max = 100, message = "应用名称长度不能超过100个字符")
    private String appName;

    @Schema(description = "应用编码")
    @NotBlank(message = "应用编码不能为空")
    @Size(max = 64, message = "应用编码长度不能超过64个字符")
    private String appCode;

    @Schema(description = "应用描述")
    @Size(max = 500, message = "应用描述长度不能超过500个字符")
    private String appDesc;

    @Schema(description = "用于前后端访问的加解密秘钥")
    @Size(max = 500, message = "用于前后端访问的加解密秘钥长度不能超过50个字符")
    private String appEncrypt;

    @Schema(description = "应用状态（0启用 1禁用）")
    private Integer status;

    @Schema(description = "访问地址（URL）")
    @Size(max = 500, message = "访问地址长度不能超过500个字符")
    private String appUrl;

    @Schema(description = "应用图标")
    @Size(max = 200, message = "应用图标长度不能超过200个字符")
    private String appIcon;

    @Schema(description = "应用的权限标识符前缀")
    @Size(max = 10, message = "权限标识符前缀长度不能超过10个字符")
    private String permissionPrefix;

}
