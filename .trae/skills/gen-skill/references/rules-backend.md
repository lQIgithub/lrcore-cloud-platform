# LRCore Cloud Platform - 后端开发规范

## 1. 规则概述

### 1.1 文档目的

本文档定义了 LRCore Cloud Platform 项目后端服务开发的完整规范，包括模块结构、命名规范、代码风格、配置规范等。

### 1.2 适用范围

适用于所有后端开发人员。

### 1.3 执行原则

- **严格遵守**：所有后端开发活动必须严格遵守本规则
- **一致性**：保持代码风格和结构的一致性
- **可维护性**：编写易于维护的代码

---

## 2. 模块结构规范

### 2.1 父子模块结构

```
lrcore-{模块名}-modules/           # 父模块（聚合模块）
├── db/                           # 数据库脚本（仅适用于需要数据库的模块）
│   └── {模块名}_schema.sql        # 模块数据库脚本
├── lrcore-{模块名}/               # 业务实现模块（可独立运行的服务）
│   ├── src/main/java/com/lrcore/{模块名}/
│   │   ├── Lrcore{模块名驼峰}Application.java    # 启动类
│   │   ├── config/               # 配置类
│   │   ├── controller/           # 控制器
│   │   ├── service/              # 服务接口
│   │   ├── service/impl/         # 服务实现
│   │   ├── mapper/               # MyBatis-Flex Mapper
│   │   ├── domain/               # 实体类
│   │   ├── feignclient/          # Feign 客户端
│   │   └── utils/                # 工具类
│   ├── src/main/resources/
│   │   ├── mapper/               # XML映射文件目录
│   │   ├── banner.txt            # 启动Banner
│   │   ├── bootstrap.yml         # 启动配置
│   │   └── logback.xml           # 日志配置
│   └── pom.xml
├── lrcore-{模块名}-api/           # API模块（对外暴露的接口和DTO）
│   ├── src/main/java/com/lrcore/{模块名}/api/
│   │   ├── Remote{模块名驼峰}Api.java       # 远程调用接口
│   │   ├── dto/                  # DTO对象
│   │   └── factory/              # 熔断降级工厂
│   └── pom.xml
└── pom.xml
```

### 2.2 目录职责说明

| 目录            | 职责        | 说明                    |
|-----------------|-------------|-------------------------|
| `config/`       | 配置类      | Spring配置类、Bean定义  |
| `controller/`   | 控制器      | REST API控制层          |
| `service/`      | 服务层      | 业务逻辑接口            |
| `service/impl/` | 服务实现    | 业务逻辑实现类          |
| `mapper/`       | 数据访问层  | MyBatis-Flex Mapper接口 |
| `domain/`       | 实体类      | 数据库实体定义          |
| `feignclient/`  | Feign客户端 | 远程服务调用            |
| `utils/`        | 工具类      | 通用工具方法            |

---

## 3. 命名规范

| 层级         | 命名规则                            | 示例                          |
|--------------|-------------------------------------|-------------------------------|
| 父模块       | `lrcore-{模块名}-modules`           | `lrcore-system-modules`       |
| 业务模块     | `lrcore-{模块名}`                   | `lrcore-rule`                 |
| API模块      | `lrcore-{模块名}-api`               | `lrcore-rule-api`             |
| 启动类       | `Lrcore{模块名驼峰}Application`     | `LrcoreFlowableApplication`   |
| 包名         | `com.lrcore.{模块名}`               | `com.lrcore.system`           |
| API包名      | `com.lrcore.{模块名}.api`           | `com.lrcore.system.api`       |
| 远程接口     | `Remote{模块名驼峰}Api`             | `RemoteSystemApi`             |
| 熔断降级工厂 | `Remote{模块名驼峰}FallbackFactory` | `RemoteSystemFallbackFactory` |
| 控制器       | `{模块名}Controller`                | `SystemController`            |
| 服务接口     | `I{模块名}Service`                  | `ISystemService`              |
| 服务实现     | `{模块名}ServiceImpl`               | `SystemServiceImpl`           |
| 实体类       | `{模块名}Entity`                    | `SystemEntity`                |
| Mapper       | `{模块名}Mapper`                    | `SystemMapper`                |
| DTO          | `{模块名}Dto`                       | `SystemDto`                   |

