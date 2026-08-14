package com.lrcore.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lrcore.common.core.web.domain.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 部门表 sys_department 实体类 - 存储部门的基本信息和层级关系
 * @ClassName: SysDeptEntity
 * @Author: lrcore
 * @Date: 2026/05/08
 * @Version: 1.0
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_dept")
@Schema(description = "部门信息实体")
public class SysDeptEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "部门名称")
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 100, message = "部门名称长度不能超过100个字符")
    private String deptName;

    @Schema(description = "父部门ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long parentId;

    @Schema(description = "企业ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long enterpriseId;

    @Schema(description = "祖级列表（存储祖级节点ID）")
    private String ancestors;

    @Schema(description = "部门编码")
    @Size(max = 50, message = "部门编码长度不能超过50个字符")
    private String deptCode;

    @Schema(description = "子部门列表")
    @Column(ignore = true)
    private List<SysDeptEntity> children = new ArrayList<>();

}
