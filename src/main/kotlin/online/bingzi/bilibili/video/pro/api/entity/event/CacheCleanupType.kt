package online.bingzi.bilibili.video.pro.api.entity.event

/**
 * 缓存清理类型枚举
 *
 * 定义了缓存清理操作的不同类型。
 * 用于分类和统计缓存清理活动。
 *
 * @author BilibiliVideoPro
 * @since 2.0.0
 */
enum class CacheCleanupType {
    /**
     * 玩家冷却时间缓存清理
     * 清理过期的玩家全局冷却时间记录
     */
    PLAYER_COOLDOWN,

    /**
     * 视频冷却时间缓存清理
     * 清理过期的视频特定冷却时间记录
     */
    VIDEO_COOLDOWN,

    /**
     * 登录会话缓存清理
     * 清理过期的二维码登录会话
     */
    LOGIN_SESSION,

    /**
     * 全部缓存清理
     * 表示一次完整的缓存清理操作
     */
    ALL
}