---

## 4. 代码风格规范

### 4.1 启动类规范

```java
package com.lrcore.{模块名};

import com.lrcore.common.annotations.annotation.LrcoreCloudApplication;
import org.springframework.boot.SpringApplication;
/**
 * {模块中文描述}
 *
 * @author lrcore
 */
@LrcoreCloudApplication
public class Lrcore{模块名驼峰}Application {
    public static void main(String[] args) {
        SpringApplication.run(Lrcore{模块名驼峰}Application.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  {模块中文名}模块启动成功   ლ(´ڡ`ლ)ﾟ");
    }
}

```

### 4.2 Controller 规范

- 使用 `@RestController` 注解
- 使用 `@RequiredArgsConstructor` 进行依赖注入
- 使用 `@Schema` 文档注解
- 继承 `BaseController` 类
- **类注解规范**：`@Schema(description = "[控制器名称] 控制器")`
- **方法注解规范**：`@Schema(description = "[方法名称] 功能")`

#### 4.2.1 统一API响应格式规范

**核心要求**：控制器中所有返回操作必须使用 `ApiResult.success()` 静态方法。

**规范说明**：

- **统一返回格式**：所有控制器方法的返回值必须通过 `ApiResult.success()` 静态方法进行封装
- **类型安全**：使用泛型 `ApiResult<T>` 确保返回数据的类型安全
- **一致性**：保证前后端数据交互格式的统一性，便于前端统一处理
- **可扩展性**：便于后续添加统一的响应拦截、日志记录、异常处理等增强功能

**规范示例**： ✅ **正确示例**：

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/user")
@Schema(description = "用户管理控制器")
public class SysUserController extends BaseController {

    @GetMapping("/list")
    @Schema(description = "获取用户列表")
    public ApiResult<List<SysUserEntity>> list() {
        List<SysUserEntity> list = userService.list();
        return ApiResult.success(list);  // ✅ 正确：使用ApiResult.success()
    }

    @GetMapping("/{userId}")
    @Schema(description = "获取用户详情")
    public ApiResult<SysUserEntity> getInfo(@PathVariable Long userId) {
        return ApiResult.success(userService.getById(userId));  // ✅ 正确：使用ApiResult.success()
    }

    @PostMapping
    @Schema(description = "新增用户")
    public ApiResult<Void> add(@RequestBody SysUserEntity user) {
        userService.save(user);
        return ApiResult.success();  // ✅ 正确：无返回值时使用ApiResult.success()
    }

    @DeleteMapping("/{userId}")
    @Schema(description = "删除用户")
    public ApiResult<Void> delete(@PathVariable Long userId) {
        userService.removeById(userId);
        return ApiResult.success();  // ✅ 正确：无返回值时使用ApiResult.success()
    }

    @PutMapping
    @Schema(description = "修改用户")
    public ApiResult<Void> update(@RequestBody SysUserEntity user) {
        userService.updateById(user);
        return ApiResult.success();  // ✅ 正确：无返回值时使用ApiResult.success()
    }
}
```

**ApiResult 常用方法**：

| 方法名                                     | 用途                   | 示例                                      |
|--------------------------------------------|------------------------|-------------------------------------------|
| `ApiResult.success()`                      | 返回成功响应（无数据） | `return ApiResult.success();`             |
| `ApiResult.success(T data)`                | 返回成功响应（带数据） | `return ApiResult.success(list);`         |
| `ApiResult.fail(String message)`           | 返回失败响应           | `return ApiResult.fail("操作失败");`      |
| `ApiResult.fail(int code, String message)` | 返回自定义失败响应     | `return ApiResult.fail(500, "系统错误");` |

**特殊场景处理**：

1. **分页查询返回**：

```java
@GetMapping("/page")
@Schema(description = "分页查询用户")
public ApiResult<Page<SysUserEntity>> page(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize) {
    Page<SysUserEntity> page = userService.page(pageNum, pageSize);
    return ApiResult.success(page);  // ✅ 正确：分页数据直接返回
}
```

2. **批量操作返回**：

