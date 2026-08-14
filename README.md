lrcore-cloud-platform-v2 项目理解总结
一、项目定位
这是一个 多租户 RBAC 权限管理云平台 的 Spring Cloud 微服务后端工程（com.lrcore:lrcore-cloud-platform v1.0.0）。核心业务是权限体系（用户/角色/菜单/数据权限/字段权限）+ 一个可热插拔的动态业务规则引擎。业务模块基于 MyBatis-Flex APT 代码生成 快速产出 CRUD。
二、技术栈
技术	版本/说明
Java	25
Spring Boot / Cloud	4.1.0 / 2025.1.2
Spring Cloud Alibaba	2025.1.0.0（Nacos 注册/配置中心、Sentinel 限流）
持久层	MyBatis-Flex 1.11.8（开启 APT，生成 XxxAPT 表定义类，mybatis-flex.config 中 classSuffix=APT）
缓存	Redis（token/验证码/黑名单/登录失败计数）、Caffeine（规则模块本地缓存）
认证	JJWT + Spring Security（监控模块）
其他	Lombok、Hutool、SpringDoc(swagger)、Kaptcha 验证码、Jasypt 加密、Spring Boot Admin 4.0.2、自定义 License 证书、.p12 私钥/证书


⚠️ 重要：lrcore-common-core / common-web / common-gateway / common-auth / common-rule / common-license / common-datascope / common-redis / common-annotations 等基础框架不在本仓库，通过外部 BOM lrcore-platform-tools:2.0.0 引入。

三、模块结构（Maven 多模块）
lrcore-cloud-platform (根 pom)
├── lrcore-base-modules                框架基础核心服务
│   ├── lrcore-gateway   (10801) 统一网关
│   ├── lrcore-auth      (10802) 认证授权中心
│   └── lrcore-monitor   (9100)  监控中心 (Spring Boot Admin)
└── lrcore-buzi-modules               业务服务
├── lrcore-system-modules
│   ├── lrcore-system     (10803) 系统/权限服务（171 个 Java 文件，核心）
│   └── lrcore-system-api        对外 Feign 接口 + DTO + 降级工厂
├── lrcore-bizrule-modules
│   └── lrcore-bizrule    (10804) 规则服务（JAR 热加载 + 规则执行引擎）
└── lrcore-bizrule-plugin-modules  规则插件（编译成 jar 供 bizrule 动态加载）
├── lrcore-bizrule-amount-plugin（金额校验，骨架实现）
└── lrcore-bizrule-phone-plugin （手机号校验，仅有 pom，无源码）
四、各模块职责与核心流程
1. lrcore-gateway（网关，单点入口）
   AuthFilter（order -200）：JWT 解析 + Redis 登录态校验，白名单（security.ignore.whites）放行，并把 userKey/userId/username 写入请求头。
   ValidateCodeFilter：对 /lrcore-auth/login、/lrcore-auth/register 校验图形验证码（math/char 两种）。
   BlackListUrlFilter / XssFilter：URL 黑名单、请求体 XSS 清洗（security.xss 开关）。
   SentinelFallbackHandler：网关限流降级；Sentinel 规则持久化到 Nacos（dataId sentinel-lrcore-gateway）。
   SpringDocConfig：监听 Nacos InstancesChangeEvent，自动聚合各服务 /v3/api-docs 到网关 Swagger UI。
   验证码生成走 WebFlux RouterFunction（GET /api/v1/auth/captcha）。
2. lrcore-auth（认证中心）
   POST /api/v1/auth/login：IP 黑名单 → 失败次数检查（≥5 次拦截）→ Feign 调 system 的 RemoteUserApi.getUserInfo → 密码校验（5 次错误锁定）→ 生成 JWT access + refresh token。
   refreshToken / logout / unlockscreen / genPassword / register（register/unlock/logout 目前多为注释掉的桩代码）。
   LicenseController（生成自定义 License 证书，绑定 IP/MAC/CPU/主板序列号，60 天有效期）。
   JasyptController（加密数据库连接串，输出 ENC(...) 密文）。
3. lrcore-monitor（监控中心）
   Spring Boot Admin Server + Spring Security 表单登录，上下文路径下放行 /assets、/login、/actuator、/instances。
