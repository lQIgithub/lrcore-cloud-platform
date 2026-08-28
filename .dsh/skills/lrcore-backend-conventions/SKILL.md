---
name: lrcore-backend-conventions
description: 在 lrcore-cloud-platform 仓库新增或修改 Java 后端代码时使用：新增 entity/mapper/service/controller、写 MyBatis-Flex 查询、取租户/登录用户上下文、统一响应与异常、新增 Feign 接口、写规则插件前，先按本 skill 对齐项目约定。
whenToUse: 新增业务表的 CRUD 链路、编写 QueryWrapper、修改 BaseEntity 相关字段、接入 SecurityUtils/租户上下文、写 @FeignClient 或 provider 端、调整 lombok 构造注入时。
---

# lrcore 后端编码约定

所有类名/路径均为仓库或外部依赖 `lrcore-platform-tools-dependencies:2.0.0`（lrcore-common-* 不在本仓库，经根 pom BOM 引入）中真实存在的。

## 0. 动手前先读
- 根 `pom.xml`：版本、dev profile（nacos.* 属性、各服务端口）、compiler 配置
- 根 `mybatis-flex.config`：APT 生成规则（tableDef 包 `${entityPackage}.apt`、classSuffix=APT、字段 upperCase、ignoreEntitySuffixes=Entity）
- 根 `lombok.config`、`lrcore_doc.md`（28 表设计）、`lrcore_cloudv1.1添加字段注释.sql`（真实 DDL；文档与 DDL 冲突以 DDL/代码为准）
- 模板样板：`lrcore-buzi-modules/lrcore-system-modules/lrcore-system/src/main/java/com/lrcore/system/` 下 SysRole（标准 CRUD）、SysPermission（软删/租户批量）、SysDataPermissionRuleMapper（default 方法）

## 1. 模块结构（真实）
```
lrcore-cloud-platform（根 pom，packaging=pom，dev profile 默认激活）
├── lrcore-base-modules
│   ├── lrcore-gateway  10801 WebFlux 网关：AuthFilter(JWT/JWKS→下游头)、Xss/BlackList/ValidateCode 过滤器、Sentinel gw-flow
│   ├── lrcore-auth     10802 SSO 授权服务器(SAS)：SsoLoginController、LrcoreSsoLoginFilter、SasAccessTokenIssuer、验证码、社交登录、OAuth2ClientAdmin(@InnerAuth)
│   └── lrcore-monitor  9100  Spring Boot Admin（@EnableAdminServer，普通 @SpringBootApplication）
└── lrcore-buzi-modules
    ├── lrcore-system-modules
    │   ├── lrcore-system   10803 系统/权限服务：sys_ 权限表 + sys_config/sys_dict_* 的 CRUD + Flowable 工作流 controller + 数据权限 provider
    │   └── lrcore-system-api Feign 接口(Remote*Api) + DTO + FallbackFactory（AutoConfiguration.imports 注册）
    ├── lrcore-bizrule-modules/lrcore-bizrule 10804 规则引擎：JAR 热加载 + ValidateRule 执行
    └── lrcore-bizrule-plugin-modules（amount-plugin 骨架样例；phone-plugin 仅 pom 无源码）
```
lrcore-system 包结构（`com.lrcore.system`）：`controller/`（含 `workflow/`）、`domain/`（实体）+ `domain/query/`（VO：RouterVo、TreeSelect、LoginUserInfo 等）、`enums/`、`feign/`（provider 端实现）、`listen/`、`mapper/`、`provider/`（SysDataScopeRuleProvider）、`service/` + `service/impl/`、启动类 `LrcoreSystemApplication`。

