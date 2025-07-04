package online.bingzi.bilibili.video.pro.internal.entity.database.service

/**
 * Player statistics
 * 玩家统计信息
 *
 * @property totalVideos 总视频数
 * @property totalLikes 总点赞数
 * @property totalCoins 总投币数
 * @property totalFavorites 总收藏数
 * @property tripleCompletedVideos 三连完成视频数
 * @property totalRewards 总奖励次数
 * @property averageTriplePerDay 平均每日三连次数
 * @constructor Create empty Player statistics
 */
data class PlayerStatistics(
    val totalVideos: Long,
    val totalLikes: Long,
    val totalCoins: Long,
    val totalFavorites: Long,
    val tripleCompletedVideos: Long,
    val totalRewards: Long = 0,
    val averageTriplePerDay: Double = 0.0
)