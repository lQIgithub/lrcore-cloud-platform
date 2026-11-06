# 项目规则

## 数据精度

- 后端接口返回 `Long` 类型的数据时，必须在字段上添加 `@JsonFormat(shape = JsonFormat.Shape.STRING)`，避免 JavaScript 侧精度丢失。
- 前端提交超过 16 位数字的主键 ID 时，必须以字符串形式传递，禁止转换为 `Number`。

这两个规则配合使用，防止雪花 ID 等长整型主键在 JSON 序列化/前端计算过程中丢失精度。

## 去重 / 重复处理（强制）

- 所有涉及去重、重复数据筛选的功能，必须统一使用 `lrcore-common-core` 中的 `com.lrcore.common.core.utils.DeduplicationUtil<T>`，禁止自行实现去重逻辑（手写 `HashSet` / `LinkedHashMap` 判重、循环 `contains` 判断、自定义 seen 集合等）。
- 保留 key 首次出现的元素、剔除重复项：`list.stream().filter(DeduplicationUtil.distinctByKey(User::getId)).collect(Collectors.toList())`。
- 只保留重复出现的数据：`list.stream().filter(DeduplicationUtil.distinctNotByKey(User::getId)).collect(Collectors.toList())`。
- 多字段去重使用组合 key：`DeduplicationUtil.distinctByKey(u -> u.getId() + u.getUsername())`。
- key 为 `null` 的元素不参与去重、直接保留；内部基于 `ConcurrentHashMap.putIfAbsent` 实现，线程安全，可用于 `parallelStream`。
