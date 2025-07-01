package online.bingzi.bilibili.video.pro.internal.database.service

import com.j256.ormlite.stmt.QueryBuilder
import online.bingzi.bilibili.video.pro.internal.database.DatabaseManager
import online.bingzi.bilibili.video.pro.internal.database.entity.VideoInteractionRecord
import online.bingzi.bilibili.video.pro.internal.network.BilibiliNetworkManager
import online.bingzi.bilibili.video.pro.internal.entity.netwrk.video.VideoInteractionResult
import java.sql.SQLException
import java.util.*

/**
 * 视频互动记录服务
 * 提供视频互动记录的增删改查和统计功能
 */
class VideoInteractionService {
    
    private val dao = DatabaseManager.getVideoInteractionRecordDao()
    private val networkManager = BilibiliNetworkManager.getInstance()
    private val playerService = PlayerBilibiliService()
    
    /**
     * 记录或更新视频互动状态
     */
    fun recordInteraction(playerUuid: String, bvid: String): RecordResult {
        return try {
            // 检查玩家绑定
            val playerBilibili = playerService.findByPlayerUuid(playerUuid)
                ?: return RecordResult.NotBound("玩家未绑定Bilibili账户")
            
            // 设置Cookie并获取互动状态
            networkManager.setCookies(playerBilibili.getCookieMap())
            
            when (val result = networkManager.videoInteraction.getVideoInteractionStatus(bvid)) {
                is VideoInteractionResult.Success -> {
                    val status = result.status
                    
                    // 查找是否已有记录
                    val existingRecord = findByPlayerAndVideo(playerUuid, bvid)
                    
                    if (existingRecord != null) {
                        // 更新现有记录
                        existingRecord.updateInteractionStatus(
                            isLiked = status.tripleAction.isLiked,
                            isCoined = status.tripleAction.isCoined,
                            coinCount = status.tripleAction.coinCount,
                            isFavorited = status.tripleAction.isFavorited,
                            isFollowingUp = status.isFollowingUp,
                            hasCommented = status.hasCommented
                        )
                        dao.update(existingRecord)
                        RecordResult.Updated("互动记录已更新", existingRecord)
                    } else {
                        // 创建新记录
                        val newRecord = VideoInteractionRecord(
                            playerUuid = playerUuid,
                            bilibiliUid = playerBilibili.bilibiliUid,
                            videoBvid = bvid,
                            videoAid = status.videoData.aid,
                            videoTitle = status.videoData.title,
                            upMid = status.videoData.ownerMid,
                            isLiked = status.tripleAction.isLiked,
                            isCoined = status.tripleAction.isCoined,
                            coinCount = status.tripleAction.coinCount,
                            isFavorited = status.tripleAction.isFavorited,
                            isFollowingUp = status.isFollowingUp,
                            hasCommented = status.hasCommented
                        )
                        dao.create(newRecord)
                        RecordResult.Created("互动记录已创建", newRecord)
                    }
                }
                is VideoInteractionResult.Error -> {
                    RecordResult.NetworkError("获取互动状态失败: ${result.message}")
                }
            }
        } catch (e: SQLException) {
            throw DatabaseServiceException("记录互动状态失败", e)
        } catch (e: Exception) {
            RecordResult.Error("处理互动记录时发生异常: ${e.message}")
        }
    }
    
    /**
     * 批量记录多个视频的互动状态
     */
    fun recordMultipleInteractions(playerUuid: String, bvids: List<String>): BatchRecordResult {
        val results = mutableMapOf<String, RecordResult>()
        var successCount = 0
        var errorCount = 0
        
        bvids.forEach { bvid ->
            val result = recordInteraction(playerUuid, bvid)
            results[bvid] = result
            
            when (result) {
                is RecordResult.Created, is RecordResult.Updated -> successCount++
                else -> errorCount++
            }
            
            // 避免请求过于频繁，稍作延迟
            Thread.sleep(1000)
        }
        
        return BatchRecordResult(
            totalCount = bvids.size,
            successCount = successCount,
            errorCount = errorCount,
            results = results
        )
    }
    
