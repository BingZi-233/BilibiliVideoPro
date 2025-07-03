package online.bingzi.bilibili.video.pro.internal.database.service

import com.j256.ormlite.dao.Dao
import online.bingzi.bilibili.video.pro.api.database.service.IVideoInteractionService
import online.bingzi.bilibili.video.pro.api.database.service.PlayerStatistics
import online.bingzi.bilibili.video.pro.internal.database.entity.VideoInteractionRecord
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

import taboolib.common.platform.service.PlatformExecutor

/**
 * Video interaction service impl
 * 视频互动服务实现
 *
 * @constructor Create empty Video interaction service impl
 */
class VideoInteractionServiceImpl : IVideoInteractionService {

    
    lateinit var videoInteractionRecordDao: Dao<VideoInteractionRecord, Long>

    override fun recordInteraction(playerUuid: String, bvid: String, videoTitle: String, isLiked: Boolean, isCoined: Boolean, isFavorited: Boolean): VideoInteractionRecord {
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
        videoInteractionRecordDao.createOrUpdate(record)
        return record
    }

    override fun findByPlayerUuidAndBvid(playerUuid: String, bvid: String): VideoInteractionRecord? {
        return videoInteractionRecordDao.queryForEq("player_uuid", playerUuid).firstOrNull { it.videoBvid == bvid }
    }

    override fun getPlayerStatistics(playerUuid: String): PlayerStatistics {
        val records = videoInteractionRecordDao.queryForEq("player_uuid", playerUuid)
        val totalVideos = records.size.toLong()
        val likedVideos = records.count { it.isLiked }.toLong()
        val coinedVideos = records.count { it.isCoined }.toLong()
        val favoritedVideos = records.count { it.isFavorited }.toLong()
        val tripleCompletedVideos = records.count { it.isLiked && it.isCoined && it.isFavorited }.toLong()
        return PlayerStatistics(
            totalVideos = totalVideos,
            likedVideos = likedVideos,
            coinedVideos = coinedVideos,
            favoritedVideos = favoritedVideos,
            tripleCompletedVideos = tripleCompletedVideos
        )
    }

    @Awake(LifeCycle.ENABLE)
    companion object {
        
        
    }
}