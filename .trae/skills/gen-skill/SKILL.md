---
name: "gen-skill"
description: "LRCore Cloud Platform development rules toolkit. Invoke when need to access backend/frontend development standards, SQL schemas, security guidelines, or deployment procedures."
---

# Gen-Skill - LRCore Cloud Platform Rules Toolkit

## 技能概述

Gen-Skill 是 LRCore Cloud Platform 项目的规则工具包，封装了所有开发规范和最佳实践。该工具包提供模块化的规则访问接口，支持按需加载各类开发规范，确保团队开发的一致性和代码质量。

## 核心功能说明

### 1. 规则模块

本工具包包含以下核心规则模块：

| 模块名称 | 文件 | 功能描述 |
|---------|------|---------|
| **版本依赖保护规则** | `rules-version.md` | 定义前后端依赖版本管理规则，确保项目依赖的稳定性和一致性 |
| **后端开发规范** | `rules-backend.md` | 定义后端服务开发的完整规范，包括模块结构、代码风格、配置规范等 |
| **前端开发规范** | `rules-frontend.md` | 定义前端项目开发的技术栈、代码风格和开发流程 |
| **部署与运维规范** | `rules-deployment.md` | 定义服务部署流程、环境配置、监控维护和终端使用管理规则 |
| **安全规范** | `rules-security.md` | 定义认证授权、数据安全、接口安全等安全相关规则 |
| **文档规范** | `rules-document.md` | 定义技术文档和代码注释的编写规范 |
| **SQL Schema 规范** | `rules-sql.md` | 定义数据库 Schema 文件的生成规范，明确主键 ID 使用 varchar(36) 数据类型 |

### 2. 模块化特性

- **统一入口**：提供统一的 API 接口访问所有规则
- **按需加载**：支持按模块加载，优化性能
- **类型安全**：TypeScript 类型定义，提供完整的类型支持
- **版本管理**：遵循语义化版本规范，支持版本追踪

### 3. 适用范围

- 后端开发人员
- 前端开发人员
- 运维人员
- 数据库设计人员
- 测试人员

## 安装指南

### NPM 安装

```bash
# 全局安装
npm install -g gen-skill

# 局部安装
npm install --save-dev gen-skill
```

### Yarn 安装

```bash
# 全局安装
yarn global add gen-skill

# 局部安装
yarn add --dev gen-skill
```

### 从源码安装

```bash
# 克隆仓库
git clone <repository-url>

# 进入目录
cd gen-skill

# 安装依赖
npm install

# 构建
npm run build
```

## 使用示例

### 1. 基本使用

```javascript
// ES6 模块导入
import { RuleManager } from 'gen-skill';

// CommonJS 导入
const { RuleManager } = require('gen-skill');

// 创建规则管理器实例
const manager = new RuleManager();

// 获取所有规则
const allRules = manager.getAllRules();

// 获取特定规则
const backendRules = manager.getRule('backend');
```

### 2. 按需加载

```javascript
import { loadRule } from 'gen-skill';

// 只加载后端规则
const backendRule = loadRule('backend');

// 只加载 SQL 规则
const sqlRule = loadRule('sql');
```

### 3. 规则验证

```javascript
import { validateRule } from 'gen-skill';

// 验证代码是否符合规则
const result = validateRule('backend', codeContent);

if (result.valid) {
  console.log('代码符合规范');
} else {
  console.log('违规项：', result.violations);
}
```

### 4. 获取规则文档

```javascript
import { getRuleDocument } from 'gen-skill';

// 获取规则的 Markdown 文档
const doc = getRuleDocument('backend');
console.log(doc);
```

## API 参考

### RuleManager 类

#### 构造函数

```typescript
constructor(options?: RuleManagerOptions)
```

**参数**：
- `options.basePath`：规则文件基础路径（默认：`./references`）
- `options.cache`：是否启用缓存（默认：`true`）

#### 方法

##### getAllRules()

获取所有规则内容。

```typescript
getAllRules(): Map<string, RuleContent>
```

**返回值**：Map 对象，键为规则名称，值为规则内容

##### getRule(name)

获取指定规则内容。

```typescript
getRule(name: string): RuleContent | null
```

**参数**：
- `name`：规则名称（如：'backend', 'frontend', 'sql' 等）

**返回值**：规则内容对象，不存在则返回 null

##### validateRule(name, content)

验证内容是否符合指定规则。

```typescript
validateRule(name: string, content: string): ValidationResult
```

**参数**：
- `name`：规则名称
- `content`：待验证的内容

**返回值**：验证结果对象

