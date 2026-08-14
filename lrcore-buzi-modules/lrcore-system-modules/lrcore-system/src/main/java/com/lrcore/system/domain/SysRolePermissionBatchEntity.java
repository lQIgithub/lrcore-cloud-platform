package com.lrcore.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lrcore.common.core.web.domain.BaseEntity;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>角色权限批量操作记录表 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>记录角色权限的批量操作</li>
 *   <li>支持批量授予、撤销、替换操作</li>
 *   <li>记录操作结果和统计信息</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_role_permission_batch")
@Schema(description = "角色权限批量操作记录实体")
public class SysRolePermissionBatchEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "批次号")
    @NotBlank(message = "批次号不能为空")
    @Size(max = 64, message = "批次号长度不能超过64个字符")
    private String batchNo;

    @Schema(description = "角色ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long roleId;

    @Schema(description = "权限ID列表（JSON格式）")
    private String permissionIds;

    @Schema(description = "操作类型：1=批量授予 2=批量撤销 3=批量替换")
    private Integer operationType;

    @Schema(description = "状态：0=待处理 1=处理中 2=成功 3=失败")
    private Integer status;

    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failCount;

    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "操作人ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "应用ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long appId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

}
