# Bilibili视频一键三连奖励插件 - 事件系统文档

## 概述

本插件基于Bilibili API设计了一套完整的事件系统，用于处理用户的三连操作、奖励发放、连续操作追踪等功能。所有事件都遵循Bukkit事件系统规范，支持其他插件监听和扩展。

## 项目结构

```
src/main/kotlin/online/bingzi/bilibili/video/pro/api/
├── entity/                          # 实体类包
│   ├── BilibiliUser.kt             # Bilibili用户实体
│   ├── BilibiliVideo.kt            # Bilibili视频实体
│   ├── TripleActionData.kt         # 三连操作数据
│   └── package-info.kt             # 包说明文档
└── event/                          # 事件类包
    ├── BilibiliEvent.kt            # 事件基类
    ├── PlayerBindBilibiliEvent.kt  # 绑定事件
    ├── BilibiliTripleActionEvent.kt # 三连操作事件
    ├── BilibiliRewardEvent.kt      # 奖励事件
    ├── BilibiliVideoDetectEvent.kt # 视频检测事件
    ├── BilibiliStreakEvent.kt      # 连续操作事件
    └── package-info.kt             # 包说明文档
```

## 核心实体类

### BilibiliUser - Bilibili用户

```kotlin
data class BilibiliUser(
    val uid: String,                    // Bilibili用户ID
    val nickname: String,               // 用户昵称
    val minecraftUuid: UUID? = null,    // 绑定的MC UUID
    val bindTime: Long                  // 绑定时间
)
```

**主要方法：**

- `isBound()`: 检查是否已绑定MC账号
- `getBindDuration()`: 获取绑定时长

### BilibiliVideo - Bilibili视频

```kotlin
data class BilibiliVideo(
    val aid: Long,              // AV号
    val bvid: String,           // BV号
    val title: String,          // 视频标题
    val authorUid: String,      // 作者UID
    val authorName: String,     // 作者昵称
    val uploadTime: Long        // 上传时间
)
```

**主要方法：**

- `getVideoUrl()`: 获取视频链接
- `isRecentlyUploaded()`: 检查是否为最近上传

### TripleActionData - 三连操作数据

```kotlin
data class TripleActionData(
    val liked: Boolean = false,         // 是否点赞
    val coined: Boolean = false,        // 是否投币
    val coinCount: Int = 0,             // 投币数量
    val favorited: Boolean = false,     // 是否收藏
    val timestamp: Long                 // 操作时间
)
```

**主要方法：**

- `isFullTriple()`: 检查是否完成完整三连
- `isPartialTriple()`: 检查是否完成部分三连
- `getScore()`: 获取操作得分

## 事件系统

### 1. PlayerBindBilibiliEvent - 玩家绑定事件

当玩家绑定Bilibili账号时触发。

```kotlin
@EventHandler
fun onPlayerBind(event: PlayerBindBilibiliEvent) {
    val player = event.player
    val bilibiliUser = event.bilibiliUser

    if (event.isFirstTime) {
        // 首次绑定特殊处理
        player.sendMessage("欢迎绑定Bilibili账号！")
    }
}
```

### 2. BilibiliTripleActionEvent - 三连操作事件

当检测到用户进行三连操作时触发。

```kotlin
@EventHandler
fun onTripleAction(event: BilibiliTripleActionEvent) {
    if (event.isJustCompletedFullTriple()) {
        // 完成完整三连
        val player = event.user.minecraftUuid?.let { Bukkit.getPlayer(it) }
        player?.sendMessage("恭喜完成完整三连！")

        // 获取新增操作
        val newActions = event.getNewActions()
        println("新增操作：${newActions.joinToString(", ")}")
    }
}
```

### 3. BilibiliRewardEvent - 奖励事件

当系统准备发放奖励时触发。

```kotlin
@EventHandler
fun onReward(event: BilibiliRewardEvent) {
    when (event.rewardType) {
        BilibiliRewardEvent.RewardType.FULL_TRIPLE_REWARD -> {
            // 完整三连奖励
            givePlayerMoney(event.player, event.rewardValue)
        }
        BilibiliRewardEvent.RewardType.BIND_REWARD -> {
            // 绑定奖励
            giveWelcomeItems(event.player)
        }
        else -> {
            // 其他奖励类型
        }
    }
}
```

### 4. BilibiliStreakEvent - 连续操作事件

当用户达成连续操作时触发。

```kotlin
@EventHandler
fun onStreak(event: BilibiliStreakEvent) {
    if (event.isMilestone()) {
        // 达到里程碑
        val player = event.user.minecraftUuid?.let { Bukkit.getPlayer(it) }
        player?.sendMessage("连续操作${event.streakCount}次里程碑达成！")

        // 额外奖励
        giveStreakBonus(player, event.streakCount)
    }
}
```

