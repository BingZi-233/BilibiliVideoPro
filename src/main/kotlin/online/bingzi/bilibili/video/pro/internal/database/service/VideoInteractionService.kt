package online.bingzi.bilibili.video.pro.internal.database.service

import com.j256.ormlite.dao.Dao
import online.bingzi.bilibili.video.pro.internal.database.DatabaseManager
import online.bingzi.bilibili.video.pro.internal.database.entity.VideoInteractionRecord
import online.bingzi.bilibili.video.pro.internal.entity.database.service.PlayerStatistics

/**
 * Video interaction service impl
 * 视频互动服务实现
 *
 * @constructor Create empty Video interaction service impl
 */
object VideoInteractionService {

    fun recordInteraction(playerUuid: String, bvid: String, videoTitle: String, isLiked: Boolean, isCoined: Boolean, isFavorited: Boolean): VideoInteractionRecord {
        val record = findByPlayerUuidAndBvid(playerUuid, bvid)?.apply {
            this.isLiked = isLiked
            this.isCoined = isCoined
            this.isFavorited = isFavorited
            this.videoTitle = videoTitle
        } ?: VideoInteractionRecord(
            playerUuid,
            0, // bilibiliUid
            bvid,
            0L, // videoAid
            videoTitle,
            0L, // upMid
            isLiked,
            isCoined,
            0, // coinCount
            isFavorited,
            false, // isFollowingUp
            false // hasCommented
        )
        DatabaseManager.videoInteractionRecordDao.createOrUpdate(record)
        return record
    }

    fun findByPlayerUuidAndBvid(playerUuid: String, bvid: String): VideoInteractionRecord? {
        return DatabaseManager.videoInteractionRecordDao.queryForEq("player_uuid", playerUuid).firstOrNull { it.videoBvid == bvid }
    }

    fun getPlayerStatistics(playerUuid: String): PlayerStatistics {
        val records = DatabaseManager.videoInteractionRecordDao.queryForEq("player_uuid", playerUuid)
        val totalVideos = records.size.toLong()
        val likedVideos = records.count { it.isLiked }.toLong()
        val coinedVideos = records.count { it.isCoined }.toLong()
        val favoritedVideos = records.count { it.isFavorited }.toLong()
        val tripleCompletedVideos = records.count { it.isLiked && it.isCoined && it.isFavorited }.toLong()
        return PlayerStatistics(
            totalVideos = totalVideos,
            totalLikes = likedVideos,
            totalCoins = coinedVideos,
            totalFavorites = favoritedVideos,
            tripleCompletedVideos = tripleCompletedVideos
        )
    }

    /**
     * 获取总互动数
     */
    fun getTotalInteractionCount(): Int {
        return try {
            DatabaseManager.videoInteractionRecordDao.countOf().toInt()
        } catch (e: Exception) {
            0
        }
    }
}