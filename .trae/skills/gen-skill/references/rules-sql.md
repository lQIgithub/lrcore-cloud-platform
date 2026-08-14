# LRCore Cloud Platform - SQL Schema 生成规范

## 1. 规则概述

### 1.1 文档目的

本文档定义了 LRCore Cloud Platform 项目中数据库 Schema 文件的生成规范，确保所有 SQL Schema 文件的一致性、可维护性和标准化。本规范旨在为开发团队提供编写高质量 SQL 代码的权威参考指南。

### 1.2 适用范围

适用于所有后端开发人员和数据库设计人员。

### 1.3 执行原则

| 原则 | 说明 |
|------|------|
| **严格遵守** | 所有数据库 Schema 文件的创建和修改必须严格遵守本规范 |
| **一致性** | 保持 Schema 文件的命名和格式一致性 |
| **可追溯性** | 确保 Schema 变更可追溯和可回滚 |
| **可读性** | 代码格式清晰，注释完整，便于团队协作维护 |

---

## 2. 主键 ID 规范（核心规则）

### 2.1 主键 ID 数据类型

**强制要求**：所有表的主键 ID 字段必须使用 `BIGINT(20)` 数据类型。

**原因说明**：
- `BIGINT(20)` 足够存储雪花ID（20 位）的存储空间
- 雪花ID 具有全局唯一性，适合分布式系统
- 避免数字类型主键在分布式环境下的冲突问题
- 支持跨环境迁移和数据合并

### 2.2 主键 ID 字段定义模板

```sql
-- 主键 ID 字段标准定义
`id` BIGINT(20) NOT NULL COMMENT '主键ID',
PRIMARY KEY (`id`)
```

### 2.3 类型映射说明（重要）

**数据库与 Java 实体类类型映射关系**：

| 数据库字段类型 | Java 实体类字段类型 | 说明 |
|-------------|------------------|------|
| `BIGINT(20)` | `Long` | 主键ID、租户ID、用户ID等关联字段（雪花ID） |
| `VARCHAR(n)` | `String` | 普通字符串字段 |
| `INT` | `Integer` | 普通整数 |
| `TINYINT` | `Integer` | 小整数（状态码、标志位） |
| `DECIMAL(p,s)` | `BigDecimal` | 精确数值（金额等） |
| `DATETIME` | `LocalDateTime` | 日期时间 |
| `DATE` | `LocalDate` | 日期 |

**示例说明**：
```sql
-- 数据库定义
`id` BIGINT(20) NOT NULL COMMENT '主键ID',
`tenant_id` BIGINT(20) NULL COMMENT '租户ID',
`create_user_id` BIGINT(20) NULL COMMENT '创建者ID',
```

```java
// Java 实体类对应字段
private Long id;           // 主键ID（雪花ID）
private Long tenantId;     // 租户ID
private Long createUserId;  // 创建者ID
```

**重要提示**：
- 虽然数据库使用 `BIGINT(20)`，Java 实体类使用 `Long` 类型接收
- 这是因为雪花ID（Snowflake ID）通常以字符串形式在系统中传递和使用
- MyBatis-Flex 会自动处理 `BIGINT` 与 `Long` 之间的类型转换

### 2.4 主键命名规范

| 规则类型 | 规范 | 示例 |
|---------|------|------|
| 字段名 | 必须为 `id` | `id BIGINT(20)` |
| 注释 | 必须包含 `主键ID` | `comment '主键ID'` |
| 约束 | 必须为主键 | `primary key (id)` |
| 位置 | 必须为表第一个字段 | - |

---

## 3. Schema 文件命名规范

### 3.1 文件命名规则

| 类型 | 命名规则 | 示例 |
|-----|---------|------|
| 模块 Schema 文件 | `{模块名}_schema.sql` | `member_schema.sql` |
| 初始化数据文件 | `{模块名}_data.sql` | `member_data.sql` |
| 测试数据文件 | `{模块名}_test_data.sql` | `member_test_data.sql` |

### 3.2 文件位置

```
lrcore-{模块名}-modules/
├── db/                           # 数据库脚本目录
│   └── {模块名}_schema.sql        # Schema 文件
│   └── {模块名}_data.sql         # 初始化数据文件（可选）
└── lrcore-{模块名}/
    └── ...
```

---

## 4. 表结构设计规范

### 4.1 表字段与实体类对应关系（核心规则）

**强制要求**：所有数据库表的字段设计必须严格遵循对应 Java 实体类的字段定义，并且必须追加 BaseEntity 中定义的所有基础字段。

**原因说明**：
- 确保 ORM 映射的一致性，避免字段不匹配问题
- 统一审计字段管理，便于数据追踪
- 避免手动维护两套字段定义造成的不一致

