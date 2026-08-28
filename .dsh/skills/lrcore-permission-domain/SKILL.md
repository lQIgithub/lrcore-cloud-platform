---
name: lrcore-permission-domain
description: 在 lrcore-cloud-platform 修改权限相关逻辑前必读：28 张 sys_ 表的分层领域模型、角色/权限继承设计、数据权限(行级)与字段权限(列级)的真实运行时链路、缓存/审计/批量表的作用与"只有 CRUD 未接线"的现状。改 sys_role/sys_permission/sys_user_role/sys_role_permission/sys_data_permission_rule、SysUserServiceImpl 权限解析或 SysDataScopeRuleProvider 时使用。
whenToUse: 涉及租户/角色/权限/数据权限/字段权限的表结构或授权逻辑变更、权限校验链路排查、评估"角色继承/权限缓存/审计是否生效"、新增授权接口前。
---

# lrcore 权限领域模型

改任何权限逻辑前先读懂：领域设计（文档）+ 真实 DDL + 当前代码实际消费了哪些表。

## 0. 事实来源与优先级
1. 代码 + 根目录 `lrcore_cloudv1.1添加字段注释.sql`（真实 DDL：27/28 张 sys_ 表 + Flowable `ACT_*`/`FLW_*` 表；**缺 `sys_column_metadata`**；`sys_config`/`sys_dict_data`/`sys_dict_type` 有代码无 DDL）
2. `lrcore_doc.md`（28 表设计文档，中文）
⚠️ 文档是设计稿，与真实 DDL/代码不一致处（已验证）：文档 `sys_permission.permission_type 1=菜单 2=按钮 3=接口`，实际字段是 `type`，取值 **0=目录 1=菜单 2=按钮 3=接口**（`PermimssionTypeEnum` + DDL 注释）；文档 `permission_name/permission_code/ancestors`，实际是 `name`（路由唯一名）/`title`/`perm_code`，且**没有 ancestors 列**；文档引用的 `lrcore-buzi-modules/lrcore-system-modules/db/lrcore_permission_schema.sql` 在仓库中不存在。
**规则：字段名、取值一律以 DDL + 代码为准，禁止照抄文档字段名。**

## 1. 28 表分层结构（按 lrcore_doc.md，已对照 DDL）
| 层 | 表 | 要点 |
|---|---|---|
| 租户/应用 | sys_tenant, sys_app, sys_tenant_app | app 带 `permission_prefix`（权限编码前缀）、`app_encrypt`（前后端加解密密钥）；tenant_app 联合主键 (tenant_id, app_id) |
| 组织架构 | sys_enterprise, sys_dept, sys_user, sys_user_enterprise | enterprise/dept 均 `parent_id` 树；user 挂 ent_id/dept_id；status 0启用 1禁用 2锁定 |
| 权限核心 | sys_permission, sys_role, sys_user_role, sys_role_permission | 见 §3 核心链 |
| 权限扩展 | sys_role_group(+member), sys_permission_group(+member), sys_role_inheritance, sys_permission_inheritance | 分组带 priority；继承表带 inherit_type/inherit_ratio/override_enabled/priority/生效期 |
| 数据/字段权限 | sys_data_permission_rule, sys_column_metadata, sys_column_permission_rule, sys_role_column_permission, sys_user_column_permission | 见 §5/§6 |
| 缓存/审计 | sys_permission_cache_config, sys_user_permission_cache, sys_permission_audit_log, sys_permission_history | 见 §7 |
| 其他 | sys_role_permission_batch, sys_demo | 批量授权记录（batch_no、operation_type 1授予 2撤销 3替换、status 0待处理 1处理中 2成功 3失败） |

通用列（每张表都有，DDL 已验证）：`id BIGINT NOT NULL`（雪花，无自增）、`tenant_id BIGINT NULL`（**NULL=系统级数据**）、`create_user_id`、`update_user_id`、`create_time DEFAULT CURRENT_TIMESTAMP`、`update_time ON UPDATE CURRENT_TIMESTAMP`、`remark VARCHAR(500)`、`build_in TINYINT DEFAULT 0`、`deleted TINYINT DEFAULT 0`。

