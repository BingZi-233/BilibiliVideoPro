# BilibiliVideoPro 数据库使用指南

## 系统概述

本系统提供了完整的持久化解决方案，支持：
- 玩家Bilibili账户绑定管理
- 视频互动状态记录
- SQLite和MySQL双数据库支持
- 自动初始化和健康检查

## 核心组件

### 1. 配置系统
- **DatabaseConfig**: 使用TabooLib的@Config注解读取database.yml配置
- 支持热重载和类型安全的配置访问

### 2. 数据库管理器
- **DatabaseManager**: 负责连接管理、表创建、DAO初始化
- **PluginManager**: 使用@Awake注解自动处理插件生命周期

### 3. 实体层
- **PlayerBilibili**: 玩家绑定实体，包含Cookie存储和用户信息
- **VideoInteractionRecord**: 视频互动记录，支持三连状态追踪

### 4. 服务层
- **PlayerBilibiliService**: 玩家绑定业务逻辑
- **VideoInteractionService**: 视频互动记录业务逻辑

## 配置示例

### database.yml
```yaml
database:
  type: "sqlite"  # 或 "mysql"
  sqlite:
    file: "bilibili_data.db"
    pool:
      maximum_pool_size: 10
      minimum_idle: 2
  mysql:
    host: "localhost"
    port: 3306
    database: "bilibili_video_pro"
    username: "bilibili_user"
    password: "your_password_here"
    pool:
      maximum_pool_size: 20
      minimum_idle: 5

table_prefix: "bvp_"
auto_create_tables: true
enable_sql_logging: false
```

## 使用示例

### 1. 玩家绑定
```kotlin
val playerService = PlayerBilibiliService()

// 创建绑定
val result = playerService.createBinding(
    playerUuid = player.uniqueId.toString(),
    playerName = player.name,
    userInfo = loginUserInfo,
    cookies = cookieMap
)

when (result) {
    is BindingResult.Success -> {
        player.sendMessage("绑定成功!")
    }
    is BindingResult.AlreadyBound -> {
        player.sendMessage("已绑定账户")
    }
    else -> {
        player.sendMessage("绑定失败")
    }
}

// 查找绑定
val binding = playerService.findByPlayerUuid(playerUuid)
if (binding != null) {
    println("用户: ${binding.bilibiliUsername}")
}
```

### 2. 视频互动记录
```kotlin
val videoService = VideoInteractionService()

// 记录视频状态
when (val result = videoService.recordInteraction(playerUuid, "BV1xx411c7mD")) {
    is RecordResult.Created -> {
        println("新记录: ${result.record.videoTitle}")
    }
    is RecordResult.Updated -> {
        println("状态: ${result.record.getTripleActionStatus()}")
    }
    else -> {
        println("记录失败")
    }
}

// 获取统计信息
val stats = videoService.getPlayerStatistics(playerUuid)
println("总视频数: ${stats.totalVideos}")
println("三连完成数: ${stats.tripleCompletedVideos}")
```

### 3. 系统健康检查
```kotlin
// 检查数据库状态
val healthInfo = DatabaseManager.healthCheck()
if (healthInfo.isHealthy) {
    console().sendInfo("数据库正常")
} else {
    console().sendInfo("数据库异常: ${healthInfo.message}")
}

// 获取连接池状态
val poolStatus = DatabaseManager.getPoolStatus()
console().sendInfo(poolStatus)
```

## 最佳实践

### 1. 异步操作
```kotlin
// 数据库操作应该在异步线程中执行
Bukkit.getScheduler().runTaskAsynchronously(plugin) {
    val result = playerService.createBinding(...)
    
    // 回到主线程更新UI
    Bukkit.getScheduler().runTask(plugin) {
        player.sendMessage("操作完成")
    }
}
```

### 2. 错误处理
```kotlin
try {
    val result = videoService.recordInteraction(...)
} catch (e: DatabaseServiceException) {
    logger.error("数据库操作失败", e)
} catch (e: Exception) {
    logger.error("未知错误", e)
}
```

### 3. 定期维护
```kotlin
// 定期清理旧数据
Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, {
    val deleted = videoService.cleanupOldRecords(30)
    console().sendInfo("清理了 $deleted 条记录")
}, 20L * 60L * 60L, 20L * 60L * 60L * 24L)
```

## 自动初始化

系统使用TabooLib的@Awake注解自动管理生命周期：

```kotlin
@Awake(value = Awake.LifeCycle.ENABLE)
fun initialize() {
    // 自动初始化数据库和网络管理器
}

@Awake(value = Awake.LifeCycle.ACTIVE)
fun healthCheck() {
    // 自动执行健康检查
}

@Awake(value = Awake.LifeCycle.DISABLE)
fun cleanup() {
    // 自动清理资源
}
```

## 故障排除

### 常见问题
1. **配置错误**: 检查database.yml格式和参数
2. **连接失败**: 验证数据库服务状态和权限
3. **性能问题**: 调整连接池大小和启用索引

### 调试工具
- 启用SQL日志: `enable_sql_logging: true`
- 健康检查: `DatabaseManager.healthCheck()`
- 连接池监控: `DatabaseManager.getPoolStatus()`

---

现在您已经拥有了一个完整的、基于TabooLib配置系统的数据库持久化解决方案，可以安全地管理玩家绑定和视频互动数据。 