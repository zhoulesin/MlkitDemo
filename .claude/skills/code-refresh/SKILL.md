---
name: code-refresh
description: 每执行10次任务后自动回顾代码，寻找重构空间和优化机会
---

# 代码重构与定期回顾指南

## 触发时机

每完成约 10 个任务/功能点后，强制进行一次代码回顾。

## 重构检查清单

### 1. 单一职责检查

- [ ] 每个类是否只做一件事？
- [ ] MainActivity 是否承担了太多职责？
- [ ] 是否有超过 200 行的类？

**重构方向**：
- 拆分类，按功能模块划分包
- 示例：`mlkit/`、`network/`、`chat/` 等独立包

### 2. 代码重复检查

- [ ] 是否有多处相同/相似的逻辑？
- [ ] 多个 Helpers 中是否有重复的工具方法？
- [ ] 是否有重复的网络请求/重试逻辑？

**重构方向**：
- 抽取公共方法到工具类
- 示例：`HttpClient`、`RetryHelper`、`DateUtils` 等

### 3. 可复用性检查

- [ ] 当前实现是否方便扩展新功能？
- [ ] 是否可以添加新的 ML Kit 功能而不修改现有代码？
- [ ] 网络层是否可以复用到其他 API？

**重构方向**：
- 定义接口，面向接口编程
- 分离可变与不变部分

### 4. 命名与结构检查

- [ ] 包名是否清晰表达职责？
- [ ] 类名/方法名是否自解释？
- [ ] 常量是否放在合适位置（companion object/Consts）？

## 项目当前架构建议

```
app/
├── mlkit/                    # ML Kit 相关
│   ├── TextRecognitionHelper.kt
│   ├── ImageLabelingHelper.kt
│   ├── FaceDetectionHelper.kt
│   └── GalleryHelper.kt
├── network/                  # 网络层（通用）
│   ├── HttpClient.kt
│   └── RetryHelper.kt
├── chat/                     # 聊天功能
│   ├── ChatManager.kt
│   └── VolcanoApiHelper.kt
└── MainActivity.kt           # 仅 UI 和事件绑定
```

## 重构触发示例

### 示例 1：VolcanoApiHelper 臃肿 → 拆分前

**问题**：
- 包含 OkHttp 配置
- 包含重试逻辑
- 包含 JSON 解析
- 包含请求构建

**重构后**：
- OkHttp 配置 → `HttpClient`
- 重试逻辑 → `RetryHelper`
- VolcanoApiHelper → 仅 API 调用和解析

### 示例 2：MainActivity 臃肿 → 拆分前

**问题**：
- 包含 3 个 ML Kit 识别逻辑
- 包含相册选择
- 包含聊天逻辑

**重构后**：
- ML Kit → `mlkit/` 包下独立 Helpers
- 相册 → `GalleryHelper`
- 聊天 → `chat/` 包

## 快速操作指南

当触发回顾时，按以下步骤操作：

1. **先看大文件**：找出 > 150 行的类
2. **找重复代码**：搜索相似代码块
3. **按功能分包**：创建新包结构
4. **逐步迁移**：每次只移动一个功能
5. **确保编译通过**：每步都验证

## 检查频率计数器

| 功能点 | 计数 |
|--------|------|
| Day 1-4 基础功能 | 4 |
| Day 5-7 聊天功能 | 3 |
| 代码重构拆分 | 1 |
| 网络超时修复 | 1 |
| **当前总计** | **9** |

⚠️ **接近 10 次，建议进行一次全面回顾！**
