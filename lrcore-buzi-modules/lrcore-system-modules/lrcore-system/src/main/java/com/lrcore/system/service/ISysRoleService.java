package com.lrcore.system.service;


import com.lrcore.common.core.web.domain.login.SysRoleInfo;
import com.lrcore.system.domain.SysRoleEntity;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 角色信息表 服务层。
 *
 * @author mybatis-flex-helper automatic generation
 * @since 1.0
 */
public interface ISysRoleService extends IService<SysRoleEntity> {

    /**
     * 获取用户分配的角色信息列表
     *
     * @param userId 用户ID
     * @return 角色信息列表
     */
    List<SysRoleInfo> getSysRoleInfoList(Long userId);
}
