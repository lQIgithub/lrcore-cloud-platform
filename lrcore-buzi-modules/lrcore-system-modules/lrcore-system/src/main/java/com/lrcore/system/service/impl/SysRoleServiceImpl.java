package com.lrcore.system.service.impl;

import com.lrcore.common.core.exception.ServiceException;
import com.lrcore.common.core.utils.FunCollectUtils;
import com.lrcore.common.core.web.domain.login.SysRoleInfo;
import com.lrcore.system.domain.SysRoleEntity;
import com.lrcore.system.domain.apt.SysRoleAPT;
import com.lrcore.system.domain.apt.SysUserRoleAPT;
import com.lrcore.system.mapper.SysRoleMapper;
import com.lrcore.system.service.ISysRoleService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 角色信息 服务类
 * @ClassName: SysRoleServiceImpl
 * @Author: Qi Liu
 * @Date: 2026/5/16 10:50
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements ISysRoleService {

    @Override
    public List<SysRoleInfo> getSysRoleInfoList(Long userId) {
        log.info("获取用户[{}]所有角色信息列表", userId);

        if (Objects.isNull(userId)) {
            log.warn("用户ID为空，无法查询角色信息");
            return Collections.emptyList();
        }

        try {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .select(SysRoleAPT.SYS_ROLE.DEFAULT_COLUMNS)
                    .from(SysRoleAPT.SYS_ROLE)
                    .innerJoin(SysUserRoleAPT.SYS_USER_ROLE)
                    .on(SysRoleAPT.SYS_ROLE.ID.eq(SysUserRoleAPT.SYS_USER_ROLE.ROLE_ID))
                    .where(SysUserRoleAPT.SYS_USER_ROLE.USER_ID.eq(userId))
                    .and(SysRoleAPT.SYS_ROLE.STATUS.eq(0))
                    .and(SysRoleAPT.SYS_ROLE.DELETED.eq(0));

            List<SysRoleInfo> result = mapper.selectListByQueryAs(queryWrapper, SysRoleInfo.class);
            log.info("查询到用户[{}]的角色信息数量: {}", userId, FunCollectUtils.size(result));
            return result;

        } catch (Exception e) {
            log.error("查询用户[{}]角色信息失败: {}", userId, e.getMessage(), e);
            throw new ServiceException("查询角色信息失败: " + e.getMessage());
        }
    }
}
