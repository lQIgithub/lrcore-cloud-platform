-- ============================================================
-- 数据权限规则管理菜单
-- 日期: 2026-11-05
-- 说明:
--   1. 在「系统管理」目录下新增「数据权限规则」菜单（type=1 菜单），
--      与「角色管理」同级；前端组件:
--      lrcore-cloud-admin-ui/src/views/system/dataPermissionRule/index.vue
--   2. 新增 查询/新增/修改/删除 四个按钮权限（type=2）
--   3. 为 admin 角色（role_code='admin'）授权菜单 + 按钮
--   4. 脚本幂等，可重复执行
-- 执行: mysql -uroot -p lrcore_cloud < 20261105_data_permission_rule_menu.sql
-- ============================================================

-- ---------- 1. 菜单：数据权限规则 ----------
INSERT INTO sys_permission
  (id, app_id, pid, type, name, title, perm_code, path, component, redirect,
   req_method, req_url, icon, sort, status, hidden, keep_live, always_show,
   build_in, version_num, has_column_permission, tenant_id,
   create_user_id, update_user_id, create_time, update_time, remark, deleted)
SELECT 7264590000000000901, r.app_id, r.pid, 1,
       'DataPermissionRule', '数据权限规则', 'perm:system:data-permission-rule',
       'data-permission-rule', 'system/dataPermissionRule/index', NULL,
       '', '', 'form', 10, 1, 0, 1, 0,
       1, 1, 0,
       r.tenant_id,
       NULL, NULL, NOW(), NOW(), '数据权限规则管理菜单', 0
FROM sys_permission r
WHERE r.perm_code = 'perm:system:role' AND r.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM sys_permission x WHERE x.id = 7264590000000000901);

-- ---------- 2. 按钮权限（挂在数据权限规则菜单下） ----------
INSERT INTO sys_permission
  (id, app_id, pid, type, name, title, perm_code, path, component,
   req_method, req_url, icon, sort, status, hidden, keep_live, always_show,
   build_in, version_num, has_column_permission, tenant_id,
   create_user_id, update_user_id, create_time, update_time, remark, deleted)
SELECT 7264590000000000902, m.app_id, m.id, 2,
       'DataPermissionRuleQuery', '数据权限规则查询', 'perm:system:data-permission-rule:query',
       '', NULL, '', '', '', 1, 1, 0, 0, 0,
       1, 1, 0, m.tenant_id, NULL, NULL, NOW(), NOW(), '', 0
FROM sys_permission m
WHERE m.id = 7264590000000000901
  AND NOT EXISTS (SELECT 1 FROM sys_permission x WHERE x.id = 7264590000000000902);

INSERT INTO sys_permission
  (id, app_id, pid, type, name, title, perm_code, path, component,
   req_method, req_url, icon, sort, status, hidden, keep_live, always_show,
   build_in, version_num, has_column_permission, tenant_id,
   create_user_id, update_user_id, create_time, update_time, remark, deleted)
SELECT 7264590000000000903, m.app_id, m.id, 2,
       'DataPermissionRuleAdd', '数据权限规则新增', 'perm:system:data-permission-rule:add',
       '', NULL, '', '', '', 2, 1, 0, 0, 0,
       1, 1, 0, m.tenant_id, NULL, NULL, NOW(), NOW(), '', 0
FROM sys_permission m
WHERE m.id = 7264590000000000901
  AND NOT EXISTS (SELECT 1 FROM sys_permission x WHERE x.id = 7264590000000000903);

INSERT INTO sys_permission
  (id, app_id, pid, type, name, title, perm_code, path, component,
   req_method, req_url, icon, sort, status, hidden, keep_live, always_show,
   build_in, version_num, has_column_permission, tenant_id,
   create_user_id, update_user_id, create_time, update_time, remark, deleted)
SELECT 7264590000000000904, m.app_id, m.id, 2,
       'DataPermissionRuleEdit', '数据权限规则修改', 'perm:system:data-permission-rule:edit',
       '', NULL, '', '', '', 3, 1, 0, 0, 0,
       1, 1, 0, m.tenant_id, NULL, NULL, NOW(), NOW(), '', 0
FROM sys_permission m
WHERE m.id = 7264590000000000901
  AND NOT EXISTS (SELECT 1 FROM sys_permission x WHERE x.id = 7264590000000000904);

