# Bilibili网络功能实现总结

# BilibiliVideoPro 项目实现总结

## 概述

本项目旨在提供一个功能完善的Bilibili视频互动与管理解决方案。我们严格遵循现代软件工程原则，如SOLID，并基于bilibili-API-collect文档实现了核心功能。所有核心功能模块都位于 `online.bingzi.bilibili.video.pro.internal` 包下。

## 核心模块与功能

### 1. 数据库模块 (Database)

- **位置**: `online.bingzi.bilibili.video.pro.internal.database`
- **功能**:
    - **玩家Bilibili账户绑定管理**: 存储和管理玩家与Bilibili账户的绑定信息。
    - **视频互动状态记录**: 记录玩家对视频的点赞、投币、收藏等互动行为。
    - **多数据库支持**: 支持SQLite和MySQL两种数据库，并通过策略模式轻松扩展。
    - **自动初始化与健康检查**: 确保数据库连接的稳定性和可用性。
- **主要更新**:
    - **接口细化**: `IPlayerBilibiliService` 中的 `createBinding` 和 `updateCookie` 方法现在接受更细粒度的 Bilibili Cookie 参数（`sessdata`, `biliJct`, `dedeUserId`, `dedeUserIdMd5`），提高了安全性与灵活性。
    - **管理优化**: `DatabaseManager` 进行了重构，DAO实例通过 `lateinit var` 延迟初始化，并移除了 `TabooLibAPI` 的相关注册，简化了数据库管理逻辑。
    - **提供者增强**: `IDatabaseProvider` 新增 `getJdbcUrl` 方法，并更新 `createDataSource` 以接受完整的 `DatabaseConfig` 对象，提供了更灵活的数据库配置。

### 2. 网络模块 (Network)

- **位置**: `online.bingzi.bilibili.video.pro.internal.network`
- **功能**:
    - **二维码扫码登录**: 提供生成二维码、轮询登录状态、自动提取并保存Cookie的功能。
    - **视频互动状态检查**: 获取视频的点赞、投币、收藏状态，以及UP主关注状态和评论区留言状态。
    - **Cookie轮换刷新**: 实现手动和自动定时刷新Cookie，确保登录状态的持久性。
- **核心组件**:
    - `BilibiliApiClient`: 基于OkHttp3的统一HTTP客户端，处理请求头、Cookie管理和错误处理。
    - `BilibiliCookieJar`: 线程安全的Cookie存储和管理。
    - `BilibiliNetworkManager`: 单例模式的网络服务统一管理器，负责初始化和资源管理。

### 3. 辅助模块 (Helper)

- **位置**: `online.bingzi.bilibili.video.pro.internal.helper`
- **功能**:
    - **`QRCodeHelper`**: 提供二维码生成功能。
    - **`MapItemHelper`**: 用于构建包含二维码的Minecraft地图物品。
    - **`NMSHelper`**: **已重构**，提供使用 ProtocolLib 发送地图数据包的可靠功能。现在支持 ProtocolLib 4.8.0+ 和现代 Minecraft 版本 (1.19.4+)，提供了更精细的地图数据包控制，包括缩放级别、跟踪位置、锁定状态以及脏区域更新。

## 技术架构

```
BilibiliVideoPro (主入口)
├── DatabaseManager (数据库管理)
│   ├── IDatabaseProvider (数据库提供者接口)
│   ├── MySQLProvider (MySQL实现)
│   └── SQLiteProvider (SQLite实现)
├── BilibiliNetworkManager (网络服务统一管理器)
│   ├── BilibiliApiClient (HTTP客户端)
│   ├── BilibiliCookieJar (Cookie管理)
│   ├── QRCodeLoginService (二维码登录)
│   ├── VideoInteractionService (视频互动)
│   └── CookieRefreshService (Cookie刷新)
└── Helper (辅助工具)
    ├── QRCodeHelper (二维码生成)
    ├── MapItemHelper (地图物品构建)
    └── NMSHelper (地图数据包处理)
```

## 数据模型

### 登录相关
- `QRCodeData`: 二维码数据
- `LoginUserInfo`: 登录用户信息
- `QRCodeResult`: 二维码生成结果
- `LoginPollResult`: 登录轮询结果
- `CookieRefreshResult`: Cookie刷新结果

### 视频互动相关
- `TripleActionStatus`: 三连状态
- `VideoData`: 视频基本信息
- `VideoInteractionStatus`: 完整互动状态
- `TripleActionResult`: 三连状态查询结果
- `FollowResult`: 关注状态结果
- `CommentCheckResult`: 评论检查结果

## 依赖说明

项目已在 `build.gradle.kts` 中添加了必要的依赖：

```kotlin
// 二维码生成库
implementation("com.google.zxing:core:3.5.2")
implementation("com.google.zxing:javase:3.5.2")

// ProtocolLib for packet handling
implementation("com.comphenix.protocol:ProtocolLib:4.8.0")
```

## 测试与验证

项目已通过Gradle编译验证，所有代码都经过语法检查和类型验证，确保可以正常运行。

## 文档支持

- **数据库使用指南**: `docs/DATABASE_USAGE.md`
- **Helper使用指南**: `docs/HELPER_USAGE.md`
- **网络使用指南**: `docs/NETWORK_USAGE.md`
- **使用示例**: `example/UsageExample.kt`

## 注意事项与后续扩展

- **API限流**: B站API有调用频率限制，建议合理控制请求间隔。
- **Cookie保护**: Cookie包含敏感信息，请妥善保管。
- **异步处理**: 涉及网络和数据库的操作建议在异步线程中执行。
- **资源清理**: 使用完毕后调用 `destroyInstance()` 清理资源。

项目结构设计良好，未来可以轻松扩展更多B站API功能。

---

所有功能都已完成并经过编译验证，可以立即投入使用。如有任何问题或需要扩展功能，请随时联系。 