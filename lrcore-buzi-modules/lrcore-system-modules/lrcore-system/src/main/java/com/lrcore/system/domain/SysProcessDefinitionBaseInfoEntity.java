package com.lrcore.system.domain;

import com.lrcore.common.core.annotation.Excel;
import com.lrcore.common.core.web.domain.BaseEntity;
import com.lrcore.common.flowable.enums.ProcessDefinitionStatus;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * <p>流程定义基础信息表 实体类</p>
 *
 * @Describe: 流程定义基础信息实体
 * @ClassName: SysProcessDefinitionBaseInfoEntity
 * @Author: lrcore
 * @Date: 2026/08/13
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>管理系统中的流程定义基本信息</li>
 *   <li>维护流程定义的分类、版本、状态等核心属性</li>
 *   <li>关联业务表单定义和流程部署信息</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "t_process_definition_base_info")
@Schema(description = "流程定义基础信息实体")
public class SysProcessDefinitionBaseInfoEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "流程定义id（Flowable act_re_procdef 主键）")
    @Column("act_re_procdef_id")
    @Size(max = 64, message = "act_re_procdef_id长度不能超过64个字符")
    private String actReProcdefId;

    @Schema(description = "流程标识key")
    @NotBlank(message = "流程标识key不能为空")
    @Size(max = 255, message = "流程标识key长度不能超过255个字符")
    @Excel(name = "流程标识")
    private String key;

    @Schema(description = "流程名称")
    @NotBlank(message = "流程名称不能为空")
    @Size(max = 255, message = "流程名称长度不能超过255个字符")
    @Excel(name = "流程名称")
    private String name;

    @Schema(description = "分类：系统通用审批；人事类审批；财务费用审批；采购资产类审批；业务项目审批；对外流程审批")
    @Size(max = 255, message = "分类长度不能超过255个字符")
    @Excel(name = "流程分类")
    private String category;

    @Schema(description = "流程描述信息")
    @Size(max = 255, message = "流程描述信息长度不能超过255个字符")
    @Excel(name = "流程描述")
    private String description;

    @Schema(description = "流程版本信息")
    @Size(max = 20, message = "流程版本信息长度不能超过20个字符")
    @Excel(name = "流程版本")
    private String version;

    @Schema(description = "流程图数据对象（JSON）")
    @Column("graph_data")
    private String graphData;

    @Schema(description = "bpmn xml字符串")
    @Column("bpmn_xml")
    private String bpmnXml;

    @Schema(description = "状态：draft草稿、deleted已删除、deployed已部署、archived已归档")
    @Size(max = 32, message = "状态长度不能超过32个字符")
    @Excel(name = "流程状态", readConverterExp = "draft=草稿,deleted=已删除,deployed=已部署,archived=已归档")
    private ProcessDefinitionStatus status;

    @Schema(description = "是否内置(0-否 1-是)")
    @Column("build_in")
    private Integer buildIn;

}
