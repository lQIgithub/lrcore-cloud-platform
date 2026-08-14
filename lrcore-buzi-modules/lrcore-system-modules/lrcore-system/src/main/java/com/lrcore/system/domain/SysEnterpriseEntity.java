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
 * <p>企业信息表 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>管理系统中的所有企业信息</li>
 *   <li>支持企业层级结构</li>
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
@Table(value = "sys_enterprise")
@Schema(description = "企业信息实体")
public class SysEnterpriseEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "企业名称")
    @NotBlank(message = "企业名称不能为空")
    @Size(max = 100, message = "企业名称长度不能超过100个字符")
    private String entName;

    @Schema(description = "企业简称")
    @Size(max = 50, message = "企业简称长度不能超过50个字符")
    private String entAbbreviation;

    @Schema(description = "企业编码（唯一标识）")
    @NotBlank(message = "企业编码不能为空")
    @Size(max = 50, message = "企业编码长度不能超过50个字符")
    private String entCode;

    @Schema(description = "上级企业ID（自关联，顶级企业为NULL）")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long parentId;

    @Schema(description = "企业描述")
    @Size(max = 500, message = "企业描述长度不能超过500个字符")
    private String entDesc;

    @Schema(description = "企业状态（0启用 1禁用）")
    private Integer status;

    @Schema(description = "祖级列表")
    @Size(max = 500, message = "祖级列表长度不能超过500个字符")
    private String ancestors;

    @Schema(description = "联系电话")
    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String phone;

    @Schema(description = "联系地址")
    @Size(max = 255, message = "联系地址长度不能超过255个字符")
    private String address;

}
