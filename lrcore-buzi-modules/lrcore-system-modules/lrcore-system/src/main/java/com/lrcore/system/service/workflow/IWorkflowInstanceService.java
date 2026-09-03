package com.lrcore.system.service.workflow;

import com.lrcore.common.flowable.model.instance.ProcessInstanceVO;
import com.lrcore.common.flowable.model.instance.StartInstanceVi;

import java.util.Map;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 流程实例 服务层（启动/查询/变量/终止/挂起激活，打通请假主链路）
 * @ClassName: IWorkflowInstanceService
 * @Author: lrcore
 * @Date: 2026/09/02
 * @Version: 1.0
 */
public interface IWorkflowInstanceService {

    /**
     * 启动流程实例并落库请假业务记录
     *
     * <p>本地事务：先保存请假记录 → Flowable 独立事务启动 → 回写实例ID；
     * 回写失败时补偿删除孤儿流程实例。</p>
     *
     * @param vi 启动参数（key 必填，variables 需含 leaveType/startDate/endDate/days/reason/manager）
     * @return 启动后的流程实例VO
     */
    ProcessInstanceVO start(StartInstanceVi vi);

    /**
     * 我的申请列表（当前用户发起的历史实例），分页返回 {list, total}
     *
     * @param processDefinitionKey 流程定义Key，空表示不过滤
     * @param businessKey          业务主键，空表示不过滤
     * @param status               实例状态（active/suspended/completed/terminated），空表示不过滤
     * @param pageNum              页码，从1开始
     * @param pageSize             每页条数
     * @return {list: List&lt;ProcessInstanceVO&gt;, total: long}
     */
    Map<String, Object> list(String processDefinitionKey, String businessKey, String status,
                             Long pageNum, Long pageSize);

    /**
     * 获取流程实例详情（历史实例 + 状态 + 变量）
     *
     * @param id 流程实例ID
     * @return 流程实例VO
     */
    ProcessInstanceVO getInfo(String id);

    /**
     * 查询流程变量
     *
     * @param id 流程实例ID
     * @return 变量 Map
     */
    Map<String, Object> getVariables(String id);

    /**
     * 设置流程变量
     *
     * @param id        流程实例ID
     * @param variables 变量 Map
     */
    void setVariables(String id, Map<String, Object> variables);

    /**
     * 终止并删除流程实例（运行时 + 历史），请假状态置为 cancelled
     *
     * @param id 流程实例ID
     */
    void delete(String id);

    /**
     * 挂起流程实例
     *
     * @param id 流程实例ID
     */
    void suspend(String id);

    /**
     * 激活流程实例
     *
     * @param id 流程实例ID
     */
    void activate(String id);

}
