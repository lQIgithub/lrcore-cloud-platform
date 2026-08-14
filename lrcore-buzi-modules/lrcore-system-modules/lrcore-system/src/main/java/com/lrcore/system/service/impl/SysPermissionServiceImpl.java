package com.lrcore.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.lrcore.common.core.enums.PermimssionStatusEnum;
import com.lrcore.common.core.exception.ServiceException;
import com.lrcore.common.core.utils.FunCollectUtils;
import com.lrcore.system.domain.SysPermissionEntity;
import com.lrcore.system.domain.apt.SysPermissionAPT;
import com.lrcore.system.domain.apt.SysRolePermissionAPT;
import com.lrcore.system.domain.apt.SysUserRoleAPT;
import com.lrcore.system.mapper.SysPermissionMapper;
import com.lrcore.system.service.ISysPermissionService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 权限信息 服务类
 * @ClassName: SysPermissionServiceImpl
 * @Author: Qi Liu
 * @Date: 2026/5/16 10:49
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermissionEntity> implements ISysPermissionService {

    /**
     * 获取所有子权限ID（递归）
     *
     * @param parentId 父权限ID
     * @return 子权限ID列表
     */
    @Override
    public List<Long> getAllChildPermissionIds(Long parentId) {
        List<Long> childIds = new ArrayList<>();
        findChildPermissionIds(parentId, childIds);
        return childIds;
    }

    /**
     * 递归查找子权限ID
     *
     * @param parentId 父权限ID
     * @param childIds 子权限ID列表
     */
    private void findChildPermissionIds(Long parentId, List<Long> childIds) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(SysPermissionAPT.SYS_PERMISSION.PID.eq(parentId))
                .and(SysPermissionAPT.SYS_PERMISSION.DELETED.eq(0));
        List<SysPermissionEntity> children = mapper.selectListByQuery(queryWrapper);

        for (SysPermissionEntity child : children) {
            childIds.add(child.getId());
            findChildPermissionIds(child.getId(), childIds);
        }
    }

    /**
     * 根据父权限ID查询子权限列表
     *
     * @param parentId 父权限ID
     * @return 子权限列表
     */
    @Override
    public List<SysPermissionEntity> getChildrenByParentId(Long parentId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(SysPermissionAPT.SYS_PERMISSION.PID.eq(parentId))
                .and(SysPermissionAPT.SYS_PERMISSION.DELETED.eq(0))
                .orderBy(SysPermissionAPT.SYS_PERMISSION.SORT.asc());
        List<SysPermissionEntity> list = mapper.selectListByQuery(queryWrapper);
        log.debug("根据父权限ID查询子权限列表成功, 父ID: {}, 数量: {}", parentId, list.size());
        return list;
    }

    /**
     * 构建权限树
     *
     * @param permissions 权限列表
     * @param parentId    父权限ID
     * @return 权限树列表
     */
    @Override
    public List<SysPermissionEntity> buildPermissionTree(List<SysPermissionEntity> permissions, Long parentId) {
        List<SysPermissionEntity> tree = permissions.stream()
                .filter(p -> Objects.equals(parentId, p.getId()))
                .map(p -> {
                    p.setChildren(buildPermissionTree(permissions, p.getId()));
                    return p;
                })
                .sorted((a, b) -> {
                    Integer sortA = a.getSort() != null ? a.getSort() : 0;
                    Integer sortB = b.getSort() != null ? b.getSort() : 0;
                    return sortA.compareTo(sortB);
                })
                .collect(Collectors.toList());
        return tree;
    }

    /**
     * 获取权限完整路径
     *
     * @param permissionId 权限ID
     * @return 权限路径列表（从根到当前权限）
     */
    @Override
    public List<SysPermissionEntity> getPermissionPath(Long permissionId) {
        List<SysPermissionEntity> path = new ArrayList<>();
        if (Objects.isNull(permissionId)) {
            return path;
        }

        SysPermissionEntity permission = mapper.selectOneById(permissionId);
        if (ObjectUtil.isNull(permission)) {
            throw new ServiceException("权限不存在");
        }

        path.add(permission);
        Long parentId = permission.getPid();

        while (parentId != null && parentId != 0) {
            SysPermissionEntity parent = mapper.selectOneById(parentId);
            if (ObjectUtil.isNull(parent)) {
                break;
            }
            path.add(0, parent);
            parentId = parent.getPid();
        }

        log.debug("获取权限路径成功, 权限ID: {}, 路径长度: {}", permissionId, path.size());
        return path;
    }

    /**
     * 获取权限路径字符串
     *
     * @param permissionId 权限ID
     * @param separator    分隔符
     * @return 路径字符串
     */
    @Override
    public String getPermissionPathString(Long permissionId, String separator) {
        List<SysPermissionEntity> path = getPermissionPath(permissionId);
        return path.stream()
                .map(SysPermissionEntity::getName)
                .collect(Collectors.joining(separator != null ? separator : " > "));
    }

    /**
     * 递归删除权限（包括所有子权限）
     *
     * @param permissionId 权限ID
     * @return 删除数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deletePermissionWithChildren(Long permissionId) {
        List<Long> allIds = getAllChildPermissionIds(permissionId);
        allIds.add(permissionId);

        int count = 0;
        for (Long id : allIds) {
            SysPermissionEntity permission = mapper.selectOneById(id);
            if (ObjectUtil.isNotNull(permission)) {
                permission.setDeleted(1);
                if (mapper.update(permission) > 0) {
                    count++;
                }
            }
        }

        log.info("递归删除权限成功, 权限ID: {}, 删除数量: {}", permissionId, count);
        return count;
    }

    /**
     * 更新权限及其子权限的租户ID
     *
     * @param permissionId 权限ID
     * @param tenantId     租户ID
     * @return 更新数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTenantIdWithChildren(Long permissionId, Long tenantId) {
        List<Long> allIds = getAllChildPermissionIds(permissionId);
        allIds.add(permissionId);

        int count = 0;
        for (Long id : allIds) {
            SysPermissionEntity permission = mapper.selectOneById(id);
            if (ObjectUtil.isNotNull(permission)) {
                permission.setTenantId(tenantId);
                if (mapper.update(permission) > 0) {
                    count++;
                }
            }
        }

        log.info("更新权限租户ID成功, 权限ID: {}, 租户ID: {}, 更新数量: {}", permissionId, tenantId, count);
        return count;
    }

    /**
     * 启用权限及其所有子权限
     *
     * @param permissionId 权限ID
     * @return 更新数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int enablePermissionWithChildren(Long permissionId) {
        List<Long> allIds = getAllChildPermissionIds(permissionId);
        allIds.add(permissionId);

        int count = 0;
        for (Long id : allIds) {
            SysPermissionEntity permission = mapper.selectOneById(id);
            if (ObjectUtil.isNotNull(permission)) {
                permission.setStatus(PermimssionStatusEnum.ACTIVATED);
                if (mapper.update(permission) > 0) {
                    count++;
                }
            }
        }

        log.info("启用权限及其子权限成功, 权限ID: {}, 更新数量: {}", permissionId, count);
        return count;
    }

    /**
     * 禁用权限及其所有子权限
     *
     * @param permissionId 权限ID
     * @return 更新数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int disablePermissionWithChildren(Long permissionId) {
        List<Long> allIds = getAllChildPermissionIds(permissionId);
        allIds.add(permissionId);

        int count = 0;
        for (Long id : allIds) {
            SysPermissionEntity permission = mapper.selectOneById(id);
            if (ObjectUtil.isNotNull(permission)) {
                permission.setStatus(PermimssionStatusEnum.DISABLE);
                if (mapper.update(permission) > 0) {
                    count++;
                }
            }
        }

        log.info("禁用权限及其子权限成功, 权限ID: {}, 更新数量: {}", permissionId, count);
        return count;
    }

    /**
     * 根据应用ID查询权限树
     *
     * @param appId 应用ID
     * @return 权限树列表
     */
    @Override
    public List<SysPermissionEntity> getPermissionTreeByAppId(Long appId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(SysPermissionAPT.SYS_PERMISSION.APP_ID.eq(appId))
                .and(SysPermissionAPT.SYS_PERMISSION.DELETED.eq(0))
                .orderBy(SysPermissionAPT.SYS_PERMISSION.SORT.asc());
        List<SysPermissionEntity> permissions = mapper.selectListByQuery(queryWrapper);
        return buildPermissionTree(permissions, 0L);
    }

    /**
     * 根据权限类型查询权限树
     *
     * @param permissionType 权限类型
     * @return 权限树列表
     */
    @Override
    public List<SysPermissionEntity> getPermissionTreeByType(Integer permissionType) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(SysPermissionAPT.SYS_PERMISSION.TYPE.eq(permissionType))
                .and(SysPermissionAPT.SYS_PERMISSION.DELETED.eq(0))
                .orderBy(SysPermissionAPT.SYS_PERMISSION.SORT.asc());
        List<SysPermissionEntity> permissions = mapper.selectListByQuery(queryWrapper);
        return buildPermissionTree(permissions, 0L);
    }

    /**
     * 获取根权限列表
     *
     * @return 根权限列表
     */
    @Override
    public List<SysPermissionEntity> getRootPermissions() {
        return getChildrenByParentId(0L);
    }

    /**
     * 检查是否存在子权限
     *
     * @param parentId 父权限ID
     * @return 是否存在子权限
     */
    @Override
    public boolean hasChildren(Long parentId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(SysPermissionAPT.SYS_PERMISSION.PID.eq(parentId))
                .and(SysPermissionAPT.SYS_PERMISSION.DELETED.eq(0));
        return mapper.selectCountByQuery(queryWrapper) > 0;
    }

    /**
     * 获取权限层级深度
     *
     * @param permissionId 权限ID
     * @return 层级深度
     */
    @Override
    public int getPermissionLevel(Long permissionId) {
        return getPermissionPath(permissionId).size();
    }

    @Override
    public List<SysPermissionEntity> getSysPermissionInfoList(Long userId) {
        log.info("获取用户[{}]所有权限信息列表", userId);

        if (Objects.isNull(userId)) {
            log.warn("用户ID为空，无法查询权限信息");
            return Collections.emptyList();
        }

        try {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .select(SysPermissionAPT.SYS_PERMISSION.DEFAULT_COLUMNS)
                    .from(SysPermissionAPT.SYS_PERMISSION)
                    .innerJoin(SysRolePermissionAPT.SYS_ROLE_PERMISSION)
                    .on(SysPermissionAPT.SYS_PERMISSION.ID.eq(SysRolePermissionAPT.SYS_ROLE_PERMISSION.PERMISSION_ID))
                    .innerJoin(SysUserRoleAPT.SYS_USER_ROLE)
                    .on(SysRolePermissionAPT.SYS_ROLE_PERMISSION.ROLE_ID.eq(SysUserRoleAPT.SYS_USER_ROLE.ROLE_ID))
                    .where(SysUserRoleAPT.SYS_USER_ROLE.USER_ID.eq(userId))
                    .and(SysPermissionAPT.SYS_PERMISSION.STATUS.eq(1))
                    .and(SysPermissionAPT.SYS_PERMISSION.DELETED.eq(0));

            List<SysPermissionEntity> result = mapper.selectListByQuery(queryWrapper);
            log.info("查询到用户[{}]的权限信息数量: {}", userId, FunCollectUtils.size(result));
            return result;

        } catch (Exception e) {
            log.error("查询用户[{}]权限信息失败: {}", userId, e.getMessage(), e);
            throw new ServiceException("查询权限信息失败: " + e.getMessage());
        }
    }
}