**字段顺序规范**：
1. **id 字段**：必须作为表的第一个字段
2. **业务字段**：紧随 id 之后，与实体类字段顺序保持一致
3. **BaseEntity 公共字段**：统一放置在业务字段之后

**验证标准**：
- ✅ 检查表是否包含全部 BaseEntity 字段
- ✅ 检查业务字段是否与实体类完全对应
- ✅ 检查字段类型、长度、约束是否一致
- ✅ 检查字段注释是否准确

### 4.2 BaseEntity 字段标准定义

**所有表必须包含的 BaseEntity 字段**（按以下顺序排列）：

```sql
-- BaseEntity 公共字段（必须包含，顺序固定，放置在业务字段之后）
`tenant_id` BIGINT(20) NULL COMMENT '租户ID',
`create_user_id` BIGINT(20) NULL COMMENT '创建者ID',
`create_user_name` VARCHAR(100) NULL COMMENT '创建者姓名',
`update_user_id` BIGINT(20) NULL COMMENT '更新者ID',
`update_user_name` VARCHAR(100) NULL COMMENT '更新者姓名',
`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
`update_time` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`remark` VARCHAR(500) NULL COMMENT '备注',
`build_in` TINYINT DEFAULT 0 NULL COMMENT '是否内置(0-否 1-是)',
`deleted` TINYINT DEFAULT 0 NOT NULL COMMENT '删除标志（0未删除 1已删除）',
```

**字段说明**：

| 字段名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| tenant_id | BIGINT(20) | 否 | 无 | 租户ID，用于多租户数据隔离 |
| create_user_id | BIGINT(20) | 否 | 无 | 创建者ID |
| create_user_name | VARCHAR(100) | 否 | 无 | 创建者姓名 |
| update_user_id | BIGINT(20) | 否 | 无 | 更新者ID |
| update_user_name | VARCHAR(100) | 否 | 无 | 更新者姓名 |
| create_time | DATETIME | 是 | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | 否 | NULL（更新时自动） | 更新时间 |
| remark | VARCHAR(500) | 否 | 无 | 备注信息 |
| deleted | TINYINT | 是 | 0 | 删除标志（0未删除 1已删除） |

### 4.3 CREATE TABLE 语句格式

**标准模板**：

```sql
-- ============================================================================
-- 表名：{表名}
-- 说明：{表用途说明}
-- 创建时间：{YYYY-MM-DD}
-- 创建人：{作者}
-- ============================================================================
DROP TABLE IF EXISTS `{表名}`;
CREATE TABLE `{表名}` (
    -- 主键字段（必须为第一个字段）
    `id` BIGINT(20) NOT NULL COMMENT '主键ID',

    -- 业务字段（必须与实体类字段顺序保持一致）
    `xxx` VARCHAR(50) NOT NULL COMMENT 'xxx字段',
    `yyy` VARCHAR(50) NULL COMMENT 'yyy字段',

    -- BaseEntity 公共字段（必须包含，顺序固定，放置在业务字段之后）
    `tenant_id` BIGINT(20) NULL COMMENT '租户ID',
    `create_user_id` BIGINT(20) NULL COMMENT '创建者ID',
    `create_user_name` VARCHAR(100) NULL COMMENT '创建者姓名',
    `update_user_id` BIGINT(20) NULL COMMENT '更新者ID',
    `update_user_name` VARCHAR(100) NULL COMMENT '更新者姓名',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) NULL COMMENT '备注',
    `deleted` TINYINT DEFAULT 0 NOT NULL COMMENT '删除标志（0未删除 1已删除）',

    -- 表级约束
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_{字段名}` (`{字段名}`),
    KEY `idx_{字段名}` (`{字段名}`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='{表注释}';
```

**完整示例**：

```sql
-- ============================================================================
-- 表名：会员信息表
-- 说明：存储会员基本信息
-- 创建时间：2026-04-29
-- 创建人：lrcore
-- ============================================================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    -- 主键字段（必须为第一个字段）
    `id` BIGINT(20) NOT NULL COMMENT '主键ID',

    -- 业务字段（必须与 MemberEntity 严格一致）
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) NULL COMMENT '昵称',
    `phone` VARCHAR(20) NULL COMMENT '手机号',
    `email` VARCHAR(100) NULL COMMENT '邮箱',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0正常 1禁用）',

    -- BaseEntity 公共字段（必须包含，顺序固定）
    `tenant_id` BIGINT(20) NULL COMMENT '租户ID',
    `create_user_id` BIGINT(20) NULL COMMENT '创建者ID',
    `create_user_name` VARCHAR(100) NULL COMMENT '创建者姓名',
    `update_user_id` BIGINT(20) NULL COMMENT '更新者ID',
    `update_user_name` VARCHAR(100) NULL COMMENT '更新者姓名',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) NULL COMMENT '备注',
    `deleted` TINYINT DEFAULT 0 NOT NULL COMMENT '删除标志（0未删除 1已删除）',

    -- 表级约束
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会员信息表';
```