    /**
     * 根据玩家和视频查找记录
     */
    fun findByPlayerAndVideo(playerUuid: String, bvid: String): VideoInteractionRecord? {
        return try {
            val queryBuilder: QueryBuilder<VideoInteractionRecord, Long> = dao.queryBuilder()
            queryBuilder.where()
                .eq(VideoInteractionRecord.FIELD_PLAYER_UUID, playerUuid)
                .and()
                .eq(VideoInteractionRecord.FIELD_VIDEO_BVID, bvid)
            
            queryBuilder.queryForFirst()
        } catch (e: SQLException) {
            throw DatabaseServiceException("查询互动记录失败", e)
        }
    }
    
    /**
     * 获取玩家的所有互动记录
     */
    fun getPlayerInteractions(playerUuid: String, limit: Int = 50): List<VideoInteractionRecord> {
        return try {
            val queryBuilder: QueryBuilder<VideoInteractionRecord, Long> = dao.queryBuilder()
            queryBuilder.where().eq(VideoInteractionRecord.FIELD_PLAYER_UUID, playerUuid)
            queryBuilder.orderBy(VideoInteractionRecord.FIELD_CHECK_TIME, false)
            queryBuilder.limit(limit.toLong())
            
            queryBuilder.query()
        } catch (e: SQLException) {
            throw DatabaseServiceException("查询玩家互动记录失败", e)
        }
    }
    
    /**
     * 获取视频的所有互动记录
     */
    fun getVideoInteractions(bvid: String): List<VideoInteractionRecord> {
        return try {
            val queryBuilder: QueryBuilder<VideoInteractionRecord, Long> = dao.queryBuilder()
            queryBuilder.where().eq(VideoInteractionRecord.FIELD_VIDEO_BVID, bvid)
            queryBuilder.orderBy(VideoInteractionRecord.FIELD_CHECK_TIME, false)
            
            queryBuilder.query()
        } catch (e: SQLException) {
            throw DatabaseServiceException("查询视频互动记录失败", e)
        }
    }
    
    /**
     * 获取UP主的互动统计
     */
    fun getUpStatistics(upMid: Long): UpStatistics {
        return try {
            val totalVideos = dao.queryBuilder()
                .where().eq(VideoInteractionRecord.FIELD_UP_MID, upMid)
                .countOf()
            
            val likedVideos = dao.queryBuilder()
                .where()
                .eq(VideoInteractionRecord.FIELD_UP_MID, upMid)
                .and()
                .eq(VideoInteractionRecord.FIELD_IS_LIKED, true)
                .countOf()
            
            val coinedVideos = dao.queryBuilder()
                .where()
                .eq(VideoInteractionRecord.FIELD_UP_MID, upMid)
                .and()
                .eq(VideoInteractionRecord.FIELD_IS_COINED, true)
                .countOf()
            
            val favoritedVideos = dao.queryBuilder()
                .where()
                .eq(VideoInteractionRecord.FIELD_UP_MID, upMid)
                .and()
                .eq(VideoInteractionRecord.FIELD_IS_FAVORITED, true)
                .countOf()
            
            val followingCount = dao.queryBuilder()
                .where()
                .eq(VideoInteractionRecord.FIELD_UP_MID, upMid)
                .and()
                .eq(VideoInteractionRecord.FIELD_IS_FOLLOWING_UP, true)
                .countOf()
            
            val commentedVideos = dao.queryBuilder()
                .where()
                .eq(VideoInteractionRecord.FIELD_UP_MID, upMid)
                .and()
                .eq(VideoInteractionRecord.FIELD_HAS_COMMENTED, true)
                .countOf()
            
            UpStatistics(
                upMid = upMid,
                totalVideos = totalVideos,
                likedVideos = likedVideos,
                coinedVideos = coinedVideos,
                favoritedVideos = favoritedVideos,
                followingCount = followingCount,
                commentedVideos = commentedVideos
            )
            
        } catch (e: SQLException) {
            throw DatabaseServiceException("获取UP主统计信息失败", e)
        }
    }
    
