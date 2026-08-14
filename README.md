git提交规范
常用 type： feat （新功能）、 fix （修复）、 refactor （重构）、 chore （构建/工具）、 docs （文档）。本次属于重构，推荐 refactor 。


<div align="center">

<img alt="vue3-element-admin" width="80" src="./src/assets/images/logo.png">

# lrcore-cloud

**SpringCloudAlibaba + Springboot + Mybatis-flex 企业级后台管理系统**

[![spring-cloud-alibaba](https://spring.io/)](https://spring.io/projects/spring-cloud-alibaba)
[![spring-boot](https://spring.io/)](https://spring.io/projects/spring-boot)
[![mybatis-flex]()](https://mybatis-flex.com/)
[![GitHub Star](https://img.shields.io/github/stars/youlaitech/vue3-element-admin?style=social)](https://github.com/lQIgithub/lrcore-cloud-platform.git)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
</div>


![](https://foruda.gitee.com/images/1708618984641188532/a7cca095_716974.png "rainbow.png")

<div align="center">
  <a target="_blank" href="">🖥️ 在线预览</a> | <a target="_blank" href="">📲 移动端预览</a> |  <a target="_blank" href="">📑 阅读文档</a>|  <a target="_blank" href="">🌐 官网</a> 
</div>

## 项目特色
- **管理云平台** 基于Spring Cloud Alibaba 微服务后端工程。核心业务是权限体系（用户/角色/菜单/数据权限/字段权限）+ 一个可热插拔的动态业务规则引擎。业务模块基于 MyBatis-Flex APT 代码生成 快速产出 CRUD。
- **简洁易用**： 基于lrcore-platform-tools:2.0.0独立封装的架构，PC应用开发便捷。
- **数据交互**： 支持 `Mock` 数据，并提供配套的 [Java](https://github.com/lQIgithub/lrcore-cloud-platform.git)后端源码。
- **系统功能：** 提供用户管理、角色管理、菜单管理、部门管理、字典管理、系统配置、通知公告、工作流等功能模块。
- **权限管理：** 支持后端动态配置路由、按钮权限、角色权限和数据权限等多种权限管理方式。
- **多租户：** 支持多租户模式与租户隔离。
- **基础设施：** 提供国际化、多布局、暗黑模式、全屏、水印、接口文档和代码生成器等功能。
- **持续更新**：项目持续开源更新，实时更新工具和依赖。

## 技术栈
| 技术栈               | 版本       | 说明                                                         |
| -------------------- | :--------- | ------------------------------------------------------------ |
| Java                 | 25         |                                                              |
| Spring Cloud Alibaba | 2025.1.0.0 | Nacos 注册/配置中心、Sentinel 限流                           |
| Spring Cloud         | 2025.1.2   |                                                              |
| Spring Boot          | 4.1.0      |                                                              |
| MyBatis-Flex         | 1.11.8     | 开启 APT，生成 XxxAPT 表定义类，mybatis-flex.config 中 classSuffix=APT |
| Redis                | 7          | token/验证码/黑名单/登录失败计数、Caffeine（规则模块本地缓存） |
|                      |            | JJWT + Spring Security                                       |
|                      |            | Lombok、Hutool、SpringDoc(swagger)、Kaptcha 验证码、Jasypt 加密、 自定义 License 证书、.p12 私钥/证书 |

## 重点依赖


 ~~~
 ⚠️ 重要：lrcore-common-core / common-web / common-gateway / common-auth / common-rule / common-license / common-datascope / common-redis / common-annotations 等基础框架不在本仓库，通过外部 BOM lrcore-platform-tools:2.0.0 引入。
 ~~~

## 模块结构（Maven 多模块）
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

## 生态矩阵

**前端**

| 项目 | 技术栈                                        | 说明 |
|:-----|:-------------------------------------------|:-----|
| [vue3-element-admin](https://github.com/lQIgithub/lrcore-cloud-admin-ui) | Vue 3 + Vite + TS + + Pinia + Element Plus | PC 管理前端（主推） |

**后端**

| 项目                                                      | 技术栈                        | 说明 |
|:--------------------------------------------------------|:---------------------------|:-----|
| [lrcore-cloud](https://github.com/lQIgithub/lrcore-cloud-platform) | Spring Cloud Alibaba + Spring Boot + MyBatis-Flex | Java 后端（主推） |


## 开发指南

| 名称     | 地址 |
| -------- | ---- |
| 视频教程 |      |
| 项目搭建 |      |
| 官方     |      |
| 代码规范 |      |
| 提交规范 |      |
| 接口文档 |      |


## 项目启动

```bash
# 克隆代码
git clone https://github.com/lQIgithub/lrcore-cloud-platform.git

# 使用Intellij Idea 打开项目
cd lrcore-cloud-platform

# Maven命令执行打包运行
mvn clean package
```

## 联系方式
添加时， 请备注来源
<table align="center">
  <tr>
    <td align="center">
      <img src="./public/images/wechat-personal.png" height="180" alt="添加作者微信"><br>
      <sub>作者微信 或 15085945045</sub>
    </td>
  </tr>
</table>
<p align="center"><em>技术交流 · 问题反馈 · 商务合作</em></p>
