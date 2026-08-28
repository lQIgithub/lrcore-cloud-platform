---
name: lrcore-build-test
description: 在 lrcore-cloud-platform 仓库执行 Maven 编译/打包/跑测试/本地启动时使用：整库或单模块 mvn 命令、Java 25 要求、单模块必须 -am 的陷阱、测试分布与离线运行方式、Nacos/MySQL/Redis/Sentinel 外部依赖、各服务端口与 profile、日志位置。
whenToUse: 需要编译验证改动、打包 jar、跑单测/集成测试、回答"服务怎么启动/依赖哪些中间件/端口是多少"、或遇到符号找不到(OAuth2ClientDto)之类的单模块构建错误时。
---

# lrcore 构建与测试

以下命令均在本机（Maven 3.9.16，JDK 25.0.3）实测通过。

## 1. 工具链与版本
- Java 25（根 pom `java.version=25`；compiler source/target=25、maven-compiler-plugin 3.13.0、`<parameters>true</parameters>` 必须保留——防抖 AOP 的 SpEL 解析依赖方法参数名）。JDK 26 运行 Maven 编译本项目也实测可用；禁止 JDK < 25。
- 关键版本：Spring Boot 4.1.0、Spring Cloud 2025.1.2、Spring Cloud Alibaba 2025.1.0.0、MyBatis-Flex 1.11.8、Lombok 1.18.44、Hutool 5.8.44、spring-boot-admin 4.0.2。
- 外部依赖：全部 `lrcore-common-*` 来自外部 BOM `com.lrcore:lrcore-platform-tools-dependencies:2.0.0`（不在本仓库），本地 ~/.m2 已有；离线环境构建可行。

## 2. 编译 / 打包
```bash
# 整库
mvn clean compile -DskipTests          # 快速编译验证（实测通过）
mvn clean package -DskipTests          # 打可运行 jar（spring-boot-maven-plugin repackage）

# 单模块（-am 必带，见下节陷阱）
mvn -pl lrcore-buzi-modules/lrcore-system-modules/lrcore-system -am compile -DskipTests
```
模块路径清单（-pl 用这些）：
- `lrcore-base-modules/lrcore-gateway`
- `lrcore-base-modules/lrcore-auth`
- `lrcore-base-modules/lrcore-monitor`
- `lrcore-buzi-modules/lrcore-system-modules/lrcore-system`
- `lrcore-buzi-modules/lrcore-system-modules/lrcore-system-api`
- `lrcore-buzi-modules/lrcore-bizrule-modules/lrcore-bizrule`
- `lrcore-buzi-modules/lrcore-bizrule-plugin-modules/lrcore-bizrule-amount-plugin`

### 陷阱：单模块必须 -am（实测）
`lrcore-auth` 依赖 `lrcore-system-api`。不带 `-am` 单独构建/测试时，Maven 从本地 ~/.m2 解析 `lrcore-system-api:v1.0.0` 的**旧版 jar**，会报 `找不到符号: 类 OAuth2ClientDto`（该类只存在于仓库源码）。
- 改过 `lrcore-system-api` 后：消费方一律 `mvn -pl <module> -am ...`，或先 `mvn -pl lrcore-buzi-modules/lrcore-system-modules/lrcore-system-api install`。

### APT 注意
MyBatis-Flex APT 在编译期生成 `XxxAPT` 表定义类到 `target/generated-sources/annotations`（如 `com.lrcore.system.domain.apt.SysRoleAPT`）。根 pom `annotationProcessorPaths` 中 **lombok 必须排在 mybatis-flex-processor 前**，不要调整；APT 产物禁止提交。

## 3. 测试（数量少，但可完全离线跑）
现状（src/test 下，JUnit 5 + AssertJ + Mockito）：
- **lrcore-auth**：`SsoFlowIntegrationTest`（12 项，真实 Tomcat + H2 + 内存 OAuth2 客户端库，锁定 JWT claims 契约/登录/登出撤销）、`SocialLoginFlowTest`、`SocialLoginFlowQqTest`（内置 Mock 微信/QQ 服务器）、`SsoPortalControllerTest`。实测：`mvn -pl lrcore-base-modules/lrcore-auth -am test` → **35 tests, 0 failures, ~9s**，无需 Nacos/Redis/MySQL。
- **lrcore-gateway**：`DualTrackJwtDecoderTest`（SAS RS256 解码器行为回归）、`GatewayWhitelistBehaviorTest`（+ `TestApplication`）。实测：`mvn -pl lrcore-base-modules/lrcore-gateway test -Dtest=DualTrackJwtDecoderTest` 通过。
- **lrcore-system**：仅工作流适配 3 个单测（`FlowConversionServiceTest`、`LogicFlowToFlowableBpmnAdapterTest`、`SimpleTest`）；**权限主链路（SysUser/SysRole/SysPermission 服务）没有任何测试**。
- monitor、bizrule、各 plugin：无测试。

