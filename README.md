# lrcore-cloud-platform
多租户 RBAC 权限管理云平台，Spring Cloud 微服务架构。Java 25 + Spring Boot 4.1.0 + Cloud Alibaba 2025.1.0.0，MyBatis-Flex APT 持久层，Redis+Caffeine 缓存，JJWT 认证。  模块结构 ：网关（JWT/验证码/限流）→ 认证中心（登录/令牌/密码）→ 系统服务（28 表 RBAC + 数据/字段权限扩展）→ 规则服务（JAR 热加载 + 隔离线程池 500ms 超时保护）+ 监控中心。  特色 ：数据权限支持行级/字段级/自定义 SQL 规则；规则引擎插件式热加载；网关自动聚合 Swagger 文档。  待优化 ：配置属性名不匹配、实体与表结构映射不一致、规则引擎重复执行
