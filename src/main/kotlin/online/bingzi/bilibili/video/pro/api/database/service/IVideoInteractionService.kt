
package online.bingzi.bilibili.video.pro.api.database.service

import online.bingzi.bilibili.video.pro.internal.database.entity.VideoInteractionRecord

/**
 * Video interaction service
 * 视频互动服务
 *
 * @constructor Create empty Video interaction service
 */
interface IVideoInteractionService {
    /**
     * Record interaction
     * 记录互动
     *
     * @param playerUuid 玩家 UUID
     * @param bvid 视频 BVID
     * @param videoTitle 视频标题
     * @param isLiked 是否点赞
     * @param isCoined 是否投币
     * @param isFavorited 是否收藏
     * @return 视频互动记录实体
     */
    fun recordInteraction(playerUuid: String, bvid: String, videoTitle: String, isLiked: Boolean, isCoined: Boolean, isFavorited: Boolean): VideoInteractionRecord

    /**
     * Find by player uuid and bvid
     * 通过玩家 UUID 和 BVID 查找
     *
     * @param playerUuid 玩家 UUID
     * @param bvid 视频 BVID
     * @return 视频互动记录实体
     */
    fun findByPlayerUuidAndBvid(playerUuid: String, bvid: String): VideoInteractionRecord?

    /**
     * Get player statistics
     * 获取玩家统计信息
     *
     * @param playerUuid 玩家 UUID
     * @return 玩家统计信息
     */
    fun getPlayerStatistics(playerUuid: String): PlayerStatistics
}

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