INSERT INTO sys_permission
  (id, app_id, pid, type, name, title, perm_code, path, component,
   req_method, req_url, icon, sort, status, hidden, keep_live, always_show,
   build_in, version_num, has_column_permission, tenant_id,
   create_user_id, update_user_id, create_time, update_time, remark, deleted)
SELECT 7264590000000000905, m.app_id, m.id, 2,
       'DataPermissionRuleRemove', '数据权限规则删除', 'perm:system:data-permission-rule:remove',
       '', NULL, '', '', '', 4, 1, 0, 0, 0,
       1, 1, 0, m.tenant_id, NULL, NULL, NOW(), NOW(), '', 0
FROM sys_permission m
WHERE m.id = 7264590000000000901
  AND NOT EXISTS (SELECT 1 FROM sys_permission x WHERE x.id = 7264590000000000905);

-- ---------- 3. 为 admin 角色授权（菜单 + 4 个按钮） ----------
INSERT INTO sys_role_permission
  (id, role_id, permission_id, tenant_id, create_user_id, update_user_id,
   create_time, update_time, remark, build_in, deleted, version_num, grant_type)
SELECT 7264590000000000911, ro.id, 7264590000000000901,
       COALESCE(ro.tenant_id, p.tenant_id, 0), NULL, NULL, NOW(), NOW(), '', 0, 0, 1, 1
FROM sys_role ro
JOIN sys_permission p ON p.id = 7264590000000000901
WHERE ro.role_code = 'admin' AND ro.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission x
    WHERE x.role_id = ro.id AND x.permission_id = 7264590000000000901
      AND x.tenant_id = COALESCE(ro.tenant_id, p.tenant_id, 0)
  );

INSERT INTO sys_role_permission
  (id, role_id, permission_id, tenant_id, create_user_id, update_user_id,
   create_time, update_time, remark, build_in, deleted, version_num, grant_type)
SELECT 7264590000000000912, ro.id, 7264590000000000902,
       COALESCE(ro.tenant_id, p.tenant_id, 0), NULL, NULL, NOW(), NOW(), '', 0, 0, 1, 1
FROM sys_role ro
JOIN sys_permission p ON p.id = 7264590000000000902
WHERE ro.role_code = 'admin' AND ro.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission x
    WHERE x.role_id = ro.id AND x.permission_id = 7264590000000000902
      AND x.tenant_id = COALESCE(ro.tenant_id, p.tenant_id, 0)
  );

INSERT INTO sys_role_permission
  (id, role_id, permission_id, tenant_id, create_user_id, update_user_id,
   create_time, update_time, remark, build_in, deleted, version_num, grant_type)
SELECT 7264590000000000913, ro.id, 7264590000000000903,
       COALESCE(ro.tenant_id, p.tenant_id, 0), NULL, NULL, NOW(), NOW(), '', 0, 0, 1, 1
FROM sys_role ro
JOIN sys_permission p ON p.id = 7264590000000000903
WHERE ro.role_code = 'admin' AND ro.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission x
    WHERE x.role_id = ro.id AND x.permission_id = 7264590000000000903
      AND x.tenant_id = COALESCE(ro.tenant_id, p.tenant_id, 0)
  );

INSERT INTO sys_role_permission
  (id, role_id, permission_id, tenant_id, create_user_id, update_user_id,
   create_time, update_time, remark, build_in, deleted, version_num, grant_type)
SELECT 7264590000000000914, ro.id, 7264590000000000904,
       COALESCE(ro.tenant_id, p.tenant_id, 0), NULL, NULL, NOW(), NOW(), '', 0, 0, 1, 1
FROM sys_role ro
JOIN sys_permission p ON p.id = 7264590000000000904
WHERE ro.role_code = 'admin' AND ro.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission x
    WHERE x.role_id = ro.id AND x.permission_id = 7264590000000000904
      AND x.tenant_id = COALESCE(ro.tenant_id, p.tenant_id, 0)
  );

INSERT INTO sys_role_permission
  (id, role_id, permission_id, tenant_id, create_user_id, update_user_id,
   create_time, update_time, remark, build_in, deleted, version_num, grant_type)
SELECT 7264590000000000915, ro.id, 7264590000000000905,
       COALESCE(ro.tenant_id, p.tenant_id, 0), NULL, NULL, NOW(), NOW(), '', 0, 0, 1, 1
FROM sys_role ro
JOIN sys_permission p ON p.id = 7264590000000000905
WHERE ro.role_code = 'admin' AND ro.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission x
    WHERE x.role_id = ro.id AND x.permission_id = 7264590000000000905
      AND x.tenant_id = COALESCE(ro.tenant_id, p.tenant_id, 0)
  );


