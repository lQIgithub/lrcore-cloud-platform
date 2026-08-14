package com.lrcore.system.service;


import com.lrcore.common.core.web.domain.login.LoginUser;
import com.lrcore.common.core.web.domain.login.LoginUserDto;
import com.lrcore.common.core.web.domain.login.SysMenuInfo;
import com.lrcore.system.domain.SysUserEntity;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 用户基础信息表 服务层。
 *
 * @author mybatis-flex-helper automatic generation
 * @since 1.0
 */
public interface ISysUserService extends IService<SysUserEntity> {

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    LoginUserDto getByUserName(String username);

    /**
     * 获取当前登录用户权限，角色，菜单等信息
     *
     * @return 用户完整信息（包含用户基础信息、角色、权限、菜单）
     */
    LoginUser getInfo();

    List<SysMenuInfo> getSysMenuInfoList();
}
