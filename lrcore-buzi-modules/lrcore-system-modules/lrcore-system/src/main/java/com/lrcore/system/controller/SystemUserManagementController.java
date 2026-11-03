package com.lrcore.system.controller;

import com.lrcore.common.core.enums.SexEnum;
import com.lrcore.common.core.enums.UserStatusEnum;
import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.domain.SysDeptEntity;
import com.lrcore.system.domain.SysRoleEntity;
import com.lrcore.system.domain.SysUserEntity;
import com.lrcore.system.domain.SysUserRoleEntity;
import com.lrcore.system.domain.apt.SysUserAPT;
import com.lrcore.system.domain.apt.SysUserRoleAPT;
import com.lrcore.system.service.ISysDeptService;
import com.lrcore.system.service.ISysRoleService;
import com.lrcore.system.service.ISysUserRoleService;
import com.lrcore.system.service.ISysUserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Tag(name = "系统用户管理")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class SystemUserManagementController extends BaseController {

    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final ISysDeptService deptService;
    private final ISysUserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "用户分页列表")
    @GetMapping
    public ApiResult<PageResponse<UserItem>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long enterpriseId,
            @RequestParam(required = false) List<String> createTime
    ) {
        QueryWrapper query = QueryWrapper.create()
                .where(SysUserAPT.SYS_USER.DELETED.eq(0));

        if (Objects.nonNull(keywords) && !keywords.isBlank()) {
            String keyword = keywords.trim();
            query.and(SysUserAPT.SYS_USER.USERNAME.like(keyword)
                    .or(SysUserAPT.SYS_USER.NICK_NAME.like(keyword))
                    .or(SysUserAPT.SYS_USER.REAL_NAME.like(keyword))
                    .or(SysUserAPT.SYS_USER.PHONE.like(keyword))
                    .or(SysUserAPT.SYS_USER.EMAIL.like(keyword)));
        }
        if (Objects.nonNull(status)) {
            query.and(SysUserAPT.SYS_USER.STATUS.eq(toUserStatus(status)));
        }
        if (Objects.nonNull(deptId)) {
            query.and(SysUserAPT.SYS_USER.DEPT_ID.eq(deptId));
        }
        if (Objects.nonNull(enterpriseId)) {
            query.and(SysUserAPT.SYS_USER.ENTERPRISE_ID.eq(enterpriseId));
        }
        if (Objects.nonNull(createTime) && createTime.size() >= 2) {
            query.and(SysUserAPT.SYS_USER.CREATE_TIME.between(
                    LocalDate.parse(createTime.get(0)).atStartOfDay(),
                    LocalDate.parse(createTime.get(1)).atTime(23, 59, 59)
            ));
        }

        Page<SysUserEntity> result = userService.page(Page.of(pageNum, pageSize), query);
        return ApiResult.success(new PageResponse<>(
                result.getRecords().stream().map(this::toUserItem).toList(),
                result.getTotalRow()
        ));
    }

    @Operation(summary = "用户表单详情")
    @GetMapping("/{id}/form")
    public ApiResult<UserForm> form(@PathVariable Long id) {
        SysUserEntity user = userService.getById(id);
        if (Objects.isNull(user)) {
            return ApiResult.success(null);
        }

        List<Long> roleIds = userRoleService.list(QueryWrapper.create()
                        .where(SysUserRoleAPT.SYS_USER_ROLE.USER_ID.eq(id)))
                .stream()
                .map(SysUserRoleEntity::getRoleId)
                .filter(Objects::nonNull)
                .toList();

        return ApiResult.success(new UserForm(
                user.getId().toString(),
                user.getAvatar(),
                Objects.toString(user.getDeptId(), null),
                user.getEmail(),
                Optional.ofNullable(user.getSex()).map(SexEnum::getCode).orElse(null),
                user.getPhone(),
                user.getNickName(),
                roleIds,
                toCommonStatus(user.getStatus()),
                user.getUsername()
        ));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public ApiResult<Boolean> create(@RequestBody UserForm form) {
        SysUserEntity user = toUserEntity(form, null);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setDeleted(0);
        boolean saved = userService.save(user);
        if (saved) {
            assignRoles(user.getId(), form.roleIds());
        }
        return ApiResult.success(saved);
    }

    @Operation(summary = "修改用户")
    @PutMapping("/{id}")
    public ApiResult<Boolean> update(@PathVariable Long id, @RequestBody UserForm form) {
        SysUserEntity user = toUserEntity(form, id);
        boolean updated = userService.updateById(user);
        if (updated) {
            assignRoles(id, form.roleIds());
        }
        return ApiResult.success(updated);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{ids}")
    public ApiResult<Boolean> delete(@PathVariable String ids) {
        List<Long> idList = parseIds(ids);
        return ApiResult.success(userService.removeByIds(idList));
    }

    @Operation(summary = "重置用户密码")
    @PutMapping("/{id}/password/reset")
    public ApiResult<Boolean> resetPassword(@PathVariable Long id, @RequestParam String password) {
        SysUserEntity user = userService.getById(id);
        if (Objects.isNull(user)) {
            return ApiResult.success(false);
        }
        user.setPassword(passwordEncoder.encode(password));
        return ApiResult.success(userService.updateById(user));
    }

    @Operation(summary = "用户下拉选项")
    @GetMapping("/options")
    public ApiResult<List<Option>> options() {
        List<SysUserEntity> users = userService.list(QueryWrapper.create()
                .where(SysUserAPT.SYS_USER.DELETED.eq(0))
                .and(SysUserAPT.SYS_USER.STATUS.eq(UserStatusEnum.NORMAL)));
        return ApiResult.success(users.stream()
                .map(user -> new Option(
                        user.getId().toString(),
                        Optional.ofNullable(user.getNickName()).filter(name -> !name.isBlank())
                                .orElseGet(() -> Objects.toString(user.getUsername(), ""))
                ))
                .toList());
    }

    private SysUserEntity toUserEntity(UserForm form, Long id) {
        SysUserEntity user = new SysUserEntity();
        user.setId(id);
        user.setUsername(form.username());
        user.setNickName(form.nickname());
        user.setPhone(form.mobile());
        user.setEmail(form.email());
        user.setSex(SexEnum.getSexEnumByCode(form.gender()));
        user.setAvatar(form.avatar());
        user.setStatus(toUserStatus(form.status()));
        user.setDeptId(Objects.isNull(form.deptId()) ? null : Long.valueOf(form.deptId()));
        return user;
    }

    private void assignRoles(Long userId, List<Long> roleIds) {
        userRoleService.remove(QueryWrapper.create()
                .where(SysUserRoleAPT.SYS_USER_ROLE.USER_ID.eq(userId)));
        if (Objects.isNull(roleIds) || roleIds.isEmpty()) {
            return;
        }
        List<SysUserRoleEntity> relations = roleIds.stream()
                .distinct()
                .map(roleId -> new SysUserRoleEntity()
                        .setUserId(userId)
                        .setRoleId(roleId)
                        .setRelationStatus(0))
                .toList();
        userRoleService.saveBatch(relations);
    }

    private UserItem toUserItem(SysUserEntity user) {
        List<SysUserRoleEntity> relations = userRoleService.list(QueryWrapper.create()
                .where(SysUserRoleAPT.SYS_USER_ROLE.USER_ID.eq(user.getId())));
        List<Long> roleIds = relations.stream()
                .map(SysUserRoleEntity::getRoleId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, SysRoleEntity> roleMap = roleIds.isEmpty()
                ? Collections.emptyMap()
                : roleService.listByIds(roleIds).stream()
                        .collect(Collectors.toMap(SysRoleEntity::getId, Function.identity(), (a, b) -> a));
        String roleNames = roleIds.stream()
                .map(roleMap::get)
                .filter(Objects::nonNull)
                .map(SysRoleEntity::getRoleName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));

        String deptName = null;
        if (Objects.nonNull(user.getDeptId())) {
            deptName = Optional.ofNullable(deptService.getById(user.getDeptId()))
                    .map(SysDeptEntity::getDeptName)
                    .orElse(null);
        }

        return new UserItem(
                user.getId().toString(),
                user.getAvatar(),
                user.getCreateTime(),
                deptName,
                user.getEmail(),
                Optional.ofNullable(user.getSex()).map(SexEnum::getCode).orElse(null),
                user.getPhone(),
                user.getNickName(),
                roleNames,
                toCommonStatus(user.getStatus()),
                user.getUsername()
        );
    }

    private UserStatusEnum toUserStatus(Integer status) {
        return Objects.equals(status, 0) ? UserStatusEnum.DISABLE : UserStatusEnum.NORMAL;
    }

    private Integer toCommonStatus(UserStatusEnum status) {
        return Objects.equals(status, UserStatusEnum.NORMAL) ? 1 : 0;
    }

    private List<Long> parseIds(String ids) {
        return Arrays.stream(ids.split(","))
                .filter(id -> !id.isBlank())
                .map(Long::valueOf)
                .toList();
    }

    private record PageResponse<T>(List<T> list, long total) {
    }

    private record UserItem(
            String id,
            String avatar,
            LocalDateTime createTime,
            String deptName,
            String email,
            Integer gender,
            String mobile,
            String nickname,
            String roleNames,
            Integer status,
            String username
    ) {
    }

    private record UserForm(
            String id,
            String avatar,
            String deptId,
            String email,
            Integer gender,
            String mobile,
            String nickname,
            List<Long> roleIds,
            Integer status,
            String username
    ) {
    }

    private record Option(String value, String label) {
    }
}