## 2. 新增 entity 必须这样写
```java
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_xxx")
@Schema(description = "xxx实体")
public class SysXxxEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "名称")
    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称长度不能超过100个字符")
    private String name;

    @Schema(description = "所属应用ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long appId;
}
```
- 继承 `com.lrcore.common.core.web.domain.BaseEntity`，固定 8 字段 id/tenantId/createUserId/updateUserId/createTime/updateTime/remark/deleted，**不要在子类重复声明**。
- 主键：BaseEntity `@Id(keyType = KeyType.Generator, value = "snowFlakeId")`（MyBatis-Flex 内置雪花生成器）；DDL 写 `id BIGINT NOT NULL`，**禁止 AUTO_INCREMENT**，不要自造 ID。
- 新表 DDL 必须含通用列（对照 `lrcore_doc.md` §2.2 与现有 DDL）：`tenant_id BIGINT NULL`（NULL=系统级数据）、`create_user_id`、`update_user_id`、`create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP`、`update_time DATETIME ... ON UPDATE CURRENT_TIMESTAMP`、`remark VARCHAR(500)`、`build_in TINYINT NULL DEFAULT 0`、`deleted TINYINT NOT NULL DEFAULT 0`。
- BaseEntity **没有 buildIn**：需要时自行加 `@Column("build_in") private Integer buildIn;`（参照 `SysProcessDefinitionBaseInfoEntity`）。
- 所有 Long 型外键（xxxId）必须加 `@JsonFormat(shape = JsonFormat.Shape.STRING)`（防前端 JS 精度丢失）。
- 枚举列用 MyBatis-Flex 枚举：枚举上标 `@EnumValue` 的 code 即存储值。参照 `com.lrcore.system.enums.DataScopeEnum`；公共枚举在 common-core（`PermimssionTypeEnum` 0=目录 1=菜单 2=按钮 3=接口、`UserStatusEnum`、`SexEnum`——`Permimssion` 拼写错误是真实类名，禁止"修正"）。
- 非数据库字段：`@Column(ignore = true)` + `transient`（参照 `SysPermissionEntity.children`）。
- 校验用 jakarta.validation（`@NotBlank`/`@Size`/`@NotNull`，参照 `SysUserEntity`）。

## 3. mapper 写法
```java
public interface SysXxxMapper extends com.mybatisflex.core.BaseMapper<SysXxxEntity> {
    // 复杂查询用 Java QueryWrapper；可放 default 方法（参照 SysDataPermissionRuleMapper）
}
```
- 不加 `@Mapper`：common-mybatisflex 的 `MyBatisFlexConfig`（@AutoConfiguration）已 `@MapperScan("com.lrcore.**.mapper")`（个别 mapper 带 @Mapper 也可，保持所在模块风格）。
- 全仓库无 MyBatis XML（无 `resources/mapper/` 目录），禁止新增 XML。
- 表定义一律引用 APT 生成类 `com.lrcore.system.domain.apt.SysXxxAPT`：静态实例为表名大写（`SysRoleAPT.SYS_ROLE`），列为大写 QueryColumn（`SYS_ROLE.ID/.TENANT_ID/.DELETED`），`DEFAULT_COLUMNS` 可选列。APT 编译期生成到 `target/generated-sources/annotations`，**禁止手写/提交 APT 类**；IDEA 看不到时重新编译该模块。

## 4. service 写法
```java
public interface ISysXxxService extends com.mybatisflex.core.service.IService<SysXxxEntity> {}

@Slf4j
@Service
@RequiredArgsConstructor
public class SysXxxServiceImpl extends ServiceImpl<SysXxxMapper, SysXxxEntity> implements ISysXxxService {
    // 直接用继承的 mapper 字段；依赖用 final 字段构造注入
}
```
- 实现类基类是 `com.mybatisflex.spring.service.impl.ServiceImpl`（不是自写基类）。
- 多步写库加 `@Transactional(rollbackFor = Exception.class)`（参照 `SysPermissionServiceImpl.deletePermissionWithChildren`）。
- 业务报错：`throw new ServiceException("xxx")`（`com.lrcore.common.core.exception.ServiceException`）。

## 5. controller + 统一响应
```java
@RestController
@RequestMapping("/sysXxx")
@RequiredArgsConstructor
@Schema(description = "xxx 控制器")
public class SysXxxController extends BaseController {
    private final ISysXxxService sysXxxService;

    @PostMapping("/save")
    public ApiResult<Boolean> save(@RequestBody SysXxxEntity e) {
        return ApiResult.success(sysXxxService.save(e));
    }
}
```
- 返回类型一律 `ApiResult<T>`（`com.lrcore.common.core.web.domain.ApiResult`，字段 code/message/errorStack/data/success/serviceDateTime）：成功 `ApiResult.success(data)`，失败 `ApiResult.fail(msg)`。**禁止自造响应包装**，禁止返回裸数据。
- 标准六端点（参照 `SysRoleController`）：`POST /save`、`PUT /update`、`DELETE /remove/{id}`、`GET /list`、`GET /getInfo/{id}`、`GET /page`（`Page<Entity>`）。
- 异常不要在 controller catch：`GlobalExceptionHandler`（lrcore-common-web）统一把 `ServiceException`、`NotPermissionException`/`NotRoleException`、`InnerAuthException`、`IdempotentException`、参数校验异常等转成 `ApiResult`。

