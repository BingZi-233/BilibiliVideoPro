package online.bingzi.bilibili.video.pro.internal.database.service

import com.j256.ormlite.dao.Dao
import online.bingzi.bilibili.video.pro.api.database.service.IVideoInteractionService
import online.bingzi.bilibili.video.pro.api.database.service.PlayerStatistics
import online.bingzi.bilibili.video.pro.internal.database.entity.VideoInteractionRecord
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Inject
import taboolib.common.platform.Instance

/**
 * Video interaction service impl
 * 视频互动服务实现
 *
 * @constructor Create empty Video interaction service impl
 */
@Instance
class VideoInteractionServiceImpl : IVideoInteractionService {

    @Inject
    lateinit var videoInteractionRecordDao: Dao<VideoInteractionRecord, Long>

    override fun recordInteraction(playerUuid: String, bvid: String, videoTitle: String, isLiked: Boolean, isCoined: Boolean, isFavorited: Boolean): VideoInteractionRecord {
        val record = findByPlayerUuidAndBvid(playerUuid, bvid)?.apply {
            this.isLiked = isLiked
            this.isCoined = isCoined
            this.isFavorited = isFavorited
            this.videoTitle = videoTitle
        } ?: VideoInteractionRecord(
            playerUuid = playerUuid,
            bvid = bvid,
            videoTitle = videoTitle,
            isLiked = isLiked,
            isCoained = isCoined,
            isFavorited = isFavorited
        )
        videoInteractionRecordDao.createOrUpdate(record)
        return record
    }

    override fun findByPlayerUuidAndBvid(playerUuid: String, bvid: String): VideoInteractionRecord? {
        return videoInteractionRecordDao.queryForEq("player_uuid", playerUuid).firstOrNull { it.bvid == bvid }
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

    companion object {
        @Awake(LifeCycle.ENABLE)
        fun init() {
            // 注册服务
            IVideoInteractionService::class.java.let { it.getConstructor() }
        }
    }
}