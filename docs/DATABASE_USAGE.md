# BilibiliVideoPro 数据库使用指南

## 系统概述

本系统提供了完整的、可扩展的持久化解决方案，其设计遵循了现代软件工程原则，如SOLID。

- **依赖注入 (DI)**: 系统全面采用TabooLib的依赖注入容器，实现了低耦合和高内聚。
- **策略模式**: 数据库后端采用策略模式，可以轻松添加对新数据库（如PostgreSQL）的支持，而无需修改核心代码。
- **玩家Bilibili账户绑定管理**
- **视频互动状态记录**
- **SQLite和MySQL双数据库支持**
- **自动初始化和健康检查**

## 核心组件

### 1. 服务 (Service)
- **位置**: `online.bingzi.bilibili.video.pro.internal.database.service`
- **描述**: 包含了数据库操作的具体实现，例如 `PlayerBilibiliService` 和 `VideoInteractionService`。这些类由TabooLib自动管理其生命周期。

### 2. 数据库提供者 (Provider)
- **位置**: `online.bingzi.bilibili.video.pro.internal.database.provider`
- **描述**: 实现了数据库创建的策略模式。每个 `IDatabaseProvider` 负责一种特定类型数据库的连接池创建。`createDataSource` 方法现在接受 `DatabaseConfig` 对象，并且新增了 `getJdbcUrl` 方法用于获取JDBC连接字符串。

### 3. 数据库管理器 (Manager)
- **DatabaseManager**: 负责协调数据库的初始化、DAO注册和健康检查。DAO实例现在通过 `lateinit var` 延迟初始化，并且不再需要通过 `TabooLibAPI.register` 进行注册。它现在是一个协调者，而不是一个包含具体实现逻辑的工厂。

## 使用示例

直接使用服务对象是与数据库交互的最佳方式。

### 在TabooLib命令或服务中使用

```kotlin
import online.bingzi.bilibili.video.pro.internal.database.service.PlayerBilibiliService
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader

@CommandHeader(name = "myprofile")
object MyProfileCommand {

    @CommandBody
    fun view(sender: Player) {
        // 直接使用服务对象，代码干净且解耦
        val binding = PlayerBilibiliService.findByPlayerUuid(sender.uniqueId.toString())
        if (binding != null) {
            sender.sendMessage("你绑定的Bilibili账户是: ${binding.bilibiliUsername}")
        } else {
            sender.sendMessage("你尚未绑定Bilibili账户。")
        }
    }
}
```

### 获取统计信息

```kotlin
import online.bingzi.bilibili.video.pro.internal.database.service.VideoInteractionService

@CommandHeader(name = "mystats")
object MyStatsCommand {

    @CommandBody
    fun view(sender: Player) {
        val stats = VideoInteractionService.getPlayerStatistics(sender.uniqueId.toString())
        sender.sendMessage("你已记录 ${stats.totalVideos} 个视频的互动，完成了 ${stats.tripleCompletedVideos} 次三连。")
    }
}
```

## 最佳实践

1.  **直接使用服务**: 在你的代码中直接使用 `PlayerBilibiliService` 和 `VideoInteractionService` 对象。
2.  **异步操作**: 数据库操作应该在异步线程中执行，以避免阻塞服务器主线程。

    ```kotlin
    submit(async = true) {
        val stats = VideoInteractionService.getPlayerStatistics(player.uniqueId.toString())
        // ... 回到主线程更新UI ...
        submit {
            player.sendMessage("查询完成！")
        }
    }
    ```

## 扩展数据库支持

如果您想添加对 **PostgreSQL** 的支持，只需两步：

1.  **创建 `PostgreSQLProvider.kt`**:

    ```kotlin
    class PostgreSQLProvider : IDatabaseProvider {
        override val type = "postgresql"
        override fun createDataSource(config: DatabaseConfig.DatabaseDetails): HikariDataSource {
            // ... 实现PostgreSQL数据源的创建逻辑 ...
        }
    }
    ```

2.  **注册提供者**:
    在 `DatabaseManager` 的 `init` 块中添加一行代码：

    ```kotlin
    init {
        registerProvider(SQLiteProvider())
        registerProvider(MySQLProvider())
        registerProvider(PostgreSQLProvider()) // 添加这一行
    }
    ```

就这样！整个系统的其他部分都无需改动。

---

通过采用依赖注入和策略模式的设计，本项目的数据库模块现在拥有了极高的灵活性和可维护性。