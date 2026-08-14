package com.lrcore.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lrcore.common.core.web.domain.BaseEntity;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>权限审计日志表 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>记录权限相关的所有操作审计日志</li>
 *   <li>支持审计类型区分（授予、撤销、变更、查询、删除等）</li>
 *   <li>记录操作人信息和请求详情</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_permission_audit_log")
@Schema(description = "权限审计日志实体")
public class SysPermissionAuditLogEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "审计类型：1=授予 2=撤销 3=变更 4=查询 5=删除 6=角色权限变更")
    private Integer auditType;

    @Schema(description = "目标类型：1=用户 2=角色 3=部门 4=企业")
    private Integer targetType;

    @Schema(description = "目标ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long targetId;

    @Schema(description = "权限ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long permissionId;

    @Schema(description = "权限编码")
    private String permissionCode;

    @Schema(description = "操作人ID")
    private String operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "操作人类型：1=系统管理员 2=普通管理员 3=普通用户")
    private Integer operatorType;

    @Schema(description = "应用ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long appId;

    @Schema(description = "请求IP地址")
    private String requestIp;

    @Schema(description = "请求URL")
    private String requestUrl;

    @Schema(description = "请求方法")
    private String requestMethod;

    @Schema(description = "请求参数（JSON格式）")
    private String requestParams;

    @Schema(description = "响应状态：0=成功 1=失败")
    private Integer responseStatus;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "执行时长（毫秒）")
    private Integer executionTime;

    @Schema(description = "浏览器信息")
    private String browserInfo;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "操作时间")
    private LocalDateTime operateTime;

}
