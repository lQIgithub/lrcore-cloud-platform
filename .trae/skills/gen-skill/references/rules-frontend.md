# LRCore Cloud Platform - 前端开发规范

## 1. 规则概述

### 1.1 文档目的

本文档定义了 LRCore Cloud Platform 项目前端开发的技术栈、代码风格和开发流程规范。

### 1.2 适用范围

适用于所有前端开发人员。

### 1.3 执行原则

- **严格遵守**：所有前端开发活动必须严格遵守本规则
- **一致性**：保持代码风格和结构的一致性
- **可维护性**：编写易于维护的代码

---

## 2. 技术栈规范

| 分类 | 技术 | 版本 | 说明 |
|-----|------|------|------|
| 框架 | Vue | ^3.5.13 | 前端框架 |
| 语言 | TypeScript | ~5.6.2 | 类型安全 |
| 构建工具 | Vite | ^6.0.5 | 构建工具 |
| UI组件 | Element Plus | ^2.8.4 | UI组件库 |
| 状态管理 | Pinia | ^2.3.0 | 状态管理 |
| 路由 | Vue Router | ^4.4.5 | 路由管理 |
| HTTP客户端 | Axios | ^1.7.9 | HTTP请求 |
| 代码规范 | ESLint | ^9.13.0 | 代码检查 |
| 格式化 | Prettier | ^3.3.3 | 代码格式化 |

**参考**: [rules-version.md](./rules-version.md) - 版本依赖保护规则

---

## 3. 目录结构规范

```
lrcore-frontend/
├── src/
│   ├── api/         # API 调用
│   ├── components/  # 公共组件
│   ├── mock/        # 模拟数据
│   ├── router/      # 路由配置
│   ├── store/       # Pinia 状态管理
│   ├── styles/      # 样式文件
│   ├── utils/       # 工具类
│   ├── views/       # 页面组件
│   ├── App.vue      # 根组件
│   └── main.ts      # 入口文件
├── public/          # 静态资源
├── .env.*           # 环境配置
├── package.json     # 依赖配置
├── tsconfig.json    # TypeScript 配置
└── vite.config.ts   # Vite 配置
```

### 3.1 目录职责说明

| 目录 | 职责 | 说明 |
|-----|------|------|
| `api/` | API调用 | 封装后端API调用 |
| `components/` | 公共组件 | 可复用的UI组件 |
| `mock/` | 模拟数据 | 开发环境模拟数据 |
| `router/` | 路由配置 | 页面路由定义 |
| `store/` | 状态管理 | Pinia store |
| `styles/` | 样式文件 | 全局样式和变量 |
| `utils/` | 工具类 | 通用工具方法 |
| `views/` | 页面组件 | 页面级组件 |

---

## 4. 代码风格规范

### 4.1 命名规范

| 类型 | 命名规则 | 示例 |
|-----|---------|------|
| 组件名 | 大驼峰 | `UserList.vue` |
| 变量名 | 小驼峰 | `userList` |
| 常量名 | 全大写 | `MAX_PAGE_SIZE` |
| 方法名 | 小驼峰 | `getUserList` |
| 文件命名 | 小写连字符 | `user-list.vue` |

### 4.2 代码格式

- 使用 2 个空格缩进
- 字符串使用单引号
- 末尾不加分号
- 大括号使用 K&R 风格

### 4.3 Vue 组件规范

```vue
<script setup lang="ts">
// 使用 <script setup> 语法
import { ref, computed } from 'vue'

// 组件属性使用 TypeScript 类型定义
defineProps<{
  title: string
  count?: number
}>()

// 响应式数据
const visible = ref(false)
const items = ref<string[]>([])

// 计算属性
const total = computed(() => items.value.length)

// 方法
const handleClick = () => {
  visible.value = true
}
</script>

<template>
  <div class="component-name">
    <h2>{{ title }}</h2>
    <p>Count: {{ count }}</p>
    <button @click="handleClick">Click</button>
  </div>
</template>

<style scoped>
.component-name {
  padding: 16px;
}
</style>
```

### 4.4 组合式 API 规范

- 使用 `ref` 定义基本类型响应式数据
- 使用 `reactive` 定义对象类型响应式数据
- 使用 `computed` 定义计算属性
- 使用生命周期钩子时遵循组合式 API 命名（`onMounted`, `onUpdated` 等）

---

## 5. 开发流程规范

### 5.1 环境配置

| 环境 | 配置文件 | 说明 |
|-----|---------|------|
| 开发环境 | `.env.development` | 开发环境配置 |
| 生产环境 | `.env.production` | 生产环境配置 |

### 5.2 构建流程

| 命令 | 说明 |
|-----|------|
| `npm run dev` | 启动开发服务器 |
| `npm run build` | 构建生产版本 |
| `npm run preview` | 预览构建结果 |
| `npm run lint` | 代码检查 |

### 5.3 代码提交规范

- 使用 Husky 进行提交前检查
- 使用 lint-staged 进行暂存文件检查
- 提交信息格式：`feat: 添加新功能`

---

## 6. 代码审查规范

### 6.1 审查标准

- **代码可读性**：变量命名清晰，注释充分
- **代码可维护性**：模块化设计，职责分离
- **代码安全性**：无安全漏洞，无敏感信息泄露
- **代码性能**：无性能瓶颈，资源使用合理

### 6.2 审查工具

| 工具 | 用途 |
|-----|------|
| ESLint | 代码质量检查 |
| Prettier | 代码格式化 |
| TypeScript | 类型检查 |

---

## 7. 交叉引用

- [返回索引](./rules-index.md)
- [版本依赖保护规则](./rules-version.md) - 前端依赖版本管理
- [后端开发规范](./rules-backend.md) - API 接口规范

---

## 版本历史

- v1.0 (2026-04-29)：初始版本，从综合规则文件拆分