### 5. BilibiliVideoDetectEvent - 视频检测事件

当检测到特定视频事件时触发。

```kotlin
@EventHandler
fun onVideoDetect(event: BilibiliVideoDetectEvent) {
    when (event.detectType) {
        BilibiliVideoDetectEvent.DetectType.NEW_VIDEO_UPLOAD -> {
            // 新视频发布
            notifySubscribers(event.video)
        }
        BilibiliVideoDetectEvent.DetectType.TRENDING_VIDEO -> {
            // 热门视频
            giveViralBonus(event.user)
        }
    }
}
```

## 奖励类型

### RewardType枚举

- `BIND_REWARD`: 绑定奖励
- `LIKE_REWARD`: 点赞奖励
- `COIN_REWARD`: 投币奖励
- `FAVORITE_REWARD`: 收藏奖励
- `PARTIAL_TRIPLE_REWARD`: 部分三连奖励
- `FULL_TRIPLE_REWARD`: 完整三连奖励
- `SPECIAL_REWARD`: 特殊奖励
- `STREAK_REWARD`: 连续奖励

## 连续操作类型

### StreakType枚举

- `LIKE_STREAK`: 连续点赞（最少3次）
- `COIN_STREAK`: 连续投币（最少3次）
- `FAVORITE_STREAK`: 连续收藏（最少3次）
- `TRIPLE_STREAK`: 连续三连（最少2次）
- `DAILY_ACTIVE`: 日常活跃（最少5次）
- `SUPER_SUPPORTER`: 超级支持者（最少3次）

## 使用示例

### 自定义奖励逻辑

```kotlin
class MyRewardListener : Listener {

    @EventHandler
    fun onBilibiliReward(event: BilibiliRewardEvent) {
        val player = event.player

        // 基础奖励
        when (event.rewardType) {
            BilibiliRewardEvent.RewardType.FULL_TRIPLE_REWARD -> {
                economy.depositPlayer(player, 100.0)
                player.sendMessage("获得100金币！")
            }
            BilibiliRewardEvent.RewardType.STREAK_REWARD -> {
                val bonus = event.rewardValue * 1.5 // 连续奖励增加50%
                economy.depositPlayer(player, bonus)
            }
        }

        // 特殊条件额外奖励
        if (event.video?.isRecentlyUploaded() == true) {
            // 对最新视频的支持额外奖励
            economy.depositPlayer(player, 50.0)
            player.sendMessage("支持最新视频，额外获得50金币！")
        }
    }

    @EventHandler
    fun onStreakAchieved(event: BilibiliStreakEvent) {
        if (event.isMilestone() && event.getEfficiencyScore() >= 80) {
            // 高效率连续操作的特殊奖励
            val player = event.user.minecraftUuid?.let { Bukkit.getPlayer(it) }
            player?.let {
                val item = ItemStack(Material.DIAMOND, event.streakCount / 10)
                it.inventory.addItem(item)
                it.sendMessage("高效率连击奖励！获得${item.amount}个钻石！")
            }
        }
    }
}
```

### 数据统计

```kotlin
class StatisticsManager {
    private val userStats = mutableMapOf<String, UserStats>()

    @EventHandler
    fun onTripleAction(event: BilibiliTripleActionEvent) {
        val uid = event.user.uid
        val stats = userStats.getOrPut(uid) { UserStats() }

        // 更新统计数据
        stats.totalActions++
        if (event.isJustCompletedFullTriple()) {
            stats.fullTriples++
        }

        // 更新最高连击记录
        event.getNewScore().let { score ->
            if (score > stats.highestSingleScore) {
                stats.highestSingleScore = score
            }
        }
    }
}
```

## 最佳实践

1. **事件监听优先级**: 使用`@EventHandler(priority = EventPriority.XXX)`控制处理顺序
2. **异步处理**: 对于耗时操作，使用异步任务避免阻塞主线程
3. **数据缓存**: 合理缓存用户数据，减少重复计算
4. **错误处理**: 添加适当的错误处理，避免事件处理失败影响其他功能
5. **配置化**: 将奖励数值、触发条件等做成可配置项

## API文档

详细的API文档请参考各类的KDoc注释，包含了完整的参数说明和使用示例。

## 依赖关系

- Bukkit/Spigot API
- TabooLib框架
- Kotlin标准库

## 版本兼容性

- Minecraft: 1.12+
- Java: 8+
- Kotlin: 1.8+

---

*此文档基于Bilibili
API文档（https://socialsisteryi.github.io/bilibili-API-collect/）设计，遵循最小化信息原则，提供完善的扩展性和类型安全保障。* 