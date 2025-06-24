package online.bingzi.bilibili.video.pro.api.event

import online.bingzi.bilibili.video.pro.api.entity.BilibiliUser
import online.bingzi.bilibili.video.pro.api.entity.BilibiliVideo

/**
 * Bilibili连续操作事件
 *
 * 当用户连续进行三连操作达到特定条件时触发此事件
 * 用于追踪用户的活跃度和连续支持行为，触发连续奖励
 *
 * @property user 执行连续操作的Bilibili用户
 * @property streakType 连续操作类型
 * @property streakCount 连续次数
 * @property currentVideo 当前操作的视频（如果有）
 * @property recentVideos 最近操作的视频列表
 * @property timeSpan 连续操作的时间跨度（毫秒）
 * @property timestamp 事件时间戳
 *
 * @constructor 创建一个Bilibili连续操作事件
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
class BilibiliStreakEvent(
    /**
     * 执行连续操作的Bilibili用户
     *
     * 包含用户的基本信息和绑定状态
     */
    override val user: BilibiliUser,

    /**
     * 连续操作类型
     *
     * 描述连续操作的具体类型
     */
    val streakType: StreakType,

    /**
     * 连续次数
     *
     * 当前连续操作的次数
     */
    val streakCount: Int,

    /**
     * 当前操作的视频
     *
     * 触发连续事件的最新视频，某些情况下可能为null
     */
    val currentVideo: BilibiliVideo? = null,

    /**
     * 最近操作的视频列表
     *
     * 参与连续操作计算的视频列表
     */
    val recentVideos: List<BilibiliVideo> = emptyList(),

    /**
     * 连续操作的时间跨度
     *
     * 从第一次操作到当前操作的时间差（毫秒）
     */
    val timeSpan: Long,

    /**
     * 事件时间戳
     *
     * 记录连续事件触发的时间
     */
    timestamp: Long = System.currentTimeMillis()
) : BilibiliEvent(user, timestamp) {

    /**
     * 连续操作类型枚举
     *
     * 定义了所有可能的连续操作类型
     */
    enum class StreakType(
        /**
         * 类型名称
         */
        val typeName: String,

        /**
         * 类型描述
         */
        val description: String,

        /**
         * 最小连续次数
         */
        val minStreakCount: Int
    ) {
        /**
         * 连续点赞
         *
         * 用户连续对多个视频点赞
         */
        LIKE_STREAK("连续点赞", "用户连续对多个视频点赞", 3),

        /**
         * 连续投币
         *
         * 用户连续对多个视频投币
         */
        COIN_STREAK("连续投币", "用户连续对多个视频投币", 3),

        /**
         * 连续收藏
         *
         * 用户连续收藏多个视频
         */
        FAVORITE_STREAK("连续收藏", "用户连续收藏多个视频", 3),

        /**
         * 连续三连
         *
         * 用户连续对多个视频进行完整三连
         */
        TRIPLE_STREAK("连续三连", "用户连续对多个视频进行完整三连", 2),

        /**
         * 日常活跃
         *
         * 用户在一天内进行多次三连操作
         */
        DAILY_ACTIVE("日常活跃", "用户在一天内进行多次三连操作", 5),

        /**
         * 超级支持者
         *
         * 用户对特定UP主进行连续支持
         */
        SUPER_SUPPORTER("超级支持者", "用户对特定UP主进行连续支持", 3)
    }

    /**
     * 检查连续次数是否达到里程碑
     *
     * @return true表示达到了重要的连续次数里程碑
     */
    fun isMilestone(): Boolean {
        val milestones = listOf(5, 10, 20, 30, 50, 100)
        return streakCount in milestones
    }

    /**
     * 获取平均操作间隔
     *
     * @return 平均每次操作的时间间隔（毫秒）
     */
    fun getAverageInterval(): Long {
        return if (streakCount <= 1) 0 else timeSpan / (streakCount - 1)
    }

    /**
     * 检查是否为快速连续操作
     *
     * 快速连续操作定义为平均间隔小于30分钟
     *
     * @return true表示是快速连续操作
     */
    fun isFastStreak(): Boolean {
        val thirtyMinutes = 30 * 60 * 1000L
        return getAverageInterval() < thirtyMinutes
    }

    /**
     * 获取连续操作的效率评分
     *
     * 基于连续次数和时间间隔计算效率评分
     *
     * @return 效率评分（0-100）
     */
    fun getEfficiencyScore(): Int {
        val baseScore = minOf(streakCount * 10, 50)
        val speedBonus = if (isFastStreak()) 30 else 10
        val milestoneBonus = if (isMilestone()) 20 else 0
        return minOf(baseScore + speedBonus + milestoneBonus, 100)
    }

    /**
     * 检查是否涉及多个不同作者
     *
     * @return true表示连续操作涉及多个不同的视频作者
     */
    fun involvesMultipleAuthors(): Boolean {
        return recentVideos.map { it.authorUid }.distinct().size > 1
    }

    /**
     * 获取主要支持的作者
     *
     * @return 在连续操作中被支持最多的作者UID，如果没有明显偏向则返回null
     */
    fun getPrimaryAuthor(): String? {
        if (recentVideos.isEmpty()) return null

        val authorCounts = recentVideos.groupBy { it.authorUid }
            .mapValues { it.value.size }

        val maxCount = authorCounts.maxByOrNull { it.value }?.value ?: 0
        val authorsWithMaxCount = authorCounts.filter { it.value == maxCount }

        // 只有在明显偏向某个作者时才返回
        return if (authorsWithMaxCount.size == 1 && maxCount >= streakCount / 2) {
            authorsWithMaxCount.keys.first()
        } else null
    }

    override fun getEventDescription(): String {
        val efficiency = getEfficiencyScore()
        val milestone = if (isMilestone()) " [里程碑]" else ""
        return "用户 ${user.nickname} 达成${streakType.typeName} ${streakCount}次连击$milestone (效率: $efficiency%)"
    }

    /**
     * 获取详细的连续操作报告
     *
     * @return 包含所有连续操作详情的报告
     */
    fun getDetailedStreakReport(): String {
        return buildString {
            appendLine("=== Bilibili连续操作报告 ===")
            appendLine("用户：${user.nickname} (${user.uid})")
            appendLine("连续类型：${streakType.typeName}")
            appendLine("连续次数：$streakCount 次")
            appendLine("时间跨度：${timeSpan / 1000 / 60} 分钟")
            appendLine("平均间隔：${getAverageInterval() / 1000 / 60} 分钟")
            appendLine("操作效率：${getEfficiencyScore()}%")
            appendLine("是否里程碑：${if (isMilestone()) "是" else "否"}")
            appendLine("涉及作者数：${recentVideos.map { it.authorUid }.distinct().size}")
            getPrimaryAuthor()?.let { authorUid ->
                val authorName = recentVideos.find { it.authorUid == authorUid }?.authorName
                appendLine("主要支持：$authorName ($authorUid)")
            }
            currentVideo?.let {
                appendLine("当前视频：${it.title}")
            }
            appendLine("记录时间：${getFormattedTime()}")
        }
    }

    override fun toString(): String {
        return "BilibiliStreakEvent(${user.nickname} -> ${streakType.typeName} x$streakCount)"
    }
} 