```java
@PostMapping("/batch")
@Schema(description = "批量删除用户")
public ApiResult<Integer> batchDelete(@RequestBody List<Long> userIds) {
    int count = userService.removeByIds(userIds);
    return ApiResult.success(count);  // ✅ 正确：返回影响行数
}
```

**验证标准**：

- ✅ 检查所有控制器方法是否使用 `ApiResult.success()` 返回
- ✅ 检查是否存在直接使用 `return success()` 的情况
- ✅ 检查返回类型是否为 `ApiResult<T>` 泛型类型
- ✅ 检查响应数据的类型是否与泛型参数一致

**参考**：

- [rules-security.md](./rules-security.md) - 安全认证相关规范
- BaseController 基类定义 - 继承关系说明

**参考**: [rules-security.md](./rules-security.md) - 安全认证相关规范

### 4.3 Service 规范

- 接口名以 `I` 开头
- **接口必须继承 `com.mybatisflex.core.service.IService`**
- 实现类必须继承 `com.mybatisflex.spring.service.impl.ServiceImpl`
- **禁止自行实现 CRUD 方法**：必须使用 IService 接口提供的标准方法
- 本地事务使用 `@Transactional` 注解进行事务管理
- 分布式环境下，事务管理由分布式事务框架（如 Seata）负责

#### 4.3.1 Service 目录分离规范（核心规则）

**强制要求**：服务接口与服务实现类必须分目录存放，严禁将实现类与接口放在同一目录下。

**目录结构**：

```
service/                          # 服务接口目录（仅存放接口定义）
├── I{业务名}Service.java         # 服务接口
└── impl/                         # 服务实现目录（仅存放实现类）
    └── {业务名}ServiceImpl.java  # 服务实现类
```

**包路径规范**：

- 服务接口包：`com.lrcore.{模块名}.service`
- 服务实现包：`com.lrcore.{模块名}.service.impl`

**原因说明**：

- 接口与实现分离是 Java 开发的基本规范，有利于代码的解耦和维护
- 便于快速定位接口定义和实现逻辑
- 与系统模块（lrcore-system）的现有结构保持一致

❌ **错误示例**：

```
service/
├── ISysSystemService.java        # ❌ 接口
└── SysSystemServiceImpl.java     # ❌ 实现类与接口在同一目录
```

✅ **正确示例**：

```
service/
├── ISysSystemService.java        # ✅ 接口在 service/ 目录
└── impl/
    └── SysSystemServiceImpl.java # ✅ 实现类在 service/impl/ 目录
```

**实现类包声明规范**：

```java
// ✅ 正确：实现类的 package 必须包含 .impl
package com.lrcore.system.service.impl;

// ❌ 错误：实现类的 package 不应与接口相同
package com.lrcore.system.service;
```

**验证标准**：

- ✅ 检查服务接口是否在 `service/` 目录下
- ✅ 检查服务实现类是否在 `service/impl/` 目录下
- ✅ 检查实现类的 package 声明是否包含 `.impl`
- ✅ 检查 service/ 目录下不存在任何 `*ServiceImpl.java` 文件

#### 4.3.2 实现类编码规范（核心规则）

**强制要求**：所有业务实现类必须遵循统一的编码格式和注解使用标准。

**标准编码格式**：

```java
package com.lrcore.{模块名}.service.impl;

import com.lrcore.{模块名}.domain.{业务名}Entity;
import com.lrcore.{模块名}.mapper.{业务名}Mapper;
import com.lrcore.{模块名}.service.I{业务名}Service;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class {业务名}ServiceImpl extends ServiceImpl<{业务名}Mapper, {业务名}Entity> implements I{业务名}Service {

}
```

**完整案例**：

```java
package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysUserEntity;
import com.lrcore.system.mapper.SysUserMapper;
import com.lrcore.system.service.ISysUserService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUserEntity> implements ISysUserService {

}
```

**注解说明**：

