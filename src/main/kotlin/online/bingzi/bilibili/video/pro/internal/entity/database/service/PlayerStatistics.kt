package online.bingzi.bilibili.video.pro.internal.entity.database.service

/**
 * Player statistics
 * 玩家统计信息
 *
 * @property totalVideos 总视频数
 * @property likedVideos 点赞视频数
 * @property coinedVideos 投币视频数
 * @property favoritedVideos 收藏视频数
 * @property tripleCompletedVideos 三连完成视频数
 * @constructor Create empty Player statistics
 */
data class PlayerStatistics(
    val totalVideos: Long,
    val likedVideos: Long,
    val coinedVideos: Long,
    val favoritedVideos: Long,
    val tripleCompletedVideos: Long
)