-- ---------- 3b. 为 super_admin 角色授权（本平台菜单实际挂在 super_admin 角色下） ----------
INSERT INTO sys_role_permission
  (id, role_id, permission_id, tenant_id, create_user_id, update_user_id,
   create_time, update_time, remark, build_in, deleted, version_num, grant_type)
SELECT 7264590000000000921, ro.id, 7264590000000000901,
       COALESCE(ro.tenant_id, p.tenant_id, 0), NULL, NULL, NOW(), NOW(), '', 0, 0, 1, 1
FROM sys_role ro
JOIN sys_permission p ON p.id = 7264590000000000901
WHERE ro.role_code = 'super_admin' AND ro.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission x
    WHERE x.role_id = ro.id AND x.permission_id = 7264590000000000901
      AND x.tenant_id = COALESCE(ro.tenant_id, p.tenant_id, 0)
  );

INSERT INTO sys_role_permission
  (id, role_id, permission_id, tenant_id, create_user_id, update_user_id,
   create_time, update_time, remark, build_in, deleted, version_num, grant_type)
SELECT 7264590000000000922, ro.id, 7264590000000000902,
       COALESCE(ro.tenant_id, p.tenant_id, 0), NULL, NULL, NOW(), NOW(), '', 0, 0, 1, 1
FROM sys_role ro
JOIN sys_permission p ON p.id = 7264590000000000902
WHERE ro.role_code = 'super_admin' AND ro.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission x
    WHERE x.role_id = ro.id AND x.permission_id = 7264590000000000902
      AND x.tenant_id = COALESCE(ro.tenant_id, p.tenant_id, 0)
  );

INSERT INTO sys_role_permission
  (id, role_id, permission_id, tenant_id, create_user_id, update_user_id,
   create_time, update_time, remark, build_in, deleted, version_num, grant_type)
SELECT 7264590000000000923, ro.id, 7264590000000000903,
       COALESCE(ro.tenant_id, p.tenant_id, 0), NULL, NULL, NOW(), NOW(), '', 0, 0, 1, 1
FROM sys_role ro
JOIN sys_permission p ON p.id = 7264590000000000903
WHERE ro.role_code = 'super_admin' AND ro.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission x
    WHERE x.role_id = ro.id AND x.permission_id = 7264590000000000903
      AND x.tenant_id = COALESCE(ro.tenant_id, p.tenant_id, 0)
  );

INSERT INTO sys_role_permission
  (id, role_id, permission_id, tenant_id, create_user_id, update_user_id,
   create_time, update_time, remark, build_in, deleted, version_num, grant_type)
SELECT 7264590000000000924, ro.id, 7264590000000000904,
       COALESCE(ro.tenant_id, p.tenant_id, 0), NULL, NULL, NOW(), NOW(), '', 0, 0, 1, 1
FROM sys_role ro
JOIN sys_permission p ON p.id = 7264590000000000904
WHERE ro.role_code = 'super_admin' AND ro.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission x
    WHERE x.role_id = ro.id AND x.permission_id = 7264590000000000904
      AND x.tenant_id = COALESCE(ro.tenant_id, p.tenant_id, 0)
  );

INSERT INTO sys_role_permission
  (id, role_id, permission_id, tenant_id, create_user_id, update_user_id,
   create_time, update_time, remark, build_in, deleted, version_num, grant_type)
SELECT 7264590000000000925, ro.id, 7264590000000000905,
       COALESCE(ro.tenant_id, p.tenant_id, 0), NULL, NULL, NOW(), NOW(), '', 0, 0, 1, 1
FROM sys_role ro
JOIN sys_permission p ON p.id = 7264590000000000905
WHERE ro.role_code = 'super_admin' AND ro.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission x
    WHERE x.role_id = ro.id AND x.permission_id = 7264590000000000905
      AND x.tenant_id = COALESCE(ro.tenant_id, p.tenant_id, 0)
  );

-- ---------- 4. 校验 ----------
SELECT id, type, name, title, perm_code, path, component
FROM sys_permission
WHERE id BETWEEN 7264590000000000901 AND 7264590000000000905;

SELECT rp.id, ro.role_code, p.perm_code
FROM sys_role_permission rp
JOIN sys_role ro ON ro.id = rp.role_id
JOIN sys_permission p ON p.id = rp.permission_id
WHERE rp.id BETWEEN 7264590000000000911 AND 7264590000000000925;