### 4.4 字段命名规范

| 字段类型 | 命名规则 | 示例 | 说明 |
|---------|---------|------|------|
| 主键 | `id` | `id bigint(20)` | 强制使用 bigint(20) |
| 租户 | `tenant_id` | `tenant_id bigint(20)` | 多租户场景使用 |
| 创建时间 | `create_time` | `create_time datetime` | 标准时间字段 |
| 更新时间 | `update_time` | `update_time datetime` | 标准时间字段 |
| 创建人 | `create_user_id` / `create_user_name` | `create_user_id bigint(20)` | 与主键类型一致 |
| 更新人 | `update_user_id` / `update_user_name` | `update_user_id bigint(20)` | 与主键类型一致 |
| 状态 | `{xxx}_status` 或 `status` | `status tinyint` | 使用 tinyint |
| 备注 | `remark` | `remark varchar(500)` | 标准备注字段 |
| 删除标志 | `deleted` | `deleted tinyint` | 使用 tinyint，0未删除 1已删除 |

### 4.5 字段类型规范

| 字段类型 | MySQL 类型        | 使用场景             | 示例 |
|---------|-----------------|------------------|------|
| 短文本 | `varchar(n)`    | 固定长度字符串，n ≤ 255  | `varchar(50)` |
| 长文本 | `text`          | 大文本内容（无长度限制）     | `text` |
| 整数 | `int`           | 一般整数（4字节）        | `int not null` |
| 小整数 | `tinyint`       | 状态码、标志位（1字节）     | `tinyint default 0` |
| 大整数 | `bigint`        | 金额、数量等大数值（8字节）   | `bigint not null` |
| 金额 | `decimal(20,6)` | 精确金额（20位总长，6位小数） | `decimal(20,6)` |
| 日期 | `date`          | 日期（年月日）          | `date` |
| 时间 | `datetime`      | 日期时间（年月日时分秒）     | `datetime` |
| 时间戳 | `timestamp`     | 自动更新timestamp    | `timestamp` |

### 4.6 字段约束规范

| 约束类型 | 关键字 | 使用场景 |
|---------|-------|---------|
| 非空 | `NOT NULL` | 必填字段 |
| 默认值 | `DEFAULT` | 有默认值的字段 |
| 唯一 | `UNIQUE KEY` | 唯一性字段 |
| 主键 | `PRIMARY KEY` | 主键字段 |
| 索引 | `KEY` / `INDEX` | 频繁查询字段 |
| 外键 | `FOREIGN KEY` | 关联表字段 |

---

## 5. 索引设计规范

### 5.1 索引命名规范

| 索引类型 | 命名规则 | 示例 |
|---------|---------|------|
| 主键索引 | `primary` | `PRIMARY KEY (id)` |
| 唯一索引 | `uk_{字段名}` | `UNIQUE KEY 'uk_username' ('username')` |
| 普通索引 | `idx_{字段名}` | `KEY 'idx_status' ('status')` |
| 联合索引 | `idx_{字段1}_{字段2}` | `KEY 'idx_user_status' ('user_id', 'status')` |

### 5.2 索引设计原则

1. **选择区分度高的字段建立索引**：区分度低于 10% 的字段不宜建立索引
2. **避免在频繁更新的字段上建立索引**：更新操作会同时更新索引，影响性能
3. **遵循最左前缀原则设计联合索引**：合理安排列的顺序，最大化索引效率
4. **定期分析索引使用情况**：删除冗余索引和低效索引
5. **控制索引数量**：每个表索引数量建议不超过 5 个

---

## 6. SQL 语句格式规范

### 6.1 格式化要求

1. **关键字大写**：SQL 关键字使用大写（SELECT, FROM, WHERE, INSERT 等）
2. **适当缩进**：使用 4 个空格缩进，增强可读性
3. **字段对齐**：同层次字段对齐显示
4. **添加注释**：表级注释、字段注释、约束注释必须完整

### 6.2 注释规范

```sql
-- ============================================================================
-- 表级注释：描述表的用途、创建信息等
-- ============================================================================

-- 字段级注释：每个字段必须有注释
-- 约束级注释：索引、外键等约束应有注释
```

### 6.3 字符集和排序规则

**强制要求**：所有表必须指定字符集和排序规则。

```sql
-- 推荐配置（一般场景）
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci

-- 区分大小写场景
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
```

---

## 7. 数据生成规则

**强制要求**：所有数据表在生成时，必须完整包含对应实体类中定义的所有业务字段，以及 BaseEntity 基类中定义的公共属性所对应的字段。