| 注解                       | 作用       | 说明                                                                                                                                                                 |
|----------------------------|------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `@Slf4j`                   | 日志支持   | Lombok 自动生成 `private static final Logger log = LoggerFactory.getLogger(类名.class)`，无需手动声明日志对象，可直接使用 `log.info()`、`log.error()` 等方法记录日志 |
| `@Service`                 | 服务层标识 | Spring 容器自动扫描并注册为 Bean，明确标识该类为业务服务层组件，便于分层架构识别和 AOP 切面管理                                                                      |
| `@RequiredArgsConstructor` | 构造器注入 | Lombok 自动生成包含所有 `final` 字段的构造函数，配合 Spring 的构造器注入机制实现依赖注入，替代 `@Autowired` 字段注入，具有不可变性和更好的可测试性                   |

**命名规范**：

| 规则       | 格式                              | 示例                                        |
|------------|-----------------------------------|---------------------------------------------|
| 实现类命名 | `{业务名}ServiceImpl`             | `SysUserServiceImpl`                        |
| 继承父类   | `ServiceImpl<{Mapper}, {Entity}>` | `ServiceImpl<SysUserMapper, SysUserEntity>` |
| 实现接口   | `I{业务名}Service`                | `ISysUserService`                           |

**规范优势**：

1. **统一性**：所有实现类遵循相同的编码格式，降低团队协作中的认知负担，新成员可快速理解代码结构
2. **可维护性**：`@Slf4j` 提供开箱即用的日志能力，业务方法中可直接使用 `log` 记录关键操作，便于问题排查和运行监控
3. **安全性**：`@RequiredArgsConstructor` 配合 `final` 字段实现构造器注入，确保依赖在对象创建时即完成初始化，避免空指针异常，同时支持不可变设计
4. **规范性**：`@Service` 明确标识服务层角色，便于架构分层治理、事务管理和 AOP 增强

**验证标准**：

- ✅ 检查实现类是否添加 `@Slf4j` 注解
- ✅ 检查实现类是否添加 `@Service` 注解
- ✅ 检查实现类是否添加 `@RequiredArgsConstructor` 注解
- ✅ 检查实现类命名是否符合 `{业务名}ServiceImpl` 格式
- ✅ 检查实现类是否继承 `ServiceImpl<{Mapper}, {Entity}>`
- ✅ 检查实现类是否实现对应的 `I{业务名}Service` 接口
- ✅ 检查实现类中不存在 `@Autowired` 字段注入

### 4.4 Mapper 规范

- 使用 MyBatis-Flex 框架
- 业务Mapper 接口继承 `BaseMapper<Entity>`
- 复杂查询使用 `QueryWrapper` 构建
- XML 映射文件放在 `resources/mapper/` 目录下

#### 4.4.1 Mapper 编码规范（核心规则）

**强制要求**：所有业务 Mapper 接口必须遵循统一的编码格式和注解使用标准。

**标准编码格式**：

```java
package com.lrcore.{模块名}.mapper;

import com.lrcore.{模块名}.domain.{业务名}Entity;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface {业务名}Mapper extends BaseMapper<{业务名}Entity> {

}
```

**完整案例**：

```java
package com.lrcore.system.mapper;

import com.lrcore.system.domain.SysUserEntity;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUserEntity> {

}
```

**注解说明**：

| 注解      | 作用       | 说明                                                                                                 |
|-----------|------------|------------------------------------------------------------------------------------------------------|
| `@Mapper` | Mapper标识 | 标识该接口为 MyBatis 映射接口，Spring 容器启动时自动扫描并生成对应的代理实现类，无需手动编写实现代码 |

**命名规范**：

| 规则           | 格式                                | 示例                              |
|----------------|-------------------------------------|-----------------------------------|
| Mapper接口命名 | `{业务名}Mapper`                    | `SysUserMapper`                   |
| 继承父接口     | `BaseMapper<{Entity}>`              | `BaseMapper<SysUserEntity>`       |
| 实体类对应关系 | `{业务名}Mapper` → `{业务名}Entity` | `SysUserMapper` → `SysUserEntity` |

**规范优势**：

1. **标识明确**：`@Mapper` 注解明确标识数据访问层接口，Spring Boot 可自动注册代理 Bean，避免因扫描遗漏导致注入失败
2. **命名一致**：Mapper 接口与实体类保持命名对应关系（`{业务名}Mapper` ↔ `{业务名}Entity`），便于快速定位关联的实体和映射关系
3. **格式统一**：所有 Mapper 接口遵循相同的编码格式，降低团队协作中的认知负担

