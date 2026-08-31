package com.lrcore.system.service;

import com.lrcore.common.flowable.enums.ProcessDefinitionStatus;
import com.lrcore.system.domain.SysProcessDefinitionBaseInfoEntity;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 流程定义基础信息 服务层
 * @ClassName: ISysProcessDefinitionBaseInfoService
 * @Author: lrcore
 * @Date: 2026/08/13
 * @Version: 1.0
 */
public interface ISysProcessDefinitionBaseInfoService extends IService<SysProcessDefinitionBaseInfoEntity> {

    /**
     * 查询工作流首页流程定义列表（主表 t_process_definition_base_info）
     *
     * <p>仅查未删除数据（deleted=0）；关键字模糊匹配流程名称/流程Key/描述；
     * 状态按生命周期状态（draft/deployed/archived）过滤；按更新时间倒序。
     * 不查询 graph_data/bpmn_xml 等大字段。</p>
     *
     * @param keyword 关键字，null/空表示不过滤
     * @param status  生命周期状态，null 表示不过滤
     * @return 流程定义主表实体列表
     */
    List<SysProcessDefinitionBaseInfoEntity> listDefinitions(String keyword, ProcessDefinitionStatus status);

    /**
     * 按主表ID 逻辑删除流程定义（deleted=1，列表不再展示，数据保留可追溯）
     *
     * @param id 主表主键
     * @return 受影响记录数（无对应未删除记录时为 0）
     */
    int logicDeleteById(Long id);

    /**
     * 部署成功后回写主表：引擎侧流程定义ID + 状态置为 deployed
     *
     * @param id              主表主键
     * @param actReProcdefId  Flowable 流程定义ID（新版本）
     */
    void markDeployed(Long id, String actReProcdefId);

}