**原因说明**：
- 确保数据完整性和一致性
- 避免数据遗漏或字段命名、映射不一致
- 避免字段冗余或冲突
- 提高数据映射的准确性

**实施要求**：
1. **业务字段完整性**：每个数据表必须完整包含对应实体类定义的所有业务字段
2. **BaseEntity 公共字段完整性**：所有表必须无条件包含 BaseEntity 公共字段
3. **字段无重复冲突**：确保业务字段与 BaseEntity 字段无重复命名冲突
4. **统一命名规范**：遵循数据库字段与 Java 实体字段映射规则
5. **统一类型映射**：确保类型一致性和准确性
6. **完整字段注释**：所有字段必须包含明确注释，与实体类注释一致

**验证检查清单**：
- [ ] 对应实体类中的所有业务字段在数据表中已完整定义
- [ ] BaseEntity 基类中的所有公共属性在数据表中已完整定义
- [ ] 业务字段与 BaseEntity 字段无重复冲突
- [ ] 字段命名符合规范
- [ ] 字段类型符合映射规范
- [ ] 字段注释准确完整
- [ ] 字段约束与实体类一致

---

## 8. 多租户字段规范

### 8.1 租户字段定义

`tenant_id` 字段已作为标准字段包含在 BaseEntity 中，定义如下：

```sql
`tenant_id` BIGINT(20) NULL COMMENT '租户ID',
```

### 8.2 租户字段使用原则

1. **敏感操作必须包含 tenant_id 条件**：所有数据操作需考虑租户隔离
2. **数据隔离查询必须检查 tenant_id**：防止跨租户数据访问
3. **跨租户查询需要明确授权**：须经过安全审计和授权审批

---

## 9. Schema 变更规范

### 9.1 变更记录

每次 Schema 变更必须记录以下信息：
- 变更日期
- 变更人
- 变更内容
- 变更原因
- 回滚方案

### 9.2 变更脚本命名

```sql
-- 日期格式：YYYYMMDD
-- 版本格式：V{序号}
{YYYYMMDD}_{版本号}_alter_{表名}.sql

-- 示例
2026050601_V001_alter_member_add_nickname.sql
```

### 9.3 变更执行原则

1. **先备份后修改**：重要表变更前先备份数据
2. **先验证后执行**：在测试环境验证后再执行生产环境
3. **保留回滚脚本**：每次变更必须准备回滚脚本
4. **小步快跑**：尽量将大变动的拆分为多个小变更，降低风险

---

## 10. 与 Java 实体类映射规范

### 10.1 类型映射关系

| MySQL 类型        | Java 类型 | 说明 |
|-----------------|-----------|------|
| `BIGINT(20)`    | `Long` | 主键ID、关联ID（雪花ID） |
| `VARCHAR(n)`    | `String` | 字符串 |
| `INT`           | `Integer` | 整数 |
| `TINYINT`       | `Integer` | 小整数 |
| `BIGINT`        | `Long` | 长整数（业务数量等） |
| `DECIMAL(20,6)` | `BigDecimal` | 金额 |
| `DATETIME`      | `LocalDateTime` | 日期时间 |
| `DATE`          | `LocalDate` | 日期 |
| `TEXT`          | `String` | 大文本 |

### 10.2 命名映射规则

| 数据库命名 | Java 命名 | 说明 |
|-----------|-----------|------|
| `create_time` | `createTime` | 下划线转驼峰 |
| `update_time` | `updateTime` | 下划线转驼峰 |
| `create_user_id` | `createUserId` | 下划线转驼峰 |
| `update_user_id` | `updateUserId` | 下划线转驼峰 |
| `deleted` | `deleted` | 布尔类型 |
| `tenant_id` | `tenantId` | 下划线转驼峰 |

**参考**：[rules-backend.md](./rules-backend.md) - Java 实体类规范

---

## 11. 交叉引用

- [返回索引](./rules-index.md)
- [后端开发规范](./rules-backend.md) - Java 实体类规范
- [版本依赖保护规则](./rules-version.md) - 数据库相关依赖版本

---

## 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|---------|
| v1.4 | 2026-05-29 | 完善类型映射说明，修正 BIGINT(20) 与 String 的映射关系，添加数据库与 Java 实体类类型转换说明 |
| v1.3 | 2026-05-06 | 全面优化文档结构，修正章节编号，清理重复内容，统一字段顺序规范 |
| v1.2 | 2026-05-06 | 统一标准默认字段定义，添加 tenant_id、修改 deleted 字段类型为 TINYINT |
| v1.1 | 2026-05-01 | 新增数据生成规则，明确实体类与BaseEntity字段要求 |
| v1.0 | 2026-04-29 | 初始版本，定义 SQL Schema 生成规范 |
