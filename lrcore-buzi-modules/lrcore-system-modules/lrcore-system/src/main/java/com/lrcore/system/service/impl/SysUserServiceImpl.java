package com.lrcore.system.service.impl;


import com.lrcore.common.core.constant.CacheConstants;
import com.lrcore.common.core.enums.PermimssionStatusEnum;
import com.lrcore.common.core.enums.PermimssionTypeEnum;
import com.lrcore.common.core.enums.UserStatusEnum;
import com.lrcore.common.core.exception.ServiceException;
import com.lrcore.common.core.utils.FunCollectUtils;
import com.lrcore.common.core.utils.FunStrUtils;
import com.lrcore.common.core.utils.SecurityUtils;
import com.lrcore.common.core.web.domain.login.*;
import com.lrcore.common.redis.service.RedisService;
import com.lrcore.system.domain.SysPermissionEntity;
import com.lrcore.system.domain.SysUserEntity;
import com.lrcore.system.domain.apt.SysUserAPT;
import com.lrcore.system.mapper.SysUserMapper;
import com.lrcore.system.service.*;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 用户基础信息表 服务类
 * @ClassName: SysUserServiceImpl
 * @Author: Qi Liu
 * @Date: 2026/5/16 10:51
 * @Version: 1.0
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUserEntity> implements ISysUserService {

    private final ISysEnterpriseService sysEnterpriseService;
    private final ISysDeptService sysDeptService;
    private final ISysRoleService sysRoleService;
    private final ISysPermissionService sysPermissionService;
    private final ISysDataPermissionRuleService sysDataPermissionRuleService;
    private final RedisService redisService;

    @Override
    @Transactional(readOnly = true)
    public LoginUserDto getByUserName(String username) {
        try {
            log.info("根据用户名[{}]查询用户信息", username);

            if (FunStrUtils.isEmpty(username)) {
                log.warn("用户名为空，抛出异常");
                throw new ServiceException("用户名不能为空");
            }
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .where(SysUserAPT.SYS_USER.USERNAME.eq(username));
            SysUserEntity sysUserEntity = this.mapper.selectOneByQuery(queryWrapper);

            if (Objects.isNull(sysUserEntity)) {
                log.warn("查询不到用户[{}]资源库", username);
                throw new ServiceException("查询不到用户" + username + "资源库");
            }

            log.info("用户[{}]基本信息查询成功，用户ID: {}", username, sysUserEntity.getId());

            if (Objects.equals(1, sysUserEntity.getDeleted())) {
                log.warn("用户[{}]已被删除", username);
                throw new ServiceException("对不起，您的账号已被删除");
            }
            if (Objects.equals(UserStatusEnum.DISABLE, sysUserEntity.getStatus())) {
                log.warn("用户[{}]已被停用", username);
                throw new ServiceException("对不起，您的账号已停用");
            }

            log.info("用户[{}]登录信息组装成功，准备返回LoginUser对象", username);
            return LoginUserDto.builder()
                    .tenantId(sysUserEntity.getTenantId())
                    .userId(sysUserEntity.getId())
                    .userName(sysUserEntity.getUsername())
                    .password(sysUserEntity.getPassword())
                    .enterpriseId(sysUserEntity.getEnterpriseId())
                    .deptId(sysUserEntity.getDeptId())
                    .build();
        } catch (ServiceException e) {
            log.error("用户[{}]登录信息查询失败: {}", username, e.getErrorMessage());
            throw e;
        } catch (Exception e) {
            log.error("用户[{}]登录信息查询异常: {}", username, e.getMessage(), e);
            throw new ServiceException("查询用户信息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LoginUser getInfo() {
        // @formatter:off
        Long userId = SecurityUtils.getUserId();
        if (Objects.isNull(userId)) {
            log.warn("当前用户未登录");
            throw new ServiceException("用户未登录");
        }
        log.info("获取用户[{}]的完整信息（基础信息、角色、权限、菜单）", userId);

        SysUserEntity sysUserEntity = this.mapper.selectOneById(userId);
        log.info("校验数据库用户[{}]基本信息是否存在", userId);
        if (Objects.isNull(sysUserEntity)) {
            log.warn("校验数据库用户[{}]不存在", userId);
            throw new ServiceException("用户不存在");
        }
        List<SysRoleInfo> roles = sysRoleService.getSysRoleInfoList(userId);
        List<SysPermissionEntity> permissionEntities = sysPermissionService.getSysPermissionInfoList(userId);
        List<SysPermissionInfo> permissions = new ArrayList<>();
        List<SysMenuInfo> menus = new ArrayList<>();
        if (FunCollectUtils.isNotEmpty(permissionEntities)) {
            permissions = convertToPermissionInfoList(permissionEntities);
            menus = buildUserMenus(permissionEntities);
        }
        // 构建LoginUser对象
        LoginUser loginUser = LoginUser.builder()
                .userId(sysUserEntity.getId())
                .userName(sysUserEntity.getUsername())
                .nickName(sysUserEntity.getNickName())
                .realName(sysUserEntity.getRealName())
                .email(sysUserEntity.getEmail())
                .phone(sysUserEntity.getPhone())
                .sex(sysUserEntity.getSex())
                .avatar(sysUserEntity.getAvatar())
                .status(sysUserEntity.getStatus())
                .deleted(sysUserEntity.getDeleted())
                .enterpriseId(sysUserEntity.getEnterpriseId())
                .deptId(sysUserEntity.getDeptId())
                .roles(roles)
                .permissions(permissions)
                .menus(menus)
                .build();

        log.info("将当前用户信息设置到安全上下文，方便后续接口调用动态获取登录用户信息");
        redisService.setCacheObject(SecurityUtils.getUserKey(), loginUser, CacheConstants.LOGIN_USER_KEY_EXPIRE_TIME, TimeUnit.MINUTES);
        return loginUser;
        // @formatter:on
    }

    private List<SysMenuInfo> buildUserMenus(List<SysPermissionEntity> permissionEntities) {
        List<SysPermissionEntity> menuPermissions = permissionEntities.stream()
                .filter(p -> p.getType() != null && (p.getType() == PermimssionTypeEnum.MENU || p.getType() == PermimssionTypeEnum.DIRECTORY))
                .filter(p -> p.getStatus() != null && PermimssionStatusEnum.ACTIVATED == p.getStatus())
                .sorted(Comparator.comparing(p -> p.getSort() != null ? p.getSort() : 0))
                .collect(Collectors.toList());

        return buildMenuTree(menuPermissions, 0L);
    }

    private List<SysMenuInfo> buildMenuTree(List<SysPermissionEntity> permissions, Long pid) {
        return permissions.stream()
                .filter(p -> Objects.equals(pid, p.getPid()))
                .map(p -> {
                    SysMenuInfo menu = SysMenuInfo.builder()
                            .path(p.getPath())
                            .component(p.getComponent())
                            .redirect(p.getRedirect())
                            .name(p.getName())
                            .meta(MenuMate.builder()
                                    .title(p.getTitle())
                                    .type(p.getType())
                                    .icon(p.getIcon())
                                    .hidden(p.getHidden())
                                    .keepAlive(p.getKeepLive())
                                    .alwaysShow(p.getAlwaysShow())
                                    .build()
                            )
                            .sort(p.getSort())
                            .build();
                    menu.setChildren(buildMenuTree(permissions, p.getId()));
                    return menu;
                })
                .sorted(Comparator.comparing(SysMenuInfo::getSort, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
    }

    private List<SysPermissionInfo> convertToPermissionInfoList(List<SysPermissionEntity> entities) {
        return entities.stream()
                .map(p -> SysPermissionInfo.builder()
                        .permissionId(p.getId())
                        .permissionName(p.getName())
                        .permissionCode(p.getPermCode())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<SysMenuInfo> getSysMenuInfoList() {
        try {
            Long userId = SecurityUtils.getUserId();
            List<SysPermissionEntity> permissionEntities = sysPermissionService.getSysPermissionInfoList(userId);
            List<SysMenuInfo> menus = new ArrayList<>();
            if (FunCollectUtils.isNotEmpty(permissionEntities)) {
                menus = buildUserMenus(permissionEntities);
            }
            return menus;
        } catch (Exception e) {
            throw new ServiceException("获取路由菜单异常：", e.getMessage());
        }
    }
}
