package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.domain.SysRoleEntity;
import com.lrcore.system.domain.SysRolePermissionEntity;
import com.lrcore.system.domain.apt.SysRoleAPT;
import com.lrcore.system.domain.apt.SysRolePermissionAPT;
import com.lrcore.system.service.ISysRolePermissionService;
import com.lrcore.system.service.ISysRoleService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Tag(name = "系统角色管理")
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class SystemRoleManagementController extends BaseController {

    private final ISysRoleService roleService;
    private final ISysRolePermissionService rolePermissionService;

    @Operation(summary = "角色分页列表")
    @GetMapping
    public ApiResult<PageResponse<RoleItem>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) Long appId,
            @RequestParam(required = false) String roleScope
    ) {
        QueryWrapper query = QueryWrapper.create()
                .where(SysRoleAPT.SYS_ROLE.DELETED.eq(0));
        if (Objects.nonNull(keywords) && !keywords.isBlank()) {
            String keyword = keywords.trim();
            query.and(SysRoleAPT.SYS_ROLE.ROLE_NAME.like(keyword)
                    .or(SysRoleAPT.SYS_ROLE.ROLE_CODE.like(keyword)));
        }
        if (Objects.nonNull(appId)) {
            query.and(SysRoleAPT.SYS_ROLE.APP_ID.eq(appId));
        }
        if (Objects.nonNull(roleScope) && !roleScope.isBlank()) {
            query.and(SysRoleAPT.SYS_ROLE.ROLE_SCOPE.eq(roleScope.trim()));
        }
        query.orderBy(SysRoleAPT.SYS_ROLE.SORT_ORDER.asc())
                .orderBy(SysRoleAPT.SYS_ROLE.CREATE_TIME.desc());

        Page<SysRoleEntity> result = roleService.page(Page.of(pageNum, pageSize), query);
        return ApiResult.success(new PageResponse<>(
                result.getRecords().stream().map(this::toRoleItem).toList(),
                result.getTotalRow()
        ));
    }

    @Operation(summary = "角色下拉选项")
    @GetMapping("/options")
    public ApiResult<List<Option>> options() {
        List<SysRoleEntity> roles = roleService.list(QueryWrapper.create()
                .where(SysRoleAPT.SYS_ROLE.DELETED.eq(0))
                .and(SysRoleAPT.SYS_ROLE.STATUS.eq(0)));
        return ApiResult.success(roles.stream()
                .map(role -> new Option(role.getId().toString(), role.getRoleName()))
                .toList());
    }

    @Operation(summary = "角色表单详情")
    @GetMapping("/{id}/form")
    public ApiResult<RoleForm> form(@PathVariable Long id) {
        SysRoleEntity role = roleService.getById(id);
        if (Objects.isNull(role)) {
            return ApiResult.success(null);
        }
        return ApiResult.success(toRoleForm(role));
    }

    @Operation(summary = "新增角色")
    @PostMapping
    public ApiResult<Boolean> create(@RequestBody RoleForm form) {
        SysRoleEntity role = toRoleEntity(form, null);
        return ApiResult.success(roleService.save(role));
    }

    @Operation(summary = "修改角色")
    @PutMapping("/{id}")
    public ApiResult<Boolean> update(@PathVariable Long id, @RequestBody RoleForm form) {
        SysRoleEntity role = toRoleEntity(form, id);
        return ApiResult.success(roleService.updateById(role));
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{ids}")
    public ApiResult<Boolean> delete(@PathVariable String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .filter(id -> !id.isBlank())
                .map(Long::valueOf)
                .toList();
        return ApiResult.success(roleService.removeByIds(idList));
    }

    @Operation(summary = "角色菜单ID集合")
    @GetMapping("/{id}/menu-ids")
    public ApiResult<List<String>> menuIds(@PathVariable Long id) {
        return ApiResult.success(rolePermissionService.list(QueryWrapper.create()
                        .where(SysRolePermissionAPT.SYS_ROLE_PERMISSION.ROLE_ID.eq(id)))
                .stream()
                .map(SysRolePermissionEntity::getPermissionId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList());
    }

    @Operation(summary = "分配角色菜单权限")
    @PutMapping("/{id}/menus")
    public ApiResult<Boolean> updateMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        Long tenantId = Optional.ofNullable(roleService.getById(id))
                .map(SysRoleEntity::getTenantId)
                .orElse(null);
        rolePermissionService.remove(QueryWrapper.create()
                .where(SysRolePermissionAPT.SYS_ROLE_PERMISSION.ROLE_ID.eq(id)));
        if (Objects.isNull(menuIds) || menuIds.isEmpty()) {
            return ApiResult.success(true);
        }

        List<SysRolePermissionEntity> relations = menuIds.stream()
                .distinct()
                .map(menuId -> {
                    SysRolePermissionEntity relation = new SysRolePermissionEntity();
                    relation.setRoleId(id);
                    relation.setTenantId(tenantId);
                    relation.setPermissionId(menuId);
                    return relation;
                })
                .toList();
        return ApiResult.success(rolePermissionService.saveBatch(relations));
    }

    private RoleItem toRoleItem(SysRoleEntity role) {
        return new RoleItem(
                role.getId().toString(),
                role.getRoleCode(),
                role.getRoleName(),
                "全部数据",
                Objects.toString(role.getAppId(), "0"),
                Objects.requireNonNullElse(role.getRoleScope(), "APP"),
                Objects.equals(role.getStatus(), 0) ? 1 : 0,
                Optional.ofNullable(role.getSortOrder()).orElse(0),
                role.getBuildIn(),
                role.getUpdateTime()
        );
    }

    private RoleForm toRoleForm(SysRoleEntity role) {
        return new RoleForm(
                role.getId().toString(),
                role.getRoleCode(),
                role.getRoleName(),
                Objects.toString(role.getAppId(), "0"),
                Objects.requireNonNullElse(role.getRoleScope(), "APP"),
                1,
                null,
                null,
                Objects.equals(role.getStatus(), 0) ? 1 : 0,
                role.getRemark()
        );
    }

    private SysRoleEntity toRoleEntity(RoleForm form, Long id) {
        SysRoleEntity role = new SysRoleEntity();
        role.setId(id);
        role.setRoleName(form.name());
        role.setRoleCode(form.code());
        if (Objects.equals(form.roleScope(), "PLATFORM")) {
            role.setAppId(0L);
        } else if (Objects.nonNull(form.appId())) {
            role.setAppId(Long.valueOf(form.appId()));
        } else if (Objects.isNull(id)) {
            role.setAppId(0L);
        }
        role.setRoleScope(Objects.equals(form.roleScope(), "PLATFORM") ? "PLATFORM" : "APP");
        role.setStatus(Objects.equals(form.status(), 0) ? 1 : 0);
        role.setSortOrder(form.sort());
        role.setRemark(form.remark());
        return role;
    }

    private record PageResponse<T>(List<T> list, long total) {
    }

    private record RoleItem(
            String id,
            String code,
            String name,
            String dataScopeLabel,
            String appId,
            String roleScope,
            Integer status,
            Integer sort,
            Integer buildIn,
            java.time.LocalDateTime updateTime
    ) {
    }

    private record RoleForm(
            String id,
            String code,
            String name,
            String appId,
            String roleScope,
            Integer dataScope,
            List<String> deptIds,
            Integer sort,
            Integer status,
            String remark
    ) {
    }

    private record Option(String value, String label) {
    }
}