    /**
     * 获取玩家的互动统计
     */
    fun getPlayerStatistics(playerUuid: String): PlayerStatistics {
        return try {
            val totalVideos = dao.queryBuilder()
                .where().eq(VideoInteractionRecord.FIELD_PLAYER_UUID, playerUuid)
                .countOf()
            
            val likedVideos = dao.queryBuilder()
                .where()
                .eq(VideoInteractionRecord.FIELD_PLAYER_UUID, playerUuid)
                .and()
                .eq(VideoInteractionRecord.FIELD_IS_LIKED, true)
                .countOf()
            
            val coinedVideos = dao.queryBuilder()
                .where()
                .eq(VideoInteractionRecord.FIELD_PLAYER_UUID, playerUuid)
                .and()
                .eq(VideoInteractionRecord.FIELD_IS_COINED, true)
                .countOf()
            
            val favoritedVideos = dao.queryBuilder()
                .where()
                .eq(VideoInteractionRecord.FIELD_PLAYER_UUID, playerUuid)
                .and()
                .eq(VideoInteractionRecord.FIELD_IS_FAVORITED, true)
                .countOf()
            
            val commentedVideos = dao.queryBuilder()
                .where()
                .eq(VideoInteractionRecord.FIELD_PLAYER_UUID, playerUuid)
                .and()
                .eq(VideoInteractionRecord.FIELD_HAS_COMMENTED, true)
                .countOf()
            
            // 计算三连完成的视频数量
            val tripleCompletedVideos = dao.queryBuilder()
                .where()
                .eq(VideoInteractionRecord.FIELD_PLAYER_UUID, playerUuid)
                .and()
                .eq(VideoInteractionRecord.FIELD_IS_LIKED, true)
                .and()
                .eq(VideoInteractionRecord.FIELD_IS_COINED, true)
                .and()
                .eq(VideoInteractionRecord.FIELD_IS_FAVORITED, true)
                .countOf()
            
            PlayerStatistics(
                playerUuid = playerUuid,
                totalVideos = totalVideos,
                likedVideos = likedVideos,
                coinedVideos = coinedVideos,
                favoritedVideos = favoritedVideos,
                commentedVideos = commentedVideos,
                tripleCompletedVideos = tripleCompletedVideos
            )
            
        } catch (e: SQLException) {
            throw DatabaseServiceException("获取玩家统计信息失败", e)
        }
    }
    
    /**
     * 获取最近的互动记录
     */
    fun getRecentInteractions(limit: Int = 20): List<VideoInteractionRecord> {
        return try {
            val queryBuilder: QueryBuilder<VideoInteractionRecord, Long> = dao.queryBuilder()
            queryBuilder.orderBy(VideoInteractionRecord.FIELD_CHECK_TIME, false)
            queryBuilder.limit(limit.toLong())
            
            queryBuilder.query()
        } catch (e: SQLException) {
            throw DatabaseServiceException("查询最近互动记录失败", e)
        }
    }
    
    /**
     * 清理旧的互动记录
     */
    fun cleanupOldRecords(daysOld: Int = 30): Int {
        return try {
            val cutoffDate = Date(System.currentTimeMillis() - daysOld * 24 * 60 * 60 * 1000L)
            
            val deleteBuilder = dao.deleteBuilder()
            deleteBuilder.where().lt(VideoInteractionRecord.FIELD_CREATED_TIME, cutoffDate)
            
            deleteBuilder.delete()
        } catch (e: SQLException) {
            throw DatabaseServiceException("清理旧记录失败", e)
        }
    }
}

/**
 * 记录结果
 */
sealed class RecordResult {
    data class Created(val message: String, val record: VideoInteractionRecord) : RecordResult()
    data class Updated(val message: String, val record: VideoInteractionRecord) : RecordResult()
    data class NotBound(val message: String) : RecordResult()
    data class NetworkError(val message: String) : RecordResult()
    data class Error(val message: String) : RecordResult()
}

/**
 * 批量记录结果
 */
data class BatchRecordResult(
    val totalCount: Int,
    val successCount: Int,
    val errorCount: Int,
    val results: Map<String, RecordResult>
)

/**
 * UP主统计信息
 */
data class UpStatistics(
    val upMid: Long,
    val totalVideos: Long,
    val likedVideos: Long,
    val coinedVideos: Long,
    val favoritedVideos: Long,
    val followingCount: Long,
    val commentedVideos: Long
)

/**
 * 玩家统计信息
 */
data class PlayerStatistics(
    val playerUuid: String,
    val totalVideos: Long,
    val likedVideos: Long,
    val coinedVideos: Long,
    val favoritedVideos: Long,
    val commentedVideos: Long,
    val tripleCompletedVideos: Long
) 