## 6. 租户与登录用户上下文（不要自己实现）
真实链路（改其中一环前必须理解全链）：
1. lrcore-auth（SAS）签 RS256 JWT，claims 契约：`sub=user_id / user_id / username / roles / tenant_id / ent_id / dept_id`（`LrcoreTokenCustomizer`，见 `LrcoreSsoHostConfiguration`）。
2. lrcore-gateway `AuthFilter`（GlobalFilter，order -200）JWKS 验签后，把 claims 映射为下游请求头：`user_key / user_id / username / tenant_id / ent_id / dept_id`（URL 编码），并清除 `from-source`。
3. 业务服务 `HeaderInterceptor`（lrcore-common-web）把请求头写入 `SecurityContextHolder`（`com.lrcore.common.core.context.SecurityContextHolder`）。
4. 代码里一律用 `SecurityUtils`（`com.lrcore.common.core.utils.SecurityUtils`）取值：`getUserId()/getUsername()/getTenantId()/getEnterpriseId()/getDeptId()/getUserKey()/getLoginUser()/isAdmin()`。
- 禁止跨服务手传 tenantId；禁止直接读 request header 绕过 SecurityUtils。
- 插入/更新自动填充：`MyBatisFlexConfig.init()` 对 `BaseEntity` 全局注册 `MybatisInsertListener`（createUserId=当前登录用户、createTime=now）与 `MybatisUpdateListener`（updateUserId、updateTime，无登录用户名时不填）。**两个监听器都不填 tenantId**：tenantId 由业务代码显式设置（NULL=系统级数据），参照 `SysPermissionServiceImpl.updateTenantIdWithChildren`。
- **逻辑删除没有全局开启**（无 isLogicDelete 注解、无全局 logicDelete 配置）：
  - 列表/详情查询必须显式加 `.DELETED.eq(0)`（参照 `SysRoleServiceImpl`、`SysPermissionServiceImpl` 各处）。
  - 删除有两种语义：通用 controller 的 `remove()` → `removeById`（**物理删除**）；领域逻辑 → `setDeleted(1)` + `update`（软删，参照 `deletePermissionWithChildren`）。新增删除前先明确业务要哪种。

## 7. 根 lombok.config（不要删/移动）
`config.stopBubbling = true` + `lombok.copyableAnnotations += org.springframework.beans.factory.annotation.Qualifier`：使 `@RequiredArgsConstructor` + `@Qualifier("ruleManageExecutor")` 能正确注入（参照 `RuleController` 注入具名线程池）。

## 8. Feign（跨服务调用）
- 接口放提供方的 `-api` 模块（参照 `lrcore-system-api/RemoteUserApi`）：`@FeignClient(contextId = "remoteXxxApi", value = ServiceNameConstants.SYSTEM_SERVICE, fallbackFactory = XxxFallbackFactory.class)`。`value` 必须用 `ServiceNameConstants` 常量（lrcore-auth/lrcore-system/lrcore-rule…），禁止字符串字面量。
- 降级：`@Component implements FallbackFactory<XxxApi>`，log.error 后返回 `ApiResult.fail(...)`；工厂类必须登记进 `lrcore-system-api/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`。
- 提供方：一个 `@RestController` 类实现该 api 接口，**不要**类级 `@RequestMapping`（路径继承接口方法注解），参照 `SysUserFeignClient`。
- 改过 `lrcore-system-api` 源码后，消费方模块必须带 `-am` 构建或先 install 该模块（见 `lrcore-build-test` skill）。
- 内部 Feign 端点用 `@InnerAuth`（校验 `from-source: inner` 头，`InnerAuthAspect` 在 common-security）；防重提交用 `@Idempotent(prefix=..., key="#xxx.xxx")`（参照 `SysDeptController`）。

## 9. 启动类
- 业务服务用 `@LrcoreCloudApplication`（lrcore-common-annotations；= `@SpringBootApplication` + `@EnableAsync` + `@EnableAspectJAutoProxy(exposeProxy=true)` + 内置 Feign 开启，exclude/scanBasePackages 是 @SpringBootApplication 的 @AliasFor）。
- 无数据源服务（auth/gateway）写 `@LrcoreCloudApplication(exclude = {DataSourceAutoConfiguration.class})`。
- 不要手写 `@SpringBootApplication` + 一堆 @Enable*（monitor 除外，它是遗留普通启动类）。

## 10. 禁止清单
- 禁止 JPA/JdbcTemplate/MyBatis XML 写业务表；禁止主键 AUTO_INCREMENT 或自造 ID。
- 禁止新增响应包装/异常体系，绕过 GlobalExceptionHandler 手拼错误 JSON。
- 禁止查询漏 `deleted=0` 过滤；禁止硬编码 tenant_id。
- 禁止改动根 pom 的 annotationProcessorPaths 顺序（lombok 必须在 mybatis-flex-processor 前）与 resource filtering（.p12/.cer 证书文件被有意排除过滤）。
- 禁止"修正" `PermimssionTypeEnum` 等真实拼写、移动根 `lombok.config`/`mybatis-flex.config`。