## 2. 核心关系链
`sys_user →(sys_user_role 联合主键 user_id+role_id)→ sys_role →(sys_role_permission)→ sys_permission`；`sys_app →(app_id)→ sys_permission`；`sys_tenant →(tenant_id 每表一列)→ 全部`。
- `sys_user_role` 额外带 per 用户角色的覆盖位：`relation_status`(0有效 1无效)、`data_scope`/`data_scope_type`/`custom_data_scope`、`field_permissions`/`row_permissions`/`column_permissions`（TEXT JSON）。
- `sys_role_permission` 带 `grant_type`（授予方式）、`effective_time`/`expire_time`（时效授权）、`version_num`。
- `sys_permission` 树用 `pid`（0=根）；唯一约束 `uk_tenant_app_code(tenant_id, app_id, perm_code, deleted)`——**同一 perm_code 在不同租户/应用下可重复**，写查询/去重必须带 tenant_id+app_id。

## 3. 当前运行时权限链路（代码实际行为，已验证）
1. 登录：lrcore-auth（SAS）签 RS256 JWT，claims 含 `user_id/username/tenant_id/ent_id/dept_id`（`LrcoreTokenCustomizer`）；网关 `AuthFilter` 映射为下游请求头。
2. 用户权限集：`SysUserServiceImpl.getInfo()` → `SysRoleServiceImpl.getSysRoleInfoList(userId)`（sys_user_role→sys_role，status=0 且 deleted=0）+ `SysPermissionServiceImpl.getSysPermissionInfoList(userId)`（**三表直连** sys_user_role→sys_role_permission→sys_permission，status=1 且 deleted=0，**不走继承**）→ 组装 `LoginUser`（roles/permissions/menus，菜单树只取 DIRECTORY+MENU 且 ACTIVATED）→ 写 Redis（`SecurityUtils.getUserKey()`，30 分钟，`CacheConstants.LOGIN_USER_KEY_EXPIRE_TIME`）。
3. 行/列数据权限：每请求由 common-datascope 拦截加载，见 §5。

## 4. 角色继承 / 权限继承（设计 vs 现状）
- 设计（文档 §5.4）：`sys_role.parent_id` + `inherit_enabled`/`inherit_type`；`sys_role_inheritance`（parent_role_id/child_role_id、inherit_type：1 完全继承 2 部分继承(inherit_ratio 比例) 3 覆盖继承 4 互斥、override_enabled、priority、start/end）；`sys_permission_inheritance` 同构。
- **现状（已验证）：这两张表只有生成的 CRUD（entity/mapper/service/controller），权限解析代码不读取它们，没有任何继承展开逻辑。** `getSysPermissionInfoList` 只算"直接授予"。
- 改授权逻辑时必须先向用户确认：是要维持"直接授予"现状，还是要实现继承展开（那属于新功能，需在权限解析处加递归/闭包，并同步 Redis 缓存失效）。

