# LRCore Cloud Platform 权限管理系统数据库表结构分析文档

## 一、概述

本文档对 [lrcore_permission_schema.sql](file:///Users/lrcore/IdeaProjects/lrcore-cloud-platform/lrcore-buzi-modules/lrcore-system-modules/db/lrcore_permission_schema.sql) 进行全面的数据库表结构分析。该系统是一个完整的**多租户权限管理平台**，包含28个数据表，覆盖租户管理、应用管理、权限控制、角色管理、组织架构、数据权限、字段权限、缓存与审计等功能模块。

---

## 二、表结构总览

### 2.1 表分类统计

| 分类 | 数量 | 表名 |
|-----|------|------|
| 租户与应用层 | 3 | sys_tenant, sys_app, sys_tenant_app |
| 组织架构层 | 3 | sys_enterprise, sys_dept, sys_user |
| 权限核心层 | 4 | sys_permission, sys_role, sys_user_role, sys_role_permission |
| 权限扩展层 | 6 | sys_role_group, sys_role_group_member, sys_role_inheritance, sys_permission_group, sys_permission_group_member, sys_permission_inheritance |
| 数据权限与字段权限层 | 5 | sys_data_permission_rule, sys_column_metadata, sys_column_permission_rule, sys_role_column_permission, sys_user_column_permission |
| 缓存与审计层 | 4 | sys_permission_cache_config, sys_user_permission_cache, sys_permission_audit_log, sys_permission_history |
| 批量操作 | 1 | sys_role_permission_batch |
| 用户-企业关联 | 1 | sys_user_enterprise |
| 演示表 | 1 | sys_demo |
| **合计** | **28** | |

### 2.2 通用字段设计模式

所有表均遵循统一的字段设计规范：

| 字段名 | 类型 | 说明 |
|-------|------|------|
| `id` | BIGINT | 主键ID |
| `tenant_id` | BIGINT | 租户ID（支持多租户隔离） |
| `create_user_id` | BIGINT | 创建人ID |
| `update_user_id` | BIGINT | 更新人ID |
| `create_time` | DATETIME | 创建时间（默认CURRENT_TIMESTAMP） |
| `update_time` | DATETIME | 更新时间（自动更新） |
| `remark` | VARCHAR(500) | 备注 |
| `build_in` | TINYINT | 是否内置（0=否 1=是） |
| `deleted` | TINYINT | 逻辑删除标识（0=未删除 1=已删除） |

---

## 三、各表详细说明

### 3.1 租户与应用层

#### 3.1.1 sys_tenant - 系统租户表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| tenant_name | VARCHAR(100) | NOT NULL, UNIQUE | 租户名称 |
| tenant_code | VARCHAR(64) | NOT NULL, UNIQUE | 租户编码 |
| tenant_desc | VARCHAR(500) | NULL | 租户描述 |
| status | TINYINT | NOT NULL, DEFAULT 0 | 租户状态（0启用 1禁用） |

**用途**：管理多租户环境中的租户信息，实现数据隔离。

#### 3.1.2 sys_app - 应用系统表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| app_name | VARCHAR(100) | NOT NULL, UNIQUE | 应用名称 |
| app_code | VARCHAR(64) | NOT NULL, UNIQUE | 应用编码 |
| app_desc | VARCHAR(500) | NULL | 应用描述 |
| app_encrypt | VARCHAR(50) | NULL | 用于前后端访问的加解密秘钥 |
| status | TINYINT | NOT NULL, DEFAULT 0 | 应用状态（0启用 1禁用） |
| app_url | VARCHAR(500) | NULL | 访问地址（URL） |
| app_icon | VARCHAR(200) | NULL | 应用图标 |
| permission_prefix | VARCHAR(10) | NULL | 权限标识符前缀 |

**用途**：管理系统中的各个应用模块，每个应用拥有独立的权限体系。

#### 3.1.3 sys_tenant_app - 租户-应用关联表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| tenant_id | BIGINT | NOT NULL, PRIMARY KEY | 租户ID |
| app_id | BIGINT | NOT NULL, PRIMARY KEY | 应用ID |

**用途**：实现租户与应用的多对多关联，控制租户可访问的应用范围。

---

### 3.2 组织架构层

#### 3.2.1 sys_enterprise - 企业信息表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| ent_name | VARCHAR(100) | NOT NULL | 企业名称 |
| ent_abbreviation | VARCHAR(50) | NULL | 企业简称 |
| ent_code | VARCHAR(50) | NOT NULL, UNIQUE | 企业编码 |
| parent_id | BIGINT | NULL | 上级企业ID（自关联） |
| ancestors | VARCHAR(500) | NULL | 祖级列表 |

**用途**：支持树形企业组织架构，parent_id实现自关联。

#### 3.2.2 sys_dept - 部门表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| dept_name | VARCHAR(100) | NOT NULL | 部门名称 |
| dept_code | VARCHAR(64) | NOT NULL | 组织编码 |
| parent_id | BIGINT | NULL | 上级部门ID |
| ent_id | BIGINT | NULL | 所属企业ID |
| ancestors | VARCHAR(500) | NULL | 祖级列表 |

**用途**：管理企业下的部门层级结构。

#### 3.2.3 sys_user - 用户基础信息表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| username | VARCHAR(64) | NOT NULL, UNIQUE | 账号 |
| password | VARCHAR(256) | NOT NULL | 密码（SHA-256加密） |
| real_name | VARCHAR(50) | NULL | 姓名 |
| nick_name | VARCHAR(50) | NULL | 用户昵称 |
| phone | VARCHAR(20) | NULL | 手机号码 |
| email | VARCHAR(100) | NULL | 电子邮箱 |
| ent_id | BIGINT | NULL | 所属企业ID |
| dept_id | BIGINT | NULL | 所属部门ID |
| status | TINYINT | NOT NULL, DEFAULT 0 | 用户状态（0启用 1禁用 2锁定） |

**用途**：存储系统用户的基础信息。

---

### 3.3 权限核心层

#### 3.3.1 sys_permission - 权限表（核心）

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| app_id | BIGINT | NOT NULL | 所属应用ID |
| parent_id | BIGINT | NOT NULL, DEFAULT 0 | 父权限ID（0为根节点） |
| ancestors | VARCHAR(500) | NOT NULL | 权限祖级列表 |
| permission_name | VARCHAR(100) | NOT NULL | 权限名称 |
| permission_code | VARCHAR(64) | NOT NULL | 权限唯一编码 |
| permission_type | TINYINT | NOT NULL | 权限类型：1=菜单 2=按钮 3=接口 |
| permission_path | VARCHAR(200) | NULL | 路由地址 |
| request_method | VARCHAR(20) | NULL | 请求方法 |
| request_url | VARCHAR(200) | NULL | 接口请求URL |
| menu_icon | VARCHAR(100) | NULL | 菜单图标 |
| menu_sort | INT | NULL | 菜单排序 |
| menu_type | TINYINT | NULL | 菜单打开方式：1=目录 2=菜单 3=内嵌 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 权限状态：0=禁用 1=启用 |
| access_count | INT | NOT NULL, DEFAULT 0 | 访问次数 |
| last_access_time | DATETIME | NULL | 最后访问时间 |
| avg_response_time | INT | NULL | 平均响应时间（毫秒） |
| cache_hit_rate | DECIMAL(5,2) | NULL | 缓存命中率 |
| version_num | INT | NOT NULL, DEFAULT 1 | 版本号 |
| has_column_permission | TINYINT | NOT NULL, DEFAULT 0 | 是否包含字段级权限 |

**用途**：整合菜单、按钮、接口权限的核心表，支持树形结构。

#### 3.3.2 sys_role - 角色信息表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| role_name | VARCHAR(100) | NOT NULL, UNIQUE(tenant_id) | 角色名称 |
| role_code | VARCHAR(64) | NOT NULL, UNIQUE(tenant_id) | 角色编码 |
| app_id | BIGINT | NULL | 所属应用ID |
| role_type | TINYINT | NOT NULL, DEFAULT 0 | 角色类型：1=系统内置 2=自定义 |
| data_scope | TINYINT | NULL | 数据权限范围 |
| parent_id | BIGINT | NULL | 父角色ID |
| inherit_enabled | TINYINT | NULL, DEFAULT 1 | 是否继承父角色权限 |
| inherit_type | TINYINT | NULL, DEFAULT 1 | 继承类型 |
| effective_time | DATETIME | NULL | 生效时间 |
| expire_time | DATETIME | NULL | 失效时间 |
| version_num | INT | NULL, DEFAULT 1 | 版本号 |

**用途**：管理角色信息，支持角色继承和时间范围生效。

#### 3.3.3 sys_user_role - 用户-角色关联表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| user_id | BIGINT | NOT NULL, PRIMARY KEY | 用户ID |
| role_id | BIGINT | NOT NULL, PRIMARY KEY | 角色ID |
| relation_status | TINYINT | NOT NULL, DEFAULT 0 | 关联状态（0有效 1无效） |
| data_scope | TINYINT | NOT NULL, DEFAULT 0 | 数据权限范围 |
| data_scope_type | TINYINT | NOT NULL, DEFAULT 0 | 数据权限类型 |
| custom_data_scope | VARCHAR(500) | NULL | 自定义数据范围 |
| field_permissions | TEXT | NULL | 字段级权限（JSON） |
| row_permissions | TEXT | NULL | 行级权限（JSON） |

**用途**：实现用户与角色的多对多关联，支持自定义权限配置。

#### 3.3.4 sys_role_permission - 角色-权限关联表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| role_id | BIGINT | NOT NULL | 角色ID |
| permission_id | BIGINT | NOT NULL | 权限ID |
| tenant_id | BIGINT | NOT NULL | 租户ID |
| version_num | INT | NOT NULL, DEFAULT 1 | 版本号 |
| effective_time | DATETIME | NULL | 生效时间 |
| expire_time | DATETIME | NULL | 失效时间 |
| grant_type | TINYINT | NOT NULL, DEFAULT 1 | 授予方式 |

**用途**：实现角色与权限的多对多关联。

---

### 3.4 权限扩展层

#### 3.4.1 sys_role_group / sys_permission_group - 分组表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| group_name | VARCHAR(100) | NOT NULL | 分组名称 |
| group_code | VARCHAR(64) | NOT NULL, UNIQUE(tenant_id) | 分组编码 |
| group_desc | VARCHAR(500) | NULL | 分组描述 |
| parent_group_id | BIGINT | NULL | 父分组ID |
| group_level | INT | NOT NULL, DEFAULT 1 | 分组层级 |
| sort_order | INT | NOT NULL, DEFAULT 0 | 排序号 |
| is_system | TINYINT | NOT NULL, DEFAULT 0 | 是否系统预置 |

**用途**：对角色/权限进行分组管理，便于批量授权。

#### 3.4.2 sys_role_group_member / sys_permission_group_member - 分组关联表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| group_id | BIGINT | NOT NULL | 分组ID |
| role_id / permission_id | BIGINT | NOT NULL | 角色/权限ID |
| priority | INT | NOT NULL, DEFAULT 100 | 优先级 |
| effective_time | DATETIME | NULL | 生效时间 |
| expire_time | DATETIME | NULL | 失效时间 |
| status | TINYINT | NOT NULL, DEFAULT 0 | 状态：0=有效 1=无效 |

**用途**：建立分组与成员的关联关系。

#### 3.4.3 sys_role_inheritance / sys_permission_inheritance - 继承关系表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| parent_role_id / parent_permission_id | BIGINT | NOT NULL | 父角色/权限ID |
| child_role_id / child_permission_id | BIGINT | NOT NULL | 子角色/权限ID |
| inherit_type | TINYINT | NOT NULL | 继承类型 |
| inherit_ratio | DECIMAL(5,2) | NULL | 继承比例 |
| override_enabled | TINYINT | NOT NULL, DEFAULT 0 | 是否允许覆盖 |
| priority | INT | NOT NULL, DEFAULT 100 | 优先级 |
| start_time | DATETIME | NULL | 生效开始时间 |
| end_time | DATETIME | NULL | 生效结束时间 |

**用途**：支持角色/权限的继承关系，实现权限的自动传递。

---

### 3.5 数据权限与字段权限层

#### 3.5.1 sys_data_permission_rule - 数据权限规则表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| rule_name | VARCHAR(100) | NOT NULL | 规则名称 |
| rule_code | VARCHAR(64) | NOT NULL, UNIQUE(tenant_id) | 规则编码 |
| rule_type | TINYINT | NOT NULL | 规则类型：1=行级 2=字段级 3=数据范围 4=自定义SQL |
| target_table | VARCHAR(100) | NULL | 目标表名 |
| target_field | VARCHAR(100) | NULL | 目标字段名 |
| permission_column | VARCHAR(100) | NULL | 权限字段 |
| permission_operator | TINYINT | NOT NULL | 操作符：1== 2=!= 3=IN 4=NOT IN 5=LIKE 6=BETWEEN |
| permission_value | VARCHAR(500) | NULL | 权限值 |
| data_scope | TINYINT | NOT NULL | 数据范围：1=全部 2=本企业 3=本部门 4=本部门及下级 5=本人 6=自定义 |
| priority | INT | NOT NULL, DEFAULT 100 | 规则优先级 |

**用途**：定义行级数据权限规则，控制用户可访问的数据范围。

#### 3.5.2 sys_column_metadata - 字段元数据表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| table_name | VARCHAR(100) | NOT NULL | 数据表名称 |
| column_name | VARCHAR(100) | NOT NULL | 字段名称 |
| column_label | VARCHAR(100) | NULL | 字段显示名称 |
| column_type | VARCHAR(50) | NOT NULL | 字段类型 |
| column_length | INT | NULL | 字段长度 |
| is_required | TINYINT | NOT NULL, DEFAULT 0 | 是否必填 |
| is_primary_key | TINYINT | NOT NULL, DEFAULT 0 | 是否主键 |
| is_sensitive | TINYINT | NOT NULL, DEFAULT 0 | 是否敏感字段 |
| sensitive_level | TINYINT | NULL | 敏感级别（1-4） |
| is_visible | TINYINT | NOT NULL, DEFAULT 1 | 是否可见 |

**用途**：存储数据表字段的元数据信息，支持字段级权限控制。

#### 3.5.3 sys_column_permission_rule - 字段权限规则表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| rule_name | VARCHAR(100) | NOT NULL | 规则名称 |
| rule_code | VARCHAR(64) | NOT NULL, UNIQUE(tenant_id) | 规则编码 |
| table_name | VARCHAR(100) | NOT NULL | 目标表名 |
| column_name | VARCHAR(100) | NULL | 目标字段名（NULL表示所有字段） |
| column_names | TEXT | NULL | 字段列表（JSON数组） |
| permission_type | TINYINT | NOT NULL | 字段权限类型：1=可见 2=可编辑 3=只读 4=隐藏 5=加密显示 |
| filter_condition | VARCHAR(500) | NULL | 过滤条件（JSON） |
| priority | INT | NOT NULL, DEFAULT 100 | 规则优先级 |

**用途**：定义字段级权限规则，控制字段的可见性和可编辑性。

#### 3.5.4 sys_role_column_permission - 角色字段权限关联表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| role_id | BIGINT | NOT NULL | 角色ID |
| table_name | VARCHAR(100) | NOT NULL | 数据表名称 |
| column_name | VARCHAR(100) | NOT NULL | 字段名称 |
| permission_type | TINYINT | NOT NULL | 字段权限类型 |
| effective_time | DATETIME | NULL | 生效时间 |
| expire_time | DATETIME | NULL | 失效时间 |

**用途**：为角色配置特定字段的权限。

#### 3.5.5 sys_user_column_permission - 用户字段权限覆盖表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| user_id | BIGINT | NOT NULL | 用户ID |
| table_name | VARCHAR(100) | NOT NULL | 数据表名称 |
| column_name | VARCHAR(100) | NOT NULL | 字段名称 |
| permission_type | TINYINT | NOT NULL | 字段权限类型 |
| is_override | TINYINT | NOT NULL, DEFAULT 1 | 是否覆盖角色权限 |

**用途**：为特定用户配置字段权限，可覆盖角色权限设置。

---

### 3.6 缓存与审计层

#### 3.6.1 sys_permission_cache_config - 权限缓存配置表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| cache_name | VARCHAR(100) | NOT NULL | 缓存名称 |
| cache_type | TINYINT | NOT NULL | 缓存类型：1=本地 2=分布式 3=混合 |
| cache_level | TINYINT | NOT NULL | 缓存层级：1=一级 2=二级 3=三级 |
| cache_key_prefix | VARCHAR(50) | NOT NULL | 缓存键前缀 |
| cache_key_pattern | VARCHAR(200) | NOT NULL | 缓存键匹配模式 |
| ttl | INT | NOT NULL | 缓存生存时间（秒） |
| preload_enabled | TINYINT | NOT NULL, DEFAULT 0 | 是否启用预加载 |
| preload_cron | VARCHAR(100) | NULL | 预加载Cron表达式 |
| refresh_enabled | TINYINT | NOT NULL, DEFAULT 0 | 是否启用自动刷新 |
| compression_enabled | TINYINT | NOT NULL, DEFAULT 0 | 是否启用压缩 |
| encryption_enabled | TINYINT | NOT NULL, DEFAULT 0 | 是否启用加密 |
| serialization_type | TINYINT | NOT NULL, DEFAULT 0 | 序列化方式 |

**用途**：配置权限缓存策略。

#### 3.6.2 sys_user_permission_cache - 用户权限缓存表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| user_id | BIGINT | NOT NULL | 用户ID |
| permission_code | VARCHAR(64) | NOT NULL | 权限编码 |
| permission_id | BIGINT | NOT NULL | 权限ID |
| permission_type | TINYINT | NOT NULL | 权限类型 |
| data_scope | TINYINT | NOT NULL | 数据权限范围 |
| grant_type | TINYINT | NOT NULL | 授予方式：1=直接 2=角色 3=部门 |
| role_id | BIGINT | NULL | 角色ID |
| dept_id | BIGINT | NULL | 部门ID |
| expire_time | DATETIME | NULL | 过期时间 |
| cache_version | INT | NOT NULL, DEFAULT 1 | 缓存版本号 |
| is_valid | TINYINT | NOT NULL, DEFAULT 1 | 是否有效 |

**用途**：缓存用户的权限信息，提高权限校验性能。

#### 3.6.3 sys_permission_audit_log - 权限审计日志表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| audit_type | TINYINT | NOT NULL | 审计类型：1=授予 2=撤销 3=变更 4=查询 5=删除 6=角色权限变更 |
| target_type | TINYINT | NOT NULL | 目标类型：1=用户 2=角色 3=部门 4=企业 |
| target_id | BIGINT | NOT NULL | 目标ID |
| permission_id | BIGINT | NULL | 权限ID |
| operator_id | BIGINT | NOT NULL | 操作人ID |
| operator_name | VARCHAR(100) | NOT NULL | 操作人姓名 |
| operator_type | TINYINT | NOT NULL | 操作人类型 |
| request_ip | VARCHAR(50) | NULL | 请求IP地址 |
| request_url | VARCHAR(500) | NULL | 请求URL |
| request_method | VARCHAR(20) | NULL | 请求方法 |
| request_params | TEXT | NULL | 请求参数（JSON） |
| response_status | TINYINT | NULL | 响应状态：0=成功 1=失败 |
| execution_time | INT | NULL | 执行时长（毫秒） |

**用途**：记录所有权限相关操作的审计日志。

#### 3.6.4 sys_permission_history - 权限变更历史表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| permission_id | BIGINT | NOT NULL | 权限ID |
| version_num | INT | NOT NULL | 版本号 |
| change_type | TINYINT | NOT NULL | 变更类型：1=新增 2=修改 3=删除 4=恢复 |
| change_field | VARCHAR(100) | NULL | 变更字段 |
| old_value | TEXT | NULL | 旧值 |
| new_value | TEXT | NULL | 新值 |
| change_reason | VARCHAR(500) | NULL | 变更原因 |
| rollback_id | BIGINT | NULL | 回滚版本ID |
| is_rollback_point | TINYINT | NOT NULL, DEFAULT 0 | 是否为回滚点 |

**用途**：记录权限的版本变更历史，支持回滚操作。

---

### 3.7 其他表

#### 3.7.1 sys_role_permission_batch - 角色权限批量操作记录表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| batch_no | VARCHAR(64) | NOT NULL | 批次号 |
| role_id | BIGINT | NOT NULL | 角色ID |
| permission_ids | TEXT | NOT NULL | 权限ID列表（JSON） |
| operation_type | TINYINT | NOT NULL | 操作类型：1=批量授予 2=批量撤销 3=批量替换 |
| status | TINYINT | NOT NULL, DEFAULT 0 | 状态：0=待处理 1=处理中 2=成功 3=失败 |
| success_count | INT | NOT NULL, DEFAULT 0 | 成功数量 |
| fail_count | INT | NOT NULL, DEFAULT 0 | 失败数量 |

**用途**：记录批量操作的执行状态和结果。

#### 3.7.2 sys_user_enterprise - 用户-企业关联表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| user_id | BIGINT | NOT NULL | 用户ID |
| enterprise_id | BIGINT | NOT NULL | 企业ID |
| role_id | BIGINT | NULL | 用户在企业中的角色ID |
| manage_scope | VARCHAR(100) | NULL | 管理权限范围 |

**用途**：记录用户与企业的关联关系。

#### 3.7.3 sys_demo - 演示信息表

| 字段名 | 数据类型 | 约束 | 说明 |
|-------|---------|------|------|
| id | BIGINT | NOT NULL, PRIMARY KEY | 主键ID |
| demo_name | VARCHAR(100) | NOT NULL | 演示名称 |
| demo_desc | TEXT | NULL | 演示描述 |

**用途**：演示数据存储表。

---

## 四、表间关系分析

### 4.1 核心关系链

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│  sys_tenant │      │  sys_tenant │      │   sys_app   │
│             │◄─────│    _app     │─────►│             │
└─────────────┘      └─────────────┘      └──────┬──────┘
                                                 │
                                                 ▼
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│ sys_user    │◄─────│ sys_user    │─────►│ sys_role    │
│             │      │    _role    │      │             │
└─────────────┘      └─────────────┘      └──────┬──────┘
                                                 │
                                                 ▼
                                ┌─────────────────────────┐
                                │   sys_role_permission   │
                                │                         │
                                └───────────┬─────────────┘
                                            │
                                            ▼
                                ┌─────────────────────────┐
                                │    sys_permission       │
                                │  (菜单/按钮/接口权限)    │
                                └─────────────────────────┘
```

### 4.2 组织架构关系

```
┌──────────────────┐
│ sys_enterprise   │
│  (企业树形结构)   │
└────────┬─────────┘
         │
         ├─────────────────────────────┐
         ▼                             ▼
┌──────────────────┐         ┌──────────────────┐
│   sys_dept       │         │ sys_user         │
│  (部门树形结构)   │         │  (用户基础信息)   │
└────────┬─────────┘         └────────┬─────────┘
         │                            │
         └───────────────┬────────────┘
                         ▼
              ┌──────────────────┐
              │ sys_user_enterprise │
              │   (用户-企业关联)   │
              └──────────────────┘
```

### 4.3 权限继承关系

```
┌─────────────────────────┐      ┌─────────────────────────┐
│ sys_permission          │      │ sys_role               │
│                         │      │                         │
│ parent_id ──► 自关联    │      │ parent_id ──► 自关联    │
└────────────┬────────────┘      └────────────┬────────────┘
             │                                │
             ▼                                ▼
┌─────────────────────────┐      ┌─────────────────────────┐
│ sys_permission_inheritance │   │ sys_role_inheritance   │
│  (权限继承关系)           │   │  (角色继承关系)          │
└─────────────────────────┘      └─────────────────────────┘
```

### 4.4 字段权限关系

```
┌──────────────────┐      ┌──────────────────┐
│ sys_column_metadata│    │sys_column_permission_rule│
│   (字段元数据)      │      │   (字段权限规则)         │
└────────┬─────────┘      └────────┬─────────┘
         │                         │
         └───────────────┬─────────┘
                         │
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
┌───────────────┐ ┌───────────────┐ ┌───────────────┐
│sys_role_column ││sys_user_column ││ sys_role      │
│_permission    ││_permission    ││ (字段权限配置) │
└───────────────┘ └───────────────┘ └───────────────┘
```

### 4.5 完整关联矩阵

| 主表 | 关联表 | 关联字段 | 关联类型 |
|-----|-------|---------|---------|
| sys_tenant | sys_tenant_app | tenant_id | 1:N |
| sys_app | sys_tenant_app | app_id | 1:N |
| sys_app | sys_permission | app_id | 1:N |
| sys_permission | sys_role_permission | permission_id | 1:N |
| sys_role | sys_role_permission | role_id | 1:N |
| sys_role | sys_user_role | role_id | 1:N |
| sys_user | sys_user_role | user_id | 1:N |
| sys_enterprise | sys_dept | ent_id | 1:N |
| sys_dept | sys_user | dept_id | 1:N |
| sys_enterprise | sys_user | ent_id | 1:N |
| sys_role_group | sys_role_group_member | group_id | 1:N |
| sys_permission_group | sys_permission_group_member | group_id | 1:N |
| sys_role | sys_role_column_permission | role_id | 1:N |
| sys_user | sys_user_column_permission | user_id | 1:N |

---

## 五、关键业务逻辑说明

### 5.1 权限控制流程

#### 用户权限获取流程
```
用户登录 → 获取用户角色 → 查询角色权限 → 计算继承权限 → 合并字段权限 → 返回最终权限集合
```

#### 权限校验流程
```
请求进入 → 解析Token获取用户ID → 查询用户权限缓存 → 权限匹配校验 → 返回校验结果
```

### 5.2 数据权限规则

| 数据范围 | 编码 | 说明 |
|---------|------|------|
| 全部 | 1 | 可访问所有数据 |
| 本企业 | 2 | 仅可访问本企业数据 |
| 本部门 | 3 | 仅可访问本部门数据 |
| 本部门及下级 | 4 | 可访问本部门及子部门数据 |
| 本人 | 5 | 仅可访问本人相关数据 |
| 自定义 | 6 | 根据自定义规则筛选 |

### 5.3 字段权限类型

| 权限类型 | 编码 | 说明 |
|---------|------|------|
| 可见 | 1 | 字段可见 |
| 可编辑 | 2 | 字段可见且可编辑 |
| 只读 | 3 | 字段可见但不可编辑 |
| 隐藏 | 4 | 字段不可见 |
| 加密显示 | 5 | 字段可见但内容加密显示 |

### 5.4 继承类型

| 继承类型 | 编码 | 说明 |
|---------|------|------|
| 完全继承 | 1 | 完全继承父角色/权限的所有权限 |
| 部分继承 | 2 | 按比例继承（inherit_ratio） |
| 覆盖继承 | 3 | 子角色权限覆盖父角色权限 |
| 互斥 | 4 | 子角色与父角色权限互斥 |

### 5.5 多租户隔离机制

所有业务表均通过 `tenant_id` 字段实现租户隔离：
- `tenant_id = NULL`：系统级数据
- `tenant_id = 具体值`：租户级数据

查询时自动过滤 `deleted = 0` 的数据，实现逻辑删除。

---

## 六、总结

本权限管理系统数据库设计具有以下特点：

1. **多租户架构**：通过 `tenant_id` 实现租户数据隔离
2. **三级权限体系**：功能权限（菜单/按钮/接口）、数据权限（行级）、字段权限（列级）
3. **继承机制**：支持角色和权限的继承关系
4. **缓存优化**：通过用户权限缓存表提高权限校验性能
5. **审计追踪**：完整的审计日志和变更历史记录
6. **标准化设计**：统一的字段命名和设计模式
7. **逻辑删除**：所有表均支持软删除

该设计满足企业级权限管理的核心需求，具备良好的扩展性和可维护性。