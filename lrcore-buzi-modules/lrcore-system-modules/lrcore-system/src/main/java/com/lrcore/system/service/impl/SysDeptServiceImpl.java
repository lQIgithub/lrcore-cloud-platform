package com.lrcore.system.service.impl;

import com.lrcore.common.core.exception.ServiceException;
import com.lrcore.common.core.utils.FunCollectUtils;
import com.lrcore.common.core.utils.SecurityUtils;
import com.lrcore.common.core.utils.tree.TreeUtils;
import com.lrcore.common.core.web.domain.TreeNode;
import com.lrcore.common.core.web.domain.login.LoginUser;
import com.lrcore.common.core.web.domain.login.SysDeptInfo;
import com.lrcore.common.core.web.domain.login.SysEnterpriseInfo;
import com.lrcore.system.domain.SysDeptEntity;
import com.lrcore.system.domain.SysEnterpriseEntity;
import com.lrcore.system.domain.SysUserEnterpriseEntity;
import com.lrcore.system.domain.SysUserEntity;
import com.lrcore.system.domain.apt.SysEnterpriseAPT;
import com.lrcore.system.mapper.SysDeptMapper;
import com.lrcore.system.service.ISysDeptService;
import com.lrcore.system.service.ISysEnterpriseService;
import com.lrcore.system.service.ISysUserEnterpriseService;
import com.lrcore.system.service.ISysUserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 部门管理 服务实现
 * @ClassName: SysDeptServiceImpl
 * @Author: Qi Liu
 * @Date: 2026/5/16 08:51
 * @Version: 1.0
 */