**验证标准**：

- ✅ 检查 Mapper 接口是否添加 `@Mapper` 注解
- ✅ 检查 Mapper 接口命名是否符合 `{业务名}Mapper` 格式
- ✅ 检查 Mapper 接口是否继承 `BaseMapper<{业务名}Entity>`
- ✅ 检查泛型参数是否为对应的业务实体类
- ✅ 检查 Mapper 接口名称与实体类名称的对应关系（`{业务名}Mapper` → `{业务名}Entity`）

### 4.5 实体类规范

- 实体类必须继承 `BaseEntity` 类
- `BaseEntity`类必须导入 `ipmort com.lrcore.common.core.web.domain.BaseEntity`
- **禁止业务实体生成id属性并使用 `@Id` 注解**：主键管理由 `BaseEntity` 统一处理
- **属性字段映射通过默认命名约定实现**：如 `userId` 映射到 `user_id` 字段，也可自定义 `@Column` 注解指定映射关系
- 使用正确的 MyBatis-Flex 导入规则
- 使用 Lombok 注解简化代码

#### 4.5.1 DTO实体类规范（核心规则）

**强制要求**：所有API模块的DTO实体类必须严格遵循以下编码规范。

##### 标准编码格式

```java
package com.lrcore.{模块名}.api.dto;

import com.lrcore.common.core.web.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * <p>类模块说明</p>
 *
 * @Describe: xxx信息表 sys_xxx
 * @ClassName: {业务名}Dto
 * @Author: lrcore
 * @Date: 2026/5/29
 * @Version: 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "{业务名}DTO")
public class {业务名}Dto extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字段说明
     */
    @Schema(description = "字段说明")
    @NotBlank(message = "字段不能为空")
    @Size(max = 50, message = "字段长度不能超过50个字符")
    private String fieldName;
}
```

##### 注解说明

| 注解                                   | 作用                     | 说明                         |
|----------------------------------------|--------------------------|------------------------------|
| `@Data`                                | 自动生成getter/setter    | Lombok注解                   |
| `@EqualsAndHashCode(callSuper = true)` | equals和hashCode包含父类 | 继承父类属性参与比较         |
| `@ToString(callSuper = true)`          | toString包含父类         | 父类属性也打印               |
| `@Builder`                             | 支持构建者模式           | 可以使用链式调用             |
| `@Accessors(chain = true)`             | 链式调用支持             | setter返回this               |
| `@Schema`                              | API文档注解              | Swagger/OpenAPI文档          |
| `@NotBlank`                            | 非空校验                 | javax.validation.constraints |
| `@Size`                                | 长度校验                 | javax.validation.constraints |

##### 规范优势

1. **统一性**：所有DTO遵循相同的编码格式，降低团队协作中的认知负担
2. **继承性**：继承BaseEntity获得基础属性（如创建时间、更新时间等）
3. **可验证性**：通过validation注解实现参数校验，提高数据安全性
4. **可文档化**：通过@Schema注解自动生成API文档

##### 验证清单

- ✅ DTO类是否继承 `BaseEntity`
- ✅ 是否添加 `@Serial` 和 `serialVersionUID`
- ✅ 是否包含 `@Accessors(chain = true)` 注解
- ✅ 类注释是否包含 `@Describe`、`@ClassName`、`@Author`、`@Date`、`@Version`
- ✅ 字段注释是否使用JavaDoc风格（`/** 注释 */`）
- ✅ 必要字段是否添加 `@NotBlank` 和 `@Size` 校验注解

### 4.6 日期时间处理规范

- **强制使用 Java 8 日期时间 API**：`LocalDateTime`、`LocalDate`、`LocalTime`
- **禁止使用旧版日期时间类**：`Date`、`Calendar`、`Timestamp`
- 创建当前日期时间使用 `LocalDateTime.now()`
- 使用 `DateTimeFormatter` 进行日期时间格式化
- 使用 `@JsonFormat` 注解进行 JSON 序列化和反序列化

---

