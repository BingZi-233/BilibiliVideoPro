# BilibiliVideoPro

BilibiliVideoPro 是一个强大的、模块化的Bilibili互动插件/库。它为开发者提供了一套完整的解决方案，用于在应用程序中集成Bilibili的各种功能，如用户认证、视频互动、数据持久化等。该项目不仅可以作为Minecraft插件使用，其核心功能也可以被任何Java/Kotlin项目轻松集成。

## 核心功能

- **网络模块 (Network):**
  - **Bilibili API 封装:** 基于 `OkHttp3` 和 `Gson`，提供了对Bilibili常用API的封装，包括二维码登录、视频信息获取、用户关系（点赞、投币、收藏、关注）查询、评论区检查等。
  - **健壮的Cookie管理:** 实现自动化的Cookie持久化、刷新和有效性验证，确保长时间稳定登录。
  - **统一管理器:** `BilibiliNetworkManager` 作为单例入口，简化了网络功能的调用和生命周期管理。

- **数据库模块 (Database):**
  - **持久化存储:** 使用 `ORMLite` 和 `HikariCP` 连接池，支持将玩家与Bilibili账户的绑定信息、视频互动记录等数据持久化。
  - **多数据库支持:** 可通过配置文件在 `SQLite` 和 `MySQL` 之间轻松切换。
  - **服务层抽象:** 将数据库操作封装在 `Service` 层，使业务逻辑更清晰。

- **辅助工具模块 (Helper):**
  - **二维码生成:** 集成 `ZXing` 库，可以方便地将文本或URL生成为二维码图片。
  - **Minecraft集成:** 提供了将二维码渲染到Minecraft地图物品上的功能，极大地改善了游戏内的交互体验。

- **插件框架:**
  - **TabooLib 驱动:** 项目基于 `TabooLib` 框架构建，利用其强大的依赖注入、配置管理和生命周期管理功能，使得代码结构清晰、易于扩展。

## 项目亮点

- **高内聚、低耦合:** 清晰的模块划分（网络、数据库、工具），使得各部分功能可以独立使用或组合使用。
- **文档齐全:** 提供了详细的 `USAGE.md` 文档，涵盖了网络、数据库和辅助工具的使用方法，便于快速上手。
- **代码质量高:** 代码风格统一，注释清晰，并遵循了良好的设计模式（如单例、服务层、DAO等）。
- **Minecraft 深度集成:** 不仅仅是API调用，还考虑到了游戏内的实际应用场景，如使用地图物品展示二维码。

## 快速开始

1. **配置数据库**
   - 编辑 `database.yml` 文件，配置数据库类型和连接信息。

2. **使用网络功能**
   ```kotlin
   val networkManager = BilibiliNetworkManager.getInstance()
   networkManager.initialize()
   ```

3. **使用数据库功能**
   ```kotlin
   val playerService = PlayerBilibiliService()
   val binding = playerService.findByPlayerUuid(playerUuid)
   ```

4. **使用Helper工具**
   ```kotlin
   val qrImage = QRCodeHelper.generateQRCode("Hello World!", 128)
   ```

## 详细文档

- [网络功能使用指南](docs/NETWORK_USAGE.md)
- [数据库使用指南](docs/DATABASE_USAGE.md)
- [Helper工具使用说明](docs/HELPER_USAGE.md)
- [功能实现总结](docs/IMPLEMENTATION_SUMMARY.md)

## 依赖

- **Google ZXing**: 用于二维码生成
- **OkHttp3**: 用于HTTP请求
- **TabooLib**: 用于配置和生命周期管理
- **ORMLite**: 用于ORM数据库持久化
- **HikariCP**: 用于数据库连接池

## 注意事项

- **API限流**: B站API有调用频率限制，避免过于频繁的请求
- **Cookie安全**: 妥善保管Cookie信息，避免泄露
- **异步操作**: 数据库和网络操作应在异步线程中执行，避免阻塞主线程。

---

如有问题或建议，请在项目的Issue中提出。