@Slf4j
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDeptEntity> implements ISysDeptService {

    private final ISysUserService userService;

    private final ISysUserEnterpriseService userEnterpriseService;
    private final ISysEnterpriseService sysEnterpriseService;

    public SysDeptServiceImpl(@Lazy ISysUserService userService, ISysUserEnterpriseService userEnterpriseService,
                              ISysEnterpriseService sysEnterpriseService) {
        this.userService = userService;
        this.userEnterpriseService = userEnterpriseService;
        this.sysEnterpriseService = sysEnterpriseService;
    }


    @Override
    public List<TreeNode> getDeptTree() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Assert.notNull(loginUser, "用户未登录，请登录!");
        List<SysDeptEntity> sysDeptEntities = this.list();
        Assert.notNull(sysDeptEntities, "部门列表不能为空");
        List<SysEnterpriseEntity> enterpriseEntities = null;
        if (loginUser.isAdmin()) { // 超级管理员
            enterpriseEntities = sysEnterpriseService.list();
        } else { // 普通账户
            List<SysEnterpriseInfo> managerEnterprises = loginUser.getManagerEnterprises();// 当前用户可管理企业
            List<Long> enterpriseIds = new ArrayList<>();
            if (FunCollectUtils.isNotEmpty(managerEnterprises)) {
                enterpriseIds.addAll(managerEnterprises.stream().map(SysEnterpriseInfo::getEnterpriseId).toList());
            } else {
                enterpriseIds.add(loginUser.getEnterpriseId());
            }
            QueryWrapper queryWrapper = QueryWrapper.create().where(SysEnterpriseAPT.SYS_ENTERPRISE.ID.in(enterpriseIds));
            enterpriseEntities = sysEnterpriseService.list(queryWrapper);
        }
        Assert.notNull(enterpriseEntities, "企业列表不能为空");
        // 组装企业树形结构
        List<TreeNode> treeNodes = TreeUtils.buildTreeNodeTree(
                enterpriseEntities,
                entity -> TreeNode.builder().nodeId(entity.getId())
                        .parentNodeId(entity.getParentId()).nodeName(entity.getEntName()).build(),
                0L
        );
        // 迭代每个企业，挂载企业的部门tree结构
        treeNodes.forEach(treeNode -> {
            List<SysDeptEntity> entAllDepts = sysDeptEntities.stream().filter(dept ->
                    Objects.equals(dept.getEnterpriseId(), treeNode.getNodeId())).toList();
            // 组装部门树形结构
            List<TreeNode> childTreeNodes = TreeUtils.buildTreeNodeTree(
                    entAllDepts,
                    dept -> TreeNode.builder().nodeId(dept.getId())
                            .parentNodeId(dept.getParentId()).nodeName(dept.getDeptName()).build(),
                    0L
            );
            // 挂载部门数据
            treeNode.setChildren(childTreeNodes);
        });
        return treeNodes;
    }


    @Override
    public List<SysDeptEntity> selectDeptTree(Long deptId) {
        log.info("查询部门树形结构，部门ID: {}", deptId);

        try {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .orderBy(SysDeptEntity::getId, true);

            if (deptId != null) {
                queryWrapper.where(SysDeptEntity::getParentId).eq(deptId);
            }

            List<SysDeptEntity> depts = this.list(queryWrapper);
            return buildDeptTree(depts, 0L);

        } catch (Exception e) {
            log.error("查询部门树形结构失败: {}", e.getMessage(), e);
            throw new ServiceException("查询部门树形结构失败: " + e.getMessage());
        }
    }

    @Override
    public List<SysDeptEntity> selectDeptsByEnterpriseId(Long enterpriseId) {
        log.info("根据企业ID查询部门列表，企业ID: {}", enterpriseId);

        if (enterpriseId == null) {
            log.warn("企业ID为空，返回空列表");
            return new ArrayList<>();
        }

        try {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .where(SysDeptEntity::getParentId).eq(enterpriseId)
                    .and(SysDeptEntity::getDeleted).eq("0")
                    .orderBy(SysDeptEntity::getId, true);

            return this.list(queryWrapper);

        } catch (Exception e) {
            log.error("根据企业ID[{}]查询部门列表失败: {}", enterpriseId, e.getMessage(), e);
            throw new ServiceException("查询部门列表失败: " + e.getMessage());
        }
    }

    @Override
    public List<SysDeptInfo> getSysDeptInfoList(Long userId) {
        log.info("获取用户[{}]所有管理部门信息列表", userId);

        if (Objects.isNull(userId)) {
            log.warn("用户ID为空，无法查询部门信息");
            return Collections.emptyList();
        }

        try {
            List<Long> enterpriseIds = this.getEnterpriseIdsByUserId(userId);
            if (enterpriseIds.isEmpty()) {
                log.warn("用户[{}]未关联任何企业", userId);
                return Collections.emptyList();
            }
            log.info("用户[{}]关联的企业ID列表: {}", userId, enterpriseIds);

            QueryWrapper queryWrapper = QueryWrapper.create()
                    .from(SysDeptEntity.class)
                    .where(SysDeptEntity::getParentId).in(enterpriseIds)
                    .and(SysDeptEntity::getDeleted).eq("0");

            List<SysDeptInfo> result = mapper.selectListByQueryAs(queryWrapper, SysDeptInfo.class);
            log.info("查询到用户[{}]的部门信息数量: {}", userId, result.size());
            return result;

        } catch (Exception e) {
            log.error("查询用户[{}]部门信息失败: {}", userId, e.getMessage(), e);
            throw new ServiceException("查询部门信息失败: " + e.getMessage());
        }
    }

    private List<Long> getEnterpriseIdsByUserId(Long userId) {
        List<Long> enterpriseIds = new ArrayList<>();

        try {
            QueryWrapper userEntQuery = QueryWrapper.create()
                    .where(SysUserEnterpriseEntity::getUserId).eq(userId);
            List<SysUserEnterpriseEntity> userEntList = userEnterpriseService.list(userEntQuery);

            if (!userEntList.isEmpty()) {
                enterpriseIds = userEntList.stream()
                        .map(SysUserEnterpriseEntity::getEnterpriseId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                log.info("从用户-企业关联表中查询到企业ID: {}", enterpriseIds);
                return enterpriseIds;
            }

            SysUserEntity user = userService.getById(userId);
            if (user != null && user.getEnterpriseId() != null) {
                enterpriseIds.add(user.getEnterpriseId());
                log.info("从用户表中查询到企业ID: {}", user.getEnterpriseId());
            }

        } catch (Exception e) {
            log.error("获取用户[{}]关联企业ID失败: {}", userId, e.getMessage(), e);
        }

        return enterpriseIds;
    }

    private List<SysDeptEntity> buildDeptTree(List<SysDeptEntity> depts, Long parentId) {
        List<SysDeptEntity> tree = new ArrayList<>();

        for (SysDeptEntity dept : depts) {
            if (parentId.equals(dept.getParentId())) {
                dept.setChildren(buildDeptTree(depts, dept.getId()));
                tree.add(dept);
            }
        }

        return tree;
    }
}