跑测试的通用命令：
```bash
mvn -pl <module-path> -am test                 # 该模块全部测试
mvn -pl <module-path> -am test -Dtest=ClassName # 单个测试类
```
测试为何能离线跑：各模块 `src/test/resources/bootstrap.yml` **shadow** 掉主 bootstrap（test-classes 优先），屏蔽 Nacos 配置导入与服务注册；auth 集成测试用 H2 + 内存配置 + 自起 mock 社交服务器。
新增测试时照抄 lrcore-auth 模式：测试类内嵌 `@SpringBootConfiguration + @EnableAutoConfiguration`（按需 exclude DataSource 自动装配）+ `@SpringBootTest` + WebTestClient 打真实端口断言；依赖外部资源一律用 H2/内存/mock。**不要**假设本地有 Nacos/Redis/MySQL 可用。

## 4. 本地启动（外部依赖齐全才能起）
关键事实：仓库内**没有**任何 `application-*.yml`。运行期配置全部在 Nacos：每个服务的 `bootstrap.yml` 通过 `spring.config.import` 导入两个 dataId（profile=dev，yml）：
- `application-dev.yml`（共享配置）
- `lrcore-{system|auth|gateway|rule}-dev.yml`（服务自身：datasource/redis/JWT 等；具体键名以你环境 Nacos 里为准，仓库不含，禁止凭猜测写）

dev profile（根 pom，默认激活）：`nacos.active=dev`、`nacos.namespace=aaf502bc-1e5b-42ae-b31f-6d77dde262c6`、`nacos.group=DEFAULT_GROUP`、`nacos.server-addr=10.10.1.204:8848`、`nacos.username`/`nacos.password`（已硬编码在 pom——不要再外泄；新增 profile 时禁止写入真实生产密码）。换 Nacos 环境用 `-Dnacos.server-addr=... -Dnacos.namespace=...` 覆盖。

外部依赖清单：
| 依赖 | 用途 |
|---|---|
| Nacos（8848） | 注册中心 + 配置中心（两个 dataId 必须先建好） |
| MySQL | 业务库（lrcore-system 等；auth 的 SSO JDBC 模式存 OAuth2 授权表，`lrcore.auth.jdbc.enabled` 控制） |
| Redis | 验证码（`captcha_codes:{uuid}`）、登录用户缓存（30 分钟，见 `CacheConstants`）等 |
| Sentinel 控制台（:8718） | gateway bootstrap 指向 `${nacos.server-addr}:8718`；gw-flow 规则 dataId `sentinel-lrcore-gateway` |

端口：gateway **10801**、auth **10802**、system **10803**、bizrule **10804**、monitor **9100**。
启动顺序：Nacos（建好 dataId）→ MySQL/Redis → gateway → auth → system（bizrule 按需）。

运行方式：
- IDEA：直接跑启动类（`LrcoreSystemApplication`、`LrcoreAuthApplication` 等，均 `@LrcoreCloudApplication`）。
- 命令行：`mvn -pl <module> -am package -DskipTests` 后 `java -jar <module>/target/<finalName>.jar`。finalName 规则：system 是 `lrcore-system-dev`（`${service-name-system}-${nacos.active}`），auth 是 `lrcore-auth`。
- bizrule 特有：Nacos 配置键 `plugin.jar.path` 指向插件 jar 目录；热加载任务提交 `ruleManageExecutor` 线程池（禁止跑在 scheduler/Tomcat 线程）；自动扫描 cron `0 * */8 * * ?`；手动接口 `GET /api/v1/rule/reloadRule`。

### 已知问题
- **lrcore-monitor 当前起不来**：其 `bootstrap.yml` 硬编码了另一套 Nacos（`10.10.10.50:8848`），且 `spring.application.name` 引用 `${service-name-monitor}`——该属性在根 pom **未定义**。按遗留模块处理，不要假设它可用。
- 根 pom 的 `service-name-flowable`/`lrcore-flowable` 常量存在但仓库无该模块（Flowable 代码在 lrcore-system 内，经 `lrcore-common-flowable` 依赖）。

## 5. 日志
各服务 `logback.xml`：工作目录下 `logs/{服务名}/info.log` + `error.log`（如 `logs/lrcore-system/`），按天滚动、保留 60 天。排障先查这里。

## 6. 禁止清单
- 禁止不带 `-am` 单独构建依赖了 `lrcore-system-api` 的模块。
- 禁止改根 pom 的 `<parameters>true</parameters>`、annotationProcessorPaths 顺序、resource filtering（`.p12`/`.cer` 被有意排除过滤，是 License 证书）。
- 禁止把真实密码/生产配置写进仓库（Nacos 上的配置不要拷进 yml 提交）。
- 禁止提交 `target/`、`logs/`（已在 .gitignore）。