## 5. 配置文件规范

### 5.1 服务名和端口配置

**新生成模块必须在顶层 pom.xml 文件中配置：**

1. **服务名称配置**：
   ```xml
   <service-name-{模块名}>{服务名}</service-name-{模块名}>
   ```

2. **开发环境端口配置**：
   ```xml
   <lrcore.{模块名}-port>{端口号}</lrcore.{模块名}-port>
   ```

3. **端口分配规则**：
    - 网关服务：10801
    - 认证服务：10802
    - 系统服务：10803
    - 文件服务：10804
    - 定时任务：10805
    - 新服务：按顺序递增

### 5.2 API模块规范

**RemoteApi 接口规范**：

- 使用 `@FeignClient` 注解
- 包含 `contextId`、`value` 和 `fallbackFactory` 属性
- 方法返回类型使用 `ApiResult<T>`

**FallbackFactory 规范**：

- 实现 `FallbackFactory` 接口
- 使用 `@Slf4j` 和 `@Component` 注解

#### 5.2.1 FallbackFactory 核心规范（核心规则）

##### 规范要点

| 要点           | 说明                                                           |
|----------------|----------------------------------------------------------------|
| **包路径**     | 必须使用 `org.springframework.cloud.openfeign.FallbackFactory` |
| **注解要求**   | `@Slf4j`、`@Component`                                         |
| **返回值类型** | 方法返回类型必须使用 `ApiResult<T>`                            |

##### FallbackFactory 实现类模板

```java
package com.lrcore.{模块名}.api.factory;

import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.{模块名}.api.Remote{业务名}Api;
import com.lrcore.{模块名}.api.dto.{业务名}Dto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Remote{业务名}FallbackFactory implements FallbackFactory<Remote{业务名}Api> {

    @Override
    public Remote{业务名}Api create(Throwable throwable) {
        log.error("{业务名}服务调用失败: {}", throwable.getMessage());
        return new Remote{业务名}Api() {
            @Override
            public ApiResult<{业务名}Dto> getInfo(String id) {
                log.error("获取{业务名}信息失败, id: {}", id, throwable);
                return ApiResult.fail("获取{业务名}信息失败");
            }
        };
    }
}
```

##### FeignClient 接口模板

```java
package com.lrcore.{模块名}.api;

import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.{模块名}.api.dto.{业务名}Dto;
import com.lrcore.{模块名}.api.factory.Remote{业务名}FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    contextId = "remote{业务名}Api",
    value = "lrcore-{模块名}",
    fallbackFactory = Remote{业务名}FallbackFactory.class
)
public interface Remote{业务名}Api {

    @GetMapping("/{模块名}/getInfo/{id}")
    ApiResult<{业务名}Dto> getInfo(@PathVariable("id") String id);
}
```

##### Spring Boot 自动配置规范（核心规则）

**强制要求**：所有 API 接口模块必须在其资源目录下生成
`resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 文件。

**文件位置**：

```
lrcore-{模块名}-api/
└── src/
    └── main/
        └── resources/
            └── META-INF/
                └── spring/
                    └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

**文件内容**：

```
com.lrcore.{模块名}.api.factory.Remote{业务名}FallbackFactory
```

**原因说明**：

- 确保 FallbackFactory 功能在 Spring Boot 自动配置流程中能够被正确识别和加载
- 遵循 Spring Boot 的自动配置机制
- 与系统模块（lrcore-system-api）保持一致

**验证标准**：

- ✅ 检查 `org.springframework.boot.autoconfigure.AutoConfiguration.imports` 文件是否存在
- ✅ 检查文件内容是否包含对应的 FallbackFactory 全类名
- ✅ 检查文件路径是否符合 `META-INF/spring/` 目录结构

**参考实现**：

```bash
# 系统模块参考路径
lrcore-system-api/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
# 文件内容
com.lrcore.system.api.factory.RemoteUserFallbackFactory
```

#### 5.2.2 API模块依赖声明规范（核心规则）

**规范背景**

随着项目业务模块的不断扩展，API接口模块的数量也在持续增长。为了确保Maven依赖管理的规范性和一致性，避免版本冲突和依赖遗漏问题，特制定本规范。所有新建的
`lrcore-xxx-api`模块必须在项目最顶层的`pom.xml`文件中进行统一的版本管理和依赖声明。

**适用范围**

本规范适用于以下场景：

- 新建业务模块的API接口模块
- 现有模块新增API子模块
- API模块版本升级或变更

**规范要点**

| 配置项   | 规范说明                                                | 强制性 |
|----------|---------------------------------------------------------|--------|
| 配置位置 | 必须添加到顶层`pom.xml`的`<dependencyManagement>`节点中 | 强制   |
| 依赖声明 | 必须使用`<dependencies>`标签包裹所有依赖项              | 强制   |
| 版本管理 | 必须引用`${lrcore.version}`变量实现版本统一管理         | 强制   |
| 注释规范 | 必须添加清晰的模块分类注释说明                          | 推荐   |

**标准配置模板**

```xml
<!-- 依赖声明 -->
<dependencyManagement>
    <dependencies>
        <!-- 各业务系统对外暴露的OpenAPI接口模块 -->
        
        <!-- 系统模块API接口 -->
        <dependency>
            <groupId>com.lrcore</groupId>
            <artifactId>lrcore-system-api</artifactId>
            <version>${lrcore.version}</version>
        </dependency>
        <!-- 其他业务模块API接口（按需添加） -->
        
    </dependencies>
</dependencyManagement>
```

**配置示例说明**

1. **依赖声明位置**：必须在`<dependencyManagement>`标签内添加
2. **模块分类注释**：使用`<!-- 模块分类注释 -->`格式，便于识别不同业务模块
3. **版本号引用**：使用`${lrcore.version}`统一管理，避免版本不一致
4. **groupId规范**：统一使用`com.lrcore`作为组织标识

**与现有规范的关联**

| 相关规范                    | 关联说明                          |
|-----------------------------|-----------------------------------|
| 5.2节 API模块基础规范       | 本规范是API模块配置的重要组成部分 |
| 5.2.1节 FallbackFactory规范 | 两者共同构成完整的API模块开发标准 |
| 6节 模块创建规范            | 必须在创建新模块时同步执行本规范  |

**常见问题与解决方案**

| 问题场景               | 解决方案                                    |
|------------------------|---------------------------------------------|
| 依赖声明后无法找到模块 | 检查模块是否执行`mvn install`安装到本地仓库 |
| 版本号不统一           | 确认是否正确引用`${lrcore.version}`变量     |
| 依赖冲突               | 检查是否在子模块中重复声明了版本号          |

**验证清单**

创建新API模块时，请确认完成以下检查项：

- [ ] 在顶层`pom.xml`的`<dependencyManagement>`中添加依赖声明
- [ ] 使用`${lrcore.version}`统一管理版本号
- [ ] 添加清晰的模块分类注释
- [ ] 确认`groupId`为`com.lrcore`
- [ ] 子模块`pom.xml`中正确引用API模块依赖（不带版本号）

### 5.4 Maven构建配置规范

**`<build>` 标签必须保持以下固定结构**：

```xml
<build>
    <finalName>${service-name-{模块名}}-${nacos.active}</finalName>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <executions>
                <execution>
                    <goals>
                        <goal>repackage</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### 5.5 MyBatis-Flex APT构建规则

- 项目已配置 MyBatis-Flex 的 APT 技术
- 生成的代码位于 `${project.build.directory}/generated-sources/annotations`
- 使用 `build-helper-maven-plugin` 确保 IDEA 正确识别生成目录

### 5.6 bootstrap.yml 规范

```yaml
server:
  port: ${lrcore.{模块名}-port}

spring:
  application:
    name: ${service-name-{模块名}}
  profiles:
    active: ${nacos.active}
  cloud:
    nacos:
      discovery:
        server-addr: ${nacos.server-addr}
        namespace: ${nacos.namespace}
      config:
        server-addr: ${nacos.server-addr}
        namespace: ${nacos.namespace}
  config:
    import:
      - nacos:application-${nacos.active}.${nacos.file-extension}
      - nacos:${service-name-{模块名}}-${nacos.active}.${nacos.file-extension}
