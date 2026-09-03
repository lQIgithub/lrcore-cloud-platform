package com.lrcore.system.enums;

/**
 * <p>请假状态枚举</p>
 *
 * @Describe: pending待审批、approved已通过、rejected已驳回、completed已通过流程进行中、cancelled已撤销
 * @ClassName: LeaveStatus
 * @Date: 2026/09/02
 * @Version: 1.0
 */
public enum LeaveStatus {
    /** 待审批（已提交） */
    pending,
    /** 已通过（流程已结束） */
    approved,
    /** 已驳回 */
    rejected,
    /** 已通过但流程仍在进行 */
    completed,
    /** 已撤销 */
    cancelled
}
