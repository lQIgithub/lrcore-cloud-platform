package com.lrcore.system.service;


import com.lrcore.system.domain.SysPermissionEntity;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 权限信息表 服务层。
 *
 * @author mybatis-flex-helper automatic generation
 * @since 1.0
 */
public interface ISysPermissionService extends IService<SysPermissionEntity> {

    /**
     * 获取所有子权限ID（递归）
     *
     * @param parentId 父权限ID
     * @return 子权限ID列表
     */
    List<Long> getAllChildPermissionIds(Long parentId);

    /**
     * 根据父权限ID查询子权限列表
     *
     * @param parentId 父权限ID
     * @return 子权限列表
     */
    List<SysPermissionEntity> getChildrenByParentId(Long parentId);

    /**
     * 构建权限树
     *
     * @param permissions 权限列表
     * @param parentId    父权限ID
     * @return 权限树列表
     */
    List<SysPermissionEntity> buildPermissionTree(List<SysPermissionEntity> permissions, Long parentId);

    /**
     * 获取权限完整路径
     *
     * @param permissionId 权限ID
     * @return 权限路径列表（从根到当前权限）
     */
    List<SysPermissionEntity> getPermissionPath(Long permissionId);

    /**
     * 获取权限路径字符串
     *
     * @param permissionId 权限ID
     * @param separator    分隔符
     * @return 路径字符串
     */
    String getPermissionPathString(Long permissionId, String separator);

    /**
     * 递归删除权限（包括所有子权限）
     *
     * @param permissionId 权限ID
     * @return 删除数量
     */
    int deletePermissionWithChildren(Long permissionId);

    /**
     * 更新权限及其子权限的租户ID
     *
     * @param permissionId 权限ID
     * @param tenantId     租户ID
     * @return 更新数量
     */
    int updateTenantIdWithChildren(Long permissionId, Long tenantId);

    /**
     * 启用权限及其所有子权限
     *
     * @param permissionId 权限ID
     * @return 更新数量
     */
    int enablePermissionWithChildren(Long permissionId);

    /**
     * 禁用权限及其所有子权限
     *
     * @param permissionId 权限ID
     * @return 更新数量
     */
    int disablePermissionWithChildren(Long permissionId);

    /**
     * 根据应用ID查询权限树
     *
     * @param appId 应用ID
     * @return 权限树列表
     */
    List<SysPermissionEntity> getPermissionTreeByAppId(Long appId);

    /**
     * 根据权限类型查询权限树
     *
     * @param permissionType 权限类型
     * @return 权限树列表
     */
    List<SysPermissionEntity> getPermissionTreeByType(Integer permissionType);

    /**
     * 获取根权限列表
     *
     * @return 根权限列表
     */
    List<SysPermissionEntity> getRootPermissions();

    /**
     * 检查是否存在子权限
     *
     * @param parentId 父权限ID
     * @return 是否存在子权限
     */
    boolean hasChildren(Long parentId);

    /**
     * 获取权限层级深度
     *
     * @param permissionId 权限ID
     * @return 层级深度
     */
    int getPermissionLevel(Long permissionId);

    /**
     * 获取用户分配权限的标识符列表
     *
     * @param userId 用户ID
     * @return 权限信息列表
     */
    List<SysPermissionEntity> getSysPermissionInfoList(Long userId);
}
