package com.lrcore.system.service.workflow;

import com.lrcore.common.flowable.model.task.ProcessTaskVO;

import java.util.Map;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 流程任务 服务层（待办查询/详情/签收/通过/驳回，打通审批主链路）
 * @ClassName: IWorkflowTaskService
 * @Author: lrcore
 * @Date: 2026/09/02
 * @Version: 1.0
 */
public interface IWorkflowTaskService {

    /**
     * 待办任务列表（当前用户候选或已处理人），分页返回 {list, total}
     *
     * @param processInstanceId 流程实例ID，空表示不过滤
     * @param status            任务状态，空表示不过滤（当前仅占位）
     * @param pageNum           页码，从1开始
     * @param pageSize          每页条数
     * @return {list: List&lt;ProcessTaskVO&gt;, total: long}
     */
    Map<String, Object> list(String processInstanceId, String status, Long pageNum, Long pageSize);

    /**
     * 获取任务详情
     *
     * @param id 任务ID
     * @return 任务VO
     */
    ProcessTaskVO getInfo(String id);

    /**
     * 签收任务（需当前用户未被他人签收）
     *
     * @param id 任务ID
     */
    void claim(String id);

    /**
     * 通过：完成任务并同步请假业务状态
     *
     * <p>node_apply（提交）→ pending；审批通过且实例结束 → approved；
     * 审批通过且实例仍在跑 → completed；approved=false → rejected。</p>
     *
     * @param id        任务ID
     * @param variables 变量（approved/comment 等）
     */
    void complete(String id, Map<String, Object> variables);

    /**
     * 驳回：任务回退到最近上游用户任务，写审批意见，请假状态置为 rejected
     *
     * @param id      任务ID
     * @param comment 驳回意见
     */
    void reject(String id, String comment);

    /**
     * 转办任务（本期保留端点，暂未实现）
     *
     * @param id           任务ID
     * @param targetUserId 目标用户ID
     */
    void transfer(String id, String targetUserId);

}
