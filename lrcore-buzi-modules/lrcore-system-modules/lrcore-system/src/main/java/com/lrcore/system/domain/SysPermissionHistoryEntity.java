package com.lrcore.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lrcore.common.core.web.domain.BaseEntity;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * <p>权限变更历史表 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>记录权限的所有变更历史</li>
 *   <li>支持版本管理和回滚</li>
 *   <li>记录变更原因和操作人</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_permission_history")
@Schema(description = "权限变更历史实体")
public class SysPermissionHistoryEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "权限ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long permissionId;

    @Schema(description = "版本号")
    private Integer versionNum;

    @Schema(description = "变更类型：1=新增 2=修改 3=删除 4=恢复")
    private Integer changeType;

    @Schema(description = "变更字段")
    private String changeField;

    @Schema(description = "旧值")
    private String oldValue;

    @Schema(description = "新值")
    private String newValue;

    @Schema(description = "变更原因")
    private String changeReason;

    @Schema(description = "回滚版本ID")
    private String rollbackId;

    @Schema(description = "是否为回滚点")
    private Integer isRollbackPoint;

    @Schema(description = "操作人ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "应用ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long appId;

}
