package com.lrcore.system.service;

import com.lrcore.common.core.web.domain.TreeNode;
import com.lrcore.common.core.web.domain.login.SysDeptInfo;
import com.lrcore.system.domain.SysDeptEntity;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 部门管理 服务层
 * @ClassName: ISysDeptService
 * @Author: Qi Liu
 * @Date: 2026/3/26 14:49
 * @Version: 1.0
 */
public interface ISysDeptService extends IService<SysDeptEntity> {

    /**
     * 查询部门树形结构
     *
     * @param deptId 部门ID（为空时查询所有）
     * @return 部门树列表
     */
    List<SysDeptEntity> selectDeptTree(Long deptId);

    /**
     * 根据企业ID查询部门列表
     *
     * @param enterpriseId 企业ID
     * @return 部门列表
     */
    List<SysDeptEntity> selectDeptsByEnterpriseId(Long enterpriseId);

    /**
     * 获取用户所属部门信息列表
     *
     * @param userId 用户ID
     * @return 部门信息列表
     */
    List<SysDeptInfo> getSysDeptInfoList(Long userId);

    /**
     * 获取登录用户管理企业及部门树形结构
     *
     * @return 部门树列表
     */
    List<TreeNode> getDeptTree();
}