```

### 5.7 logback.xml 规范

- 日志文件路径：`logs/lrcore-{模块名}/`
- 日志级别：INFO
- 日志格式：`%d{HH:mm:ss.SSS} [%thread] %-5level %logger{20} - [%method,%line] - %msg%n`
- 日志文件保留 60 天

---

## 6. 模块创建规范

**新建模块时，其 `pom.xml` 文件中的依赖配置必须遵循以下规则：**

1. **依赖配置参照标准**：必须完全参照 `lrcore-rule` 模块的 `pom.xml` 文件
2. **参照文件路径**：`lrcore-buzi-modules/lrcore-system-modules/lrcore-system/pom.xml`
3. **强制要求**：
    - 不得擅自修改依赖版本号
    - 不得擅自添加或删除依赖项
    - 不得擅自修改 `scope` 或 `exclusions` 配置
    - 如有特殊需求，必须经过项目负责人审批

### 6.1 IntelliJ IDEA 模块文件（.iml）规范

**强制要求**：当开发人员在项目中创建新模块时，必须同步创建对应的 `lrcore-xxx.iml` 文件。

**原因说明**：

- 确保 IntelliJ IDEA 开发工具能够正确识别模块结构
- 保证项目依赖关系的正确解析
- 维护开发环境的一致性
- 提高团队协作开发效率

**iml 文件命名规范**： | 模块类型 | 命名规则 | 示例 | |---------|---------|------| | 业务模块 | `lrcore-{模块名}.iml` |
`lrcore-system.iml` | | API模块 | `lrcore-{模块名}-api.iml` | `lrcore-system-api.iml` | | 父模块 |
`lrcore-{模块名}-modules.iml` | `lrcore-system-modules.iml` |

**iml 文件标准模板**：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<module version="4">
  <component name="AdditionalModuleElements">
    <content url="file://$MODULE_DIR$" dumb="true">
      <sourceFolder url="file://$MODULE_DIR$/src/main/java" isTestSource="false" />
      <sourceFolder url="file://$MODULE_DIR$/src/main/resources" type="java-resource" />
    </content>
  </component>
</module>
```

**iml 文件存放位置**：

```
lrcore-buzi-modules/
└── lrcore-{模块名}-modules/
    ├── lrcore-{模块名}/                    # 业务模块根目录
    │   └── lrcore-{模块名}.iml             # 业务模块 iml 文件
    ├── lrcore-{模块名}-api/                # API模块根目录
    │   └── lrcore-{模块名}-api.iml         # API模块 iml 文件
    └── lrcore-{模块名}-modules.iml         # 父模块 iml 文件
```

**验证标准**：

- ✅ 检查 .iml 文件是否已创建
- ✅ 检查 .iml 文件命名是否符合规范
- ✅ 检查 .iml 文件内容格式是否正确
- ✅ 检查 .iml 文件存放位置是否正确

**参考**: [rules-version.md](./rules-version.md) - 版本依赖保护规则

---

## 7. 交叉引用

- [返回索引](./rules-index.md)
- [版本依赖保护规则](./rules-version.md) - 依赖版本管理
- [安全规范](./rules-security.md) - 安全认证相关
- [部署规范](./rules-deployment.md) - 部署配置相关

---

## 版本历史

| 版本 | 日期       | 变更说明                                                                                                                          |
|------|------------|-----------------------------------------------------------------------------------------------------------------------------------|
| v1.4 | 2026-05-30 | 新增API模块依赖声明规范（5.2.2节），明确要求新建API模块必须在顶层pom.xml中声明依赖                                                |
| v1.3 | 2026-05-30 | 新增 Spring Boot 自动配置规范（5.2.1节），明确要求 API 模块必须生成 AutoConfiguration.imports 文件以确保 FallbackFactory 正确加载 |
| v1.2 | 2026-05-07 | 新增统一API响应格式规范（4.2.1节），明确要求使用ApiResult.success()静态方法                                                       |
| v1.1 | 2026-05-06 | 新增 IntelliJ IDEA 模块文件（.iml）规范                                                                                           |
| v1.0 | 2026-04-29 | 初始版本，从综合规则文件拆分                                                                                                      |
