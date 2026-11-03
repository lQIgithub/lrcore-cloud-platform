INSERT INTO `sys_permission`
    (`id`, `app_id`, `pid`, `type`, `name`, `title`, `perm_code`, `path`, `component`, `icon`, `sort`, `status`, `hidden`, `keep_live`, `always_show`, `build_in`, `version_num`, `has_column_permission`, `tenant_id`, `deleted`)
SELECT 7264590000000000185, 7264590000000000011, 7264590000000000141, 1, 'App', '应用管理', 'perm:system:app', 'app', 'system/app/index', 'el-icon-Platform', 10, 1, 0, 1, 0, 1, 1, 0, 7264590000000000001, 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_permission`
    WHERE `id` = 7264590000000000185 AND `deleted` = 0
);

INSERT INTO `sys_permission`
    (`id`, `app_id`, `pid`, `type`, `name`, `title`, `perm_code`, `path`, `component`, `icon`, `sort`, `status`, `hidden`, `keep_live`, `always_show`, `build_in`, `version_num`, `has_column_permission`, `tenant_id`, `deleted`)
SELECT 7264590000000000186, 7264590000000000011, 7264590000000000141, 1, 'Tenant', '租户管理', 'perm:system:tenant', 'tenant', 'system/tenant/index', 'el-icon-OfficeBuilding', 11, 1, 0, 1, 0, 1, 1, 0, 7264590000000000001, 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_permission`
    WHERE `id` = 7264590000000000186 AND `deleted` = 0
);

INSERT INTO `sys_role_permission`
    (`id`, `role_id`, `permission_id`, `tenant_id`, `build_in`, `deleted`, `version_num`, `grant_type`)
SELECT 7264590000000000185, 7264590000000000061, 7264590000000000185, 7264590000000000001, 1, 0, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission`
    WHERE `role_id` = 7264590000000000061
      AND `permission_id` = 7264590000000000185
      AND `deleted` = 0
);

INSERT INTO `sys_role_permission`
    (`id`, `role_id`, `permission_id`, `tenant_id`, `build_in`, `deleted`, `version_num`, `grant_type`)
SELECT 7264590000000000186, 7264590000000000061, 7264590000000000186, 7264590000000000001, 1, 0, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission`
    WHERE `role_id` = 7264590000000000061
      AND `permission_id` = 7264590000000000186
      AND `deleted` = 0
);
