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
 * <p>字段元数据表 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>存储数据表的字段元数据信息</li>
 *   <li>管理字段的敏感级别</li>
 *   <li>支持字段可见性配置</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_column_metadata")
@Schema(description = "字段元数据实体")
public class SysColumnMetadataEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "数据表名称")
    @NotBlank(message = "数据表名称不能为空")
    @Size(max = 100, message = "数据表名称长度不能超过100个字符")
    private String tableName;

    @Schema(description = "字段名称")
    @NotBlank(message = "字段名称不能为空")
    @Size(max = 100, message = "字段名称长度不能超过100个字符")
    private String columnName;

    @Schema(description = "字段显示名称")
    @Size(max = 100, message = "字段显示名称长度不能超过100个字符")
    private String columnLabel;

    @Schema(description = "字段类型（如：VARCHAR, INT, DATE等）")
    @NotBlank(message = "字段类型不能为空")
    @Size(max = 50, message = "字段类型长度不能超过50个字符")
    private String columnType;

    @Schema(description = "字段长度")
    private Integer columnLength;

    @Schema(description = "是否必填字段（0=否 1=是）")
    private Integer isRequired;

    @Schema(description = "是否主键（0=否 1=是）")
    private Integer isPrimaryKey;

    @Schema(description = "是否敏感字段（0=否 1=是）")
    private Integer isSensitive;

    @Schema(description = "敏感级别（1=低 2=中 3=高 4=极高）")
    private Integer sensitiveLevel;

    @Schema(description = "是否可见（0=隐藏 1=显示）")
    private Integer isVisible;

    @Schema(description = "默认值")
    @Size(max = 200, message = "默认值长度不能超过200个字符")
    private String defaultValue;

    @Schema(description = "字段描述")
    @Size(max = 500, message = "字段描述长度不能超过500个字符")
    private String description;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "应用ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long appId;

}