## 5. 数据权限（行级）——唯一真实生效的自动数据过滤
- 规则表 `sys_data_permission_rule`：`rule_type`（`DataRuleTypeEnum`：1 行级 2 字段级 3 数据范围 4 自定义SQL）、`target_table`/`target_field`、`permission_column` + `permission_operator`（`PermOperatorEnum`：1 = 2 != 3 IN 4 NOT IN 5 LIKE 6 BETWEEN）+ `permission_value`、`filter_condition`、`data_scope`（`DataScopeEnum`：1 全部 2 本企业 3 本部门 4 本部门及下级 5 本人 6 自定义）、`priority`、`is_enabled`、`app_id`（可空=全部应用）。规则是**租户级**（无 role_id 列；引擎侧另有 admin 跳过与按角色过滤逻辑）。
- 运行时链路（改数据权限只动这条链）：
  `DataScopeInterceptor`（common-datascope 的 HandlerInterceptor，path `/**`，排除 /login /logout /refresh /public/** /error /actuator/** /swagger-ui/** /v3/api-docs/**）
  → `DataScopeRuleService`/`DataScopeRuleEngine`（取规则、按 priority 排序、admin 可跳过）
  → **`SysDataScopeRuleProvider`**（`com.lrcore.system.provider`，实现 common-datascope 的 `DataScopeRuleProvider`）：按当前 `SecurityUtils.getTenantId()` 查 `sys_data_permission_rule`（is_enabled=1、deleted=0、priority asc，经 `SysDataPermissionRuleMapper.selectValidRules*`）与 `sys_column_permission_rule`，转成 common-core 的 `PermissionRule{dataPermissionRules, columnPermissionRules}`；**tenantId 为空直接返回空规则**
  → `DataScopeDialectImpl` 注册进 MyBatis-Flex `DialectFactory`（MySQL/MariaDB），在 SQL 执行前按规则改写。
- 配置前缀 `lrcore.datascope`（Nacos）：`enabled`、`defaultScope`、`cacheEnabled`、`cacheSize`、`debugEnabled`、`adminRoleCode`（默认 "admin"）、`skipAdminCheck`。
- ⚠️ `sys_role.data_scope` 与 `sys_user_role.data_scope/data_scope_type/custom_data_scope` 是模型级字段，**当前运行时规则加载不读它们**（只读规则表）；不要假设它们生效，也不要悄悄改它们的含义。

## 6. 字段权限（列级）
- 运行时生效的只有 `sys_column_permission_rule`（`table_name`、`column_name` 可空=整表、`column_names` JSON 数组、`permission_type`：1 可见 2 可编辑 3 只读 4 隐藏 5 加密显示、`filter_condition`、`priority`/`is_default`/`is_enabled`），经 §5 同一条 provider→dialect 链路生效。
- 仅设计/CRUD 未接线：`sys_column_metadata`（字段元数据，`is_sensitive`、`sensitive_level` 1-4、`is_visible`）、`sys_role_column_permission`（角色级字段授权，带生效期）、`sys_user_column_permission`（用户级覆盖，`is_override` 覆盖角色配置）——三张表只有生成的 CRUD，无运行时消费。
- 响应级脱敏另有 common-sensitive（`@Sensitive` 注解 + `DesensitizedUtil` + JSON 序列化器），与列权限规则表是两套东西，别混用。

## 7. 缓存与审计表（设计目的 + 现状）
| 表 | 设计用途 | 现状（已验证） |
|---|---|---|
| sys_permission_cache_config | 缓存策略配置：cache_type 1本地 2分布式 3混合、cache_level 1-3、key 前缀/模式、ttl、preload_cron、refresh/压缩/加密/序列化开关 | 仅 CRUD |
| sys_user_permission_cache | 用户→权限物化缓存（免每次 join 三表）：grant_type 1直接 2角色 3部门、cache_version、is_valid、expire_time | 仅 CRUD，无人写入/读取 |
| sys_permission_audit_log | 权限操作审计：audit_type 1授予 2撤销 3变更 4查询 5删除 6角色权限变更；target_type 1用户 2角色 3部门 4企业；operator/request/response 快照 | 仅 CRUD，无人写入 |
| sys_permission_history | 权限版本历史：change_type 1新增 2修改 3删除 4恢复、old/new_value、change_reason、rollback_id、is_rollback_point | 仅 CRUD |
- 结论：这 4 张表 + §4 继承表 + 分组表 + batch 表目前是"模型与 CRUD 就绪、引擎未接"。做需求时先问：是补运行时接线（新功能），还是只维护 CRUD。

## 8. 改动规则（硬性）
- 查任何 sys_ 表必须带 `tenant_id` + `deleted=0`（本仓库未开 MyBatis-Flex 全局逻辑删除，靠查询条件，见 `lrcore-backend-conventions` §6）。
- 保持 `uk_tenant_app_code` 唯一性契约；perm_code 判重必须 (tenant_id, app_id, perm_code) 维度。
- 不要改 JWT claims 契约（user_id/username/tenant_id/ent_id/dept_id）与下游请求头名——网关 `AuthFilter` 与 `HeaderInterceptor` 硬依赖。
- 不要给 sys_permission 加 `ancestors`、不要引用文档里不存在的字段名。
- 权限解析结果有 Redis 缓存（30 分钟）：改授权数据后若不设计缓存失效，前端仍会看到旧权限——明确告知用户。
- 改 `SysDataScopeRuleProvider` / 规则表结构后，用 `lrcore-build-test` skill 的命令验证编译（`mvn -pl lrcore-buzi-modules/lrcore-system-modules/lrcore-system -am compile`）。
