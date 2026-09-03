package com.lrcore.system.domain;

import com.lrcore.common.core.web.domain.BaseEntity;
import com.lrcore.system.enums.LeaveStatus;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * <p>请假申请表 实体类</p>
 *
 * @Describe: 请假申请业务实体，通过 business_key / process_instance_id 与 Flowable 流程实例关联
 * @ClassName: LeaveApplicationEntity
 * @Author: lrcore
 * @Date: 2026/09/02
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>记录请假申请的业务数据（类型/日期/天数/事由）</li>
 *   <li>以 businessKey 关联流程实例，回写 processInstanceId 与状态</li>
 *   <li>状态：pending待审批、approved已通过、rejected已驳回、completed已通过流程进行中、cancelled已撤销</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "t_leave_application")
@Schema(description = "请假申请表实体")
public class LeaveApplicationEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "业务主键（流程 businessKey）")
    @Column("business_key")
    @Size(max = 64, message = "business_key长度不能超过64个字符")
    private String businessKey;

    @Schema(description = "流程实例ID（启动后回写）")
    @Column("process_instance_id")
    @Size(max = 64, message = "process_instance_id长度不能超过64个字符")
    private String processInstanceId;

    @Schema(description = "申请人ID")
    @Column("apply_user_id")
    private Long applyUserId;

    @Schema(description = "申请人姓名")
    @Column("apply_user_name")
    @Size(max = 64, message = "apply_user_name长度不能超过64个字符")
    private String applyUserName;

    @Schema(description = "请假类型：1事假 2病假 3年假 4调休 5其他")
    @Column("leave_type")
    private Integer leaveType;

    @Schema(description = "开始日期")
    @Column("start_date")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    @Column("end_date")
    private LocalDate endDate;

    @Schema(description = "请假天数")
    @Column("days")
    private BigDecimal days;

    @Schema(description = "请假事由")
    @Size(max = 500, message = "reason长度不能超过500个字符")
    private String reason;

    @Schema(description = "状态：pending待审批 approved已通过 rejected已驳回 completed已通过流程进行中 cancelled已撤销")
    @Size(max = 20, message = "status长度不能超过20个字符")
    private LeaveStatus status;

}