### 辅助函数

#### loadRule(name)

按需加载指定规则。

```typescript
loadRule(name: string): Promise<RuleContent>
```

#### getRuleDocument(name)

获取规则的 Markdown 文档。

```typescript
getRuleDocument(name: string): string
```

#### listRules()

列出所有可用规则。

```typescript
listRules(): string[]
```

### 类型定义

```typescript
interface RuleContent {
  name: string;
  version: string;
  description: string;
  content: string;
  metadata: RuleMetadata;
}

interface RuleMetadata {
  author: string;
  createdAt: string;
  updatedAt: string;
  tags: string[];
}

interface ValidationResult {
  valid: boolean;
  violations: Violation[];
}

interface Violation {
  rule: string;
  message: string;
  line?: number;
  column?: number;
}
```

## 常见问题解答

### Q1: 如何更新规则到最新版本？

```bash
npm update gen-skill
```

### Q2: 如何自定义规则路径？

```javascript
const manager = new RuleManager({
  basePath: '/custom/path/to/rules'
});
```

### Q3: 如何扩展自定义规则？

```javascript
import { RuleManager } from 'gen-skill';

const manager = new RuleManager();
manager.addCustomRule('my-rule', customRuleContent);
```

### Q4: 规则文件支持哪些格式？

目前支持 Markdown (.md) 格式的规则文件。

### Q5: 如何在 CI/CD 中使用？

```yaml
# GitHub Actions 示例
- name: Validate Code
  run: |
    npm install gen-skill
    node scripts/validate-rules.js
```

## 目录结构

```
gen-skill/
├── SKILL.md                 # 技能说明文档（本文件）
├── package.json             # 包配置文件
├── references/              # 规则文件目录
│   ├── rules-backend.md     # 后端开发规范
│   ├── rules-deployment.md  # 部署与运维规范
│   ├── rules-document.md    # 文档规范
│   ├── rules-frontend.md    # 前端开发规范
│   ├── rules-index.md       # 规则索引
│   ├── rules-security.md    # 安全规范
│   ├── rules-sql.md         # SQL Schema 规范
│   └── rules-version.md     # 版本依赖保护规则
├── src/                     # 源代码目录
│   ├── index.ts            # 主入口文件
│   ├── RuleManager.ts      # 规则管理器
│   ├── loader.ts           # 规则加载器
│   ├── validator.ts        # 规则验证器
│   └── types.ts            # 类型定义
├── dist/                    # 构建输出目录
├── scripts/                 # 脚本工具目录
│   ├── build.js            # 构建脚本
│   └── validate.js         # 验证脚本
├── test/                    # 测试目录
│   ├── unit/               # 单元测试
│   └── integration/        # 集成测试
├── assets/                  # 资源文件目录
├── .eslintrc.js            # ESLint 配置
├── .prettierrc             # Prettier 配置
├── tsconfig.json           # TypeScript 配置
├── .gitignore              # Git 忽略文件
└── README.md               # 项目说明文档
```

## 版本历史

### v1.0.0 (2026-05-29)

**初始版本发布**

- ✅ 完整提取 `.trae/rules` 文件夹中的所有规则文件
- ✅ 实现模块化封装，提供统一 API 接口
- ✅ 支持按需加载机制
- ✅ 提供完整的 TypeScript 类型定义
- ✅ 实现规则验证功能
- ✅ 提供详细的文档和使用示例
- ✅ 支持 NPM/Yarn 安装
- ✅ 提供单元测试和集成测试

### 变更日志

#### [1.0.0] - 2026-05-29

**新增功能**：
- 规则管理器（RuleManager）核心类
- 规则加载器（loader）模块
- 规则验证器（validator）模块
- 按需加载功能
- TypeScript 类型定义
- 单元测试和集成测试
- ESLint 和 Prettier 配置
- 详细的 API 文档

**包含规则**：
- rules-version.md v1.0
- rules-backend.md v1.2
- rules-frontend.md v1.0
- rules-deployment.md v1.0
- rules-security.md v1.0
- rules-document.md v1.0
- rules-sql.md v1.3

## 许可证

MIT License

## 贡献指南

欢迎提交 Issue 和 Pull Request。在提交代码前，请确保：

1. 代码通过所有测试：`npm test`
2. 代码符合 ESLint 规范：`npm run lint`
3. 代码格式符合 Prettier 规范：`npm run format`

## 联系方式

- 项目地址：LRCore Cloud Platform
- 维护团队：lrcore

---

**文档状态**: 活跃维护中  
**适用项目**: LRCore Cloud Platform
