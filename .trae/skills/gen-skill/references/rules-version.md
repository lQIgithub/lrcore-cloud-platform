# LRCore Cloud Platform - 版本依赖保护规则

## 1. 规则概述

### 1.1 文档目的

本文档定义了 LRCore Cloud Platform 项目的版本依赖保护规则，旨在确保项目依赖版本的稳定性和一致性，防止未经授权的依赖修改。

### 1.2 适用范围

适用于所有参与 LRCore Cloud Platform 项目开发的开发人员、测试人员和运维人员。

### 1.3 执行原则

- **严格遵守**：所有开发活动必须严格遵守本规则
- **版本固定**：依赖版本号必须保持固定，不允许自动修改
- **责任明确**：每个开发人员对自己的代码质量负责

---

## 2. 后端依赖保护

| 配置项                                | 当前版本       | 位置          |
|------------------------------------|------------|-------------|
| project.reporting.outputEncoding   | UTF-8      | pom.xml#L38 |
| java.version                       | 25         | pom.xml#L39 |
| spring-boot.version                | 4.1.0      | pom.xml#L40 |
| spring-cloud.version               | 2025.1.2   | pom.xml#L41 |
| spring-cloud-alibaba.version       | 2025.1.0.0 | pom.xml#L42 |
| spring-boot-admin.version          | 4.0.2      | pom.xml#L43 |
| kaptcha.version                    | 2.3.3      | pom.xml#L44 |
| pagehelper.boot.version            | 2.1.0      | pom.xml#L45 |
| druid.version                      | 1.2.28     | pom.xml#L46 |
| dynamic-ds.version                 | 4.5.0      | pom.xml#L47 |
| commons.io.version                 | 2.21.0     | pom.xml#L48 |
| velocity.version                   | 2.3        | pom.xml#L49 |
| fastjson.version                   | 2.0.61     | pom.xml#L50 |
| jjwt.version                       | 0.9.1      | pom.xml#L51 |
| minio.version                      | 8.2.2      | pom.xml#L52 |
| poi.version                        | 4.1.2      | pom.xml#L53 |
| springdoc.version                  | 3.0.2      | pom.xml#L54 |
| transmittable-thread-local.version | 2.14.5     | pom.xml#L55 |
| lombok.version                     | 1.18.38    | pom.xml#L57 |
| hutool.version                     | 5.8.22     | pom.xml#L58 |
| aliyun.sms.version                 | 2.0.24     | pom.xml#L61 |
| aliyun.sms.api.version             | 0.3.2      | pom.xml#L62 |
| aliyun.sms.util.version            | 0.2.21     | pom.xml#L63 |
| mybatis-flex.version               | 1.11.8     | pom.xml#L66 |

---

## 3. 前端依赖保护

| 依赖                               | 当前版本     | 位置                           |
|----------------------------------|----------|------------------------------|
| axios                            | ^1.7.9   | lrcore-frontend/package.json |
| element-plus                     | ^2.8.4   | lrcore-frontend/package.json |
| pinia                            | ^2.3.0   | lrcore-frontend/package.json |
| pinia-plugin-persistedstate      | ^3.2.1   | lrcore-frontend/package.json |
| vue                              | ^3.5.13  | lrcore-frontend/package.json |
| vue-router                       | ^4.4.5   | lrcore-frontend/package.json |
| @typescript-eslint/eslint-plugin | ^8.10.0  | lrcore-frontend/package.json |
| @typescript-eslint/parser        | ^8.10.0  | lrcore-frontend/package.json |
| @vitejs/plugin-vue               | ^5.2.1   | lrcore-frontend/package.json |
| eslint                           | ^9.13.0  | lrcore-frontend/package.json |
| eslint-config-prettier           | ^9.1.0   | lrcore-frontend/package.json |
| eslint-plugin-prettier           | ^5.2.1   | lrcore-frontend/package.json |
| eslint-plugin-vue                | ^9.30.0  | lrcore-frontend/package.json |
| husky                            | ^9.1.6   | lrcore-frontend/package.json |
| lint-staged                      | ^15.2.10 | lrcore-frontend/package.json |
| prettier                         | ^3.3.3   | lrcore-frontend/package.json |
| sass                             | ^1.98.0  | lrcore-frontend/package.json |
| terser                           | ^5.46.1  | lrcore-frontend/package.json |
| typescript                       | ~5.6.2   | lrcore-frontend/package.json |
| vite                             | ^6.0.5   | lrcore-frontend/package.json |
| vue-tsc                          | ^2.1.10  | lrcore-frontend/package.json |

---

## 4. 版本管理规则

### 4.1 禁止修改

任何开发人员不得未经授权修改上述依赖版本。

### 4.2 版本升级条件

只有在以下情况可以升级版本：
- 存在严重安全漏洞
- 功能需求必须使用新版本
- 性能问题需要新版本解决

### 4.3 升级流程

1. 提出升级申请，说明理由
2. 进行充分测试验证
3. 获得项目负责人批准
4. 记录版本变更日志

### 4.4 依赖冲突处理

如遇依赖冲突，优先使用现有版本，通过排除依赖方式解决。

### 4.5 版本固定规则

父级pom.xml文件中已配置的所有依赖包版本号必须保持固定，在任何情况下（包括但不限于开发阶段、测试验证、构建过程及自动化流程）均不允许进行自动修改或更新操作。此规则适用于所有团队成员及自动化工具，确保依赖版本的稳定性与一致性。

---

## 5. 交叉引用

- [返回索引](./rules-index.md)
- [后端开发规范](./rules-backend.md) - 包含模块创建规范，需参照依赖版本
- [前端开发规范](./rules-frontend.md) - 包含前端技术栈规范

---

## 版本历史

- v1.0 (2026-04-29)：初始版本，从综合规则文件拆分