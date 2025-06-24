# 数据库持久化使用指南

## 概述

BilibiliVideoPro 插件使用 ORMLite 轻量级 ORM 框架实现数据持久化，支持 SQLite 和 MySQL
两种数据源的无缝切换。本文档详细介绍了数据库系统的使用方法、配置选项和最佳实践。

## 📋 目录

- [系统架构](#系统架构)
- [数据库配置](#数据库配置)
- [实体模型](#实体模型)
- [数据访问层](#数据访问层)
- [使用示例](#使用示例)
- [性能优化](#性能优化)
- [故障排除](#故障排除)

## 🏗️ 系统架构

### 技术栈

- **ORM框架**: ORMLite 6.1 - 轻量级、高性能的Java ORM框架
- **连接池**: HikariCP 5.1.0 - 高性能JDBC连接池
- **支持数据库**:
    - SQLite 3.44.1.0 - 适合小型应用和开发环境
    - MySQL 8.0.33 - 适合生产环境和大规模应用

### 架构层次

```
┌─────────────────────────────────────┐
│           应用层 (Plugin)            │
├─────────────────────────────────────┤
│         服务层 (DatabaseService)     │
├─────────────────────────────────────┤
│          DAO层 (Data Access)        │
├─────────────────────────────────────┤
│         管理层 (DatabaseManager)     │
├─────────────────────────────────────┤
│          ORM层 (ORMLite)            │
├─────────────────────────────────────┤
│         连接池 (HikariCP)            │
├─────────────────────────────────────┤
│        数据库 (SQLite/MySQL)         │
└─────────────────────────────────────┘
```

## ⚙️ 数据库配置

### SQLite 配置

```yaml
database:
  type: sqlite
  sqlite:
    file: "plugins/BilibiliVideoPro/data.db"
  pool:
    maxPoolSize: 1      # SQLite只支持单连接
    minIdle: 1
    connectionTimeout: 30000
    validationTimeout: 5000
```

**优点**:

- 零配置，开箱即用
- 适合小型服务器和开发环境
- 自动创建文件和目录
- 支持自动备份功能

**缺点**:

- 并发性能有限
- 不支持多服务器共享数据

### MySQL 配置

```yaml
database:
  type: mysql
  mysql:
    host: "localhost"
    port: 3306
    database: "bilibili_video_pro"
    username: "root"
    password: "your_password"
  pool:
    maxPoolSize: 10     # 生产环境建议10-20
    minIdle: 2
    maxLifetime: 600000
    connectionTimeout: 30000
    idleTimeout: 300000
    validationTimeout: 5000
```

**优点**:

- 高并发性能
- 支持多服务器共享数据
- 完整的事务支持
- 丰富的管理工具

**配置要求**:

- MySQL 5.7+ 或 MariaDB 10.2+
- 支持 utf8mb4 字符集
- 建议启用 InnoDB 存储引擎

### 连接池优化

| 参数                | SQLite 推荐值 | MySQL 推荐值 | 说明       |
|-------------------|------------|-----------|----------|
| maxPoolSize       | 1          | 10-20     | 最大连接数    |
| minIdle           | 1          | 2-5       | 最小空闲连接   |
| maxLifetime       | 600000     | 600000    | 连接最大生命周期 |
| connectionTimeout | 30000      | 30000     | 连接超时时间   |
| idleTimeout       | 300000     | 300000    | 空闲超时时间   |

## 📊 实体模型

### BilibiliUserEntity - 用户实体

```kotlin
@DatabaseTable(tableName = "bilibili_users")
class BilibiliUserEntity {
    @DatabaseField(generatedId = true)
    var id: Long = 0
    
    @DatabaseField(unique = true, index = true)
    var uid: String = ""
    
    @DatabaseField
    var nickname: String = ""
    
    @DatabaseField(index = true)
    var minecraftUuid: String? = null
    
    @DatabaseField
    var bindTime: Long = 0
    
    // ... 其他字段
}
```

**字段说明**:

- `id`: 自增主键
- `uid`: Bilibili用户UID（唯一）
- `nickname`: 用户昵称
- `minecraftUuid`: 绑定的MC玩家UUID
- `bindTime`: 绑定时间戳
- `status`: 用户状态 (1:正常, 0:禁用)

### BilibiliVideoEntity - 视频实体

```kotlin
@DatabaseTable(tableName = "bilibili_videos")
class BilibiliVideoEntity {
    @DatabaseField(generatedId = true)
    var id: Long = 0
    
    @DatabaseField(unique = true, index = true)
    var aid: Long = 0
    
    @DatabaseField(unique = true, index = true)
    var bvid: String = ""
    
    @DatabaseField
    var title: String = ""
    
    @DatabaseField(index = true)
    var authorUid: String = ""
    
    // ... 其他字段
}
```

### TripleActionRecordEntity - 三连记录实体

```kotlin
@DatabaseTable(tableName = "triple_action_records")
class TripleActionRecordEntity {
    @DatabaseField(generatedId = true)
    var id: Long = 0
    
    @DatabaseField(index = true)
    var userUid: String = ""
    
    @DatabaseField(index = true)
    var videoBvid: String = ""
    
    @DatabaseField
    var liked: Int = 0      // 1:已点赞, 0:未点赞
    
    @DatabaseField
    var coined: Int = 0     // 1:已投币, 0:未投币
    
    @DatabaseField
    var favorited: Int = 0  // 1:已收藏, 0:未收藏
    
    // ... 其他字段
}
```

## 🔧 数据访问层

### DatabaseService - 统一服务接口

```kotlin
// 初始化数据库
DatabaseService.initializeWithSqlite("data/bilibili.db")
// 或者
DatabaseService.initializeWithMysql("localhost", 3306, "db", "user", "pass")

// 用户操作
val success = DatabaseService.createOrUpdateUser("123456", "用户名", playerUuid)
val user = DatabaseService.getUserByUid("123456")
val boundUser = DatabaseService.getUserByMinecraftUuid(playerUuid)

// 视频操作
DatabaseService.createOrUpdateVideo(aid, bvid, title, authorUid, authorName, uploadTime)
val video = DatabaseService.getVideoByBvid("BV1234567890")

// 三连记录
DatabaseService.recordTripleAction(userUid, videoBvid, videoAid, true, true, 2, true)
val records = DatabaseService.getFullTripleRecords()
```

### DAO层直接访问

```kotlin
// 获取数据库管理器
val dbManager = DatabaseManager(config)
dbManager.initialize()

// 使用DAO
val userDao = BilibiliUserDao(dbManager)
val videoDao = BilibiliVideoDao(dbManager)
val actionDao = TripleActionRecordDao(dbManager)

// 复杂查询
val recentUsers = userDao.findRecentlyBoundUsers(7)
val activeUsers = userDao.findActiveUsers()
val userStats = userDao.getUserStats()
```

## 💡 使用示例

### 基础操作示例

```kotlin
class BilibiliService {
    
    fun bindPlayerAccount(player: Player, bilibiliUid: String) {
        try {
            // 检查是否已绑定其他账号
            val existingUser = DatabaseService.getUserByMinecraftUuid(player.uniqueId)
            if (existingUser != null) {
                player.sendMessage("§c您已经绑定了账号: ${existingUser.nickname}")
                return
            }
            
            // 绑定新账号
            val success = DatabaseService.createOrUpdateUser(
                uid = bilibiliUid,
                nickname = "临时昵称", // 后续通过API获取
                minecraftUuid = player.uniqueId
            )
            
            if (success) {
                player.sendMessage("§a账号绑定成功！")
                // 触发绑定奖励
                triggerBindReward(player)
            } else {
                player.sendMessage("§c绑定失败，请稍后重试")
            }
            
        } catch (e: Exception) {
            console().sendMessage("绑定账号时发生错误: ${e.message}")
            player.sendMessage("§c系统错误，请联系管理员")
        }
    }
    
    fun checkTripleAction(userUid: String, videoBvid: String): TripleActionResult {
        // 获取之前的记录
        val previousRecord = DatabaseService.getTripleActionRecord(userUid, videoBvid)
        
        // 通过API获取最新状态
        val currentState = BilibiliApiManager.getTripleState(userUid, videoBvid)
        
        // 比较并记录变化
        val hasNewAction = if (previousRecord != null) {
            currentState.liked != previousRecord.isLiked() ||
            currentState.coined != previousRecord.isCoined() ||
            currentState.favorited != previousRecord.isFavorited()
        } else {
            currentState.liked || currentState.coined || currentState.favorited
        }
        
        if (hasNewAction) {
            // 记录新的操作
            DatabaseService.recordTripleAction(
                userUid = userUid,
                videoBvid = videoBvid,
                videoAid = currentState.aid,
                liked = currentState.liked,
                coined = currentState.coined,
                coinCount = currentState.coinCount,
                favorited = currentState.favorited
            )
            
            return TripleActionResult(
                hasChange = true,
                isFullTriple = currentState.liked && currentState.coined && currentState.favorited,
                newActions = detectNewActions(previousRecord, currentState)
            )
        }
        
        return TripleActionResult(hasChange = false)
    }
}
```

### 批量处理示例

```kotlin
class BatchProcessor {
    
    suspend fun processPendingRewards() = withContext(Dispatchers.IO) {
        val unrewardedRecords = DatabaseService.getUnrewardedRecords()
        
        unrewardedRecords.forEach { record ->
            try {
                val user = DatabaseService.getUserByUid(record.userUid)
                if (user?.isBoundToMinecraft() == true) {
                    val player = Bukkit.getPlayer(user.getMinecraftUUID()!!)
                    if (player != null && player.isOnline) {
                        // 发放奖励
                        val rewardType = determineRewardType(record)
                        val success = giveReward(player, rewardType, record)
                        
                        if (success) {
                            DatabaseService.markAsRewarded(
                                recordId = record.id,
                                rewardType = rewardType.name,
                                rewardContent = rewardType.description
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                console().sendMessage("处理奖励时发生错误: ${e.message}")
            }
        }
    }
    
    fun generateDailyReport(): DailyReport {
        val stats = DatabaseService.getDatabaseStats()
        val userStats = DatabaseService.getUserStats()
        val fullTripleCount = DatabaseService.getFullTripleRecords().size
        
        return DailyReport(
            totalUsers = userStats?.totalUsers ?: 0,
            activeUsers = userStats?.activeUsers ?: 0,
            boundUsers = userStats?.boundUsers ?: 0,
            totalVideos = stats?.videoCount ?: 0,
            totalTripleActions = stats?.actionRecordCount ?: 0,
            fullTripleActions = fullTripleCount.toLong(),
            databaseStatus = stats?.isConnected ?: false
        )
    }
}
```

## 🚀 性能优化

### 数据库索引

所有实体都已经配置了合适的索引：

```kotlin
// 用户表索引
@DatabaseField(unique = true, index = true)  // uid 唯一索引
@DatabaseField(index = true)                 // minecraft_uuid 索引

// 视频表索引  
@DatabaseField(unique = true, index = true)  // aid, bvid 唯一索引
@DatabaseField(index = true)                 // author_uid, upload_time 索引

// 操作记录表索引
@DatabaseField(index = true)                 // user_uid, video_bvid, action_time 索引
```

### 连接池调优

**小型服务器 (< 50人)**:

```yaml
pool:
  maxPoolSize: 5
  minIdle: 2
  connectionTimeout: 15000
```

**中型服务器 (50-200人)**:

```yaml
pool:
  maxPoolSize: 10
  minIdle: 3
  connectionTimeout: 20000
```

**大型服务器 (> 200人)**:

```yaml
pool:
  maxPoolSize: 20
  minIdle: 5
  connectionTimeout: 30000
```

### 查询优化

```kotlin
// ✅ 好的做法 - 使用索引字段查询
val user = userDao.findByUid(uid)
val video = videoDao.findByBvid(bvid)

// ✅ 好的做法 - 批量操作
val users = userDao.findActiveUsers()

// ❌ 避免 - 全表扫描
val users = userDao.findAll().filter { it.isActive() }

// ✅ 好的做法 - 分页查询
val users = userDao.findByPage(page = 1, pageSize = 20)
```

### 缓存策略

```kotlin
class CachedUserService {
    private val userCache = CacheBuilder.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .maximumSize(1000)
        .build<String, BilibiliUserEntity>()
    
    fun getUserByUid(uid: String): BilibiliUserEntity? {
        return userCache.get(uid) {
            DatabaseService.getUserByUid(uid)
        }
    }
}
```

## 🔍 故障排除

### 常见问题

#### 1. 数据库连接失败

**SQLite 问题**:

```
java.sql.SQLException: path to 'data.db': 'plugins/BilibiliVideoPro' does not exist
```

**解决方法**:

- 检查文件路径权限
- 确保父目录存在
- 检查磁盘空间

**MySQL 问题**:

```
java.sql.SQLException: Access denied for user 'root'@'localhost'
```

**解决方法**:

- 验证用户名和密码
- 检查MySQL服务状态
- 确认数据库存在
- 检查防火墙设置

#### 2. 表创建失败

```
java.sql.SQLException: table bilibili_users already exists
```

**解决方法**:

- 删除旧表：`DROP TABLE IF EXISTS bilibili_users`
- 或者禁用表自动创建，手动管理表结构

#### 3. 连接池耗尽

```
java.sql.SQLException: Connection is not available, request timed out after 30000ms
```

**解决方法**:

- 增加 `maxPoolSize`
- 减少 `connectionTimeout`
- 检查连接泄漏
- 优化慢查询

### 监控和诊断

```kotlin
// 数据库状态监控
fun monitorDatabaseHealth() {
    val stats = DatabaseService.getDatabaseStats()
    if (stats != null) {
        console().sendMessage("数据库状态: ${if (stats.isConnected) "正常" else "异常"}")
        console().sendMessage("活跃连接: ${stats.poolStats.activeConnections}")
        console().sendMessage("空闲连接: ${stats.poolStats.idleConnections}")
        console().sendMessage("等待连接: ${stats.poolStats.threadsAwaitingConnection}")
        
        // 警告阈值
        if (stats.poolStats.threadsAwaitingConnection > 5) {
            console().sendMessage("§c警告: 连接池可能不足")
        }
    }
}
```

### 性能分析

```kotlin
// SQL执行时间监控
class DatabaseProfiler {
    fun <T> profile(operation: String, block: () -> T): T {
        val startTime = System.currentTimeMillis()
        return try {
            block()
        } finally {
            val duration = System.currentTimeMillis() - startTime
            if (duration > 100) { // 超过100ms记录
                console().sendMessage("§e慢查询: $operation 耗时 ${duration}ms")
            }
        }
    }
}
```

## 📚 最佳实践

### 1. 数据库设计

- **使用合适的索引**: 为经常查询的字段创建索引
- **避免过度规范化**: 在性能和存储之间找平衡
- **使用枚举替代字符串**: 提高存储效率和查询性能

### 2. 连接管理

- **及时关闭连接**: 使用 try-with-resources 或 use 函数
- **使用连接池**: 避免频繁创建和销毁连接
- **监控连接状态**: 定期检查连接池健康状况

### 3. 异常处理

```kotlin
fun safeDbOperation<T>(operation: () -> T): T? {
    return try {
        operation()
    } catch (e: SQLException) {
        console().sendMessage("数据库操作失败: ${e.message}")
        null
    } catch (e: Exception) {
        console().sendMessage("未知错误: ${e.message}")
        null
    }
}
```

### 4. 数据备份

```kotlin
// 定期备份 (仅SQLite)
class BackupScheduler {
    fun scheduleBackup() {
        timer(period = 24 * 60 * 60 * 1000L) { // 每24小时
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val backupPath = "backups/bilibili_${timestamp}.db"
            
            if (DatabaseService.backup(backupPath)) {
                console().sendMessage("数据库备份成功: $backupPath")
                cleanOldBackups()
            }
        }
    }
}
```

## 🔗 相关文档

- [事件系统文档](EVENT_SYSTEM.md)
- [HTTP客户端使用文档](HTTP_CLIENT_USAGE.md)
- [ORMLite 官方文档](https://ormlite.com/)
- [HikariCP 配置指南](https://github.com/brettwooldridge/HikariCP)

---

**版本**: 1.0.0  
**最后更新**: 2024年12月  
**维护者**: BilibiliVideoPro Team 