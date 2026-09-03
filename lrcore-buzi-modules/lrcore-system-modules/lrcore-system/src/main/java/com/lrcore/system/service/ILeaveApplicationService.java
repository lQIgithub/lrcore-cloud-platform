package com.lrcore.system.service;

import com.lrcore.system.domain.LeaveApplicationEntity;
import com.lrcore.system.enums.LeaveStatus;
import com.mybatisflex.core.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 请假申请 服务层
 * @ClassName: ILeaveApplicationService
 * @Author: lrcore
 * @Date: 2026/09/02
 * @Version: 1.0
 */
public interface ILeaveApplicationService extends IService<LeaveApplicationEntity> {

    /**
     * 启动流程前落库：从流程变量中抽取请假业务字段保存
     *
     * @param businessKey 流程业务主键
     * @param applyUserId 申请人ID
     * @param variables   流程变量（leaveType/startDate/endDate/days/reason 等）
     * @return 已保存的请假记录
     */
    LeaveApplicationEntity buildAndSave(String businessKey, Long applyUserId, Map<String, Object> variables);

    /**
     * 流程启动成功后回写流程实例ID
     *
     * @param businessKey       流程业务主键
     * @param processInstanceId 流程实例ID
     */
    void bindProcessInstance(String businessKey, String processInstanceId);

    /**
     * 按流程实例ID更新请假业务状态
     *
     * @param processInstanceId 流程实例ID
     * @param status            目标状态
     */
    void markStatusByInstance(String processInstanceId, LeaveStatus status);

    /**
     * 按业务主键查询请假记录
     *
     * @param businessKey 流程业务主键
     * @return 请假记录，不存在返回 null
     */
    LeaveApplicationEntity getByBusinessKey(String businessKey);

    /**
     * 查询指定申请人（当前用户）的请假记录列表
     *
     * @param applyUserId 申请人ID
     * @return 请假记录列表（未删除）
     */
    List<LeaveApplicationEntity> listByApplyUser(Long applyUserId);

}