4. lrcore-system（核心权限系统，28 张表）
   RBAC 关联：sys_user_role、sys_role_permission、sys_permission（菜单/按钮/接口三类，树形结构，ancestors 祖级路径）。
   用户登录信息：SysUserServiceImpl.getInfo() 组装用户 + 角色 + 权限 + 菜单树，并写 Redis 缓存；getByUserName() 供 auth 服务 Feign 调用。
   菜单路由：SysPermissionController /api/v1/menus/routes 返回当前用户菜单树（SysMenuInfo，供前端 vue 路由）。
   数据权限（本项目亮点）：SysDataScopeRuleProvider 实现 common-datascope 的 DataScopeRuleProvider，按租户加载行级/字段级/数据范围/自定义SQL 规则并转换为公共枚举，是数据权限在 SQL 查询中生效的关键扩展点。数据范围：全部/本企业/本部门/本部门及下级/本人/自定义；字段权限类型：可见/可编辑/只读/隐藏/加密显示。

扩展权限：权限组/角色组、权限继承/角色继承、批量授权（sys_role_permission_batch）、审计日志、权限缓存配置、权限变更历史等实体与 CRUD 已就绪（多数 Service 目前是空实现，仅继承 MyBatis-Flex ServiceImpl）。
其他：企业/部门树、字典、参数配置、应用、租户、演示表等。
5. lrcore-bizrule（动态规则引擎，独立特色）
   JAR 热加载：JarRuleLoadServiceImpl 扫描 plugin.jar.path 目录下的 jar，MD5 判断是否变更，用自定义 RuleClassLoader（URLClassLoader）加载所有实现 ValidateRule 接口的类并注册到 RuleRegistry（putIfAbsent 不覆盖旧对象）。手动接口 GET /api/v1/rule/reloadRule + 定时 8 小时自动扫描。
   规则执行：RuleFeignClient implements RemoteRuleApi，按 bizCode 从 sys_biz_rule_config 表（Caffeine 2h 缓存）查规则列表，逐条在隔离线程池 ruleRunExecutor 中执行，单条规则 500ms 超时保护，防止用户规则死循环占满 Tomcat；管理操作走 ruleManageExecutor。
   规则插件编译为独立 jar 上传到目录即生效，实现"业务上线规则验证"。
   五、数据模型要点（db/lrcore_permission_schema.sql，28 表 + 6 视图）
   统一规范：id（雪花 Long 19 位）、tenant_id、create_user_id/update_user_id、create_time/update_time、remark、build_in、deleted（逻辑删除）。分五层：租户应用层、组织架构层、RBAC 核心层、扩展权限层（数据/字段权限、继承、分组）、缓存审计层。根目录另有两份测试数据 SQL（sys_column_permission_rule_test_data.sql、sys_data_permission_rule_test_data.sql），db/data.sql 含约 78 条演示数据，menu.json 是前端菜单种子数据。
   六、我发现的问题/不一致点（供参考）
   规则服务端口占位符不匹配：lrcore-bizrule/bootstrap.yml 用 ${lrcore.rule-port}，但根 pom 定义的是 <lrcore.system-rule>10804</lrcore.system-rule>，lrcore.rule-port 不存在（且属性名与模块语义不符）。
   monitor 硬编码不一致：monitor 的 bootstrap.yml 硬编码 Nacos 10.10.10.50:8848 和另一 namespace，且使用 ${service-name-monitor}，但根 pom 未定义 service-name-monitor 属性（其他模块走 dev profile 的 localhost:8848）。
   Schema 与实体列名不一致：sys_data_permission_rule 表列是 rule_type / permission_operator / data_scope，而实体 SysDataPermissionRuleEntity 字段是 ruleTypeEnum / permOperatorEnum / dataScopeEnum，MyBatis-Flex 默认映射会去找 rule_type_enum / perm_operator_enum / data_scope_enum，测试 SQL 与实体字段无法对应（除非另加 @Column 映射）。
   RuleFeignClient 重复执行规则：executeRule 中先直接调了一次 validateRule.validate(...)，又在线程池任务里执行一次，属重复执行。
   auth 的 register/unlock/logout 基本是桩代码（业务逻辑被注释）；SysUserEntityOnSetListener 为空实现。
   插件模块尚未完成：amount 插件返回空 ruleCode/ruleName 且 validate 为空；phone 插件无任何源码。
   README 仍是 Gitee 模板占位内容，未描述真实架构。
   七、总体评价
   项目骨架完整、分层清晰（网关→认证→系统/规则），权限模型设计较全面（多租户 + 应用隔离 + RBAC + 行级/字段级数据权限 + 继承/分组 + 审计缓存），规则引擎的"插件 jar 热加载 + 隔离线程池 + 超时保护"是亮点。当前大量 Service/Controller 仍处于 MyBatis-Flex 自动生成的 CRUD 层面，核心业务逻辑集中在 SysUserServiceImpl、SysPermissionServiceImpl、SysDataScopeRuleProvider 和 RuleFeignClient。上述配置/映射不一致点建议后续优先修正。
   如果你需要，我可以进一步：输出架构图（mermaid）、生成各表/接口清单，或直接修复上面列出的一致性问题。