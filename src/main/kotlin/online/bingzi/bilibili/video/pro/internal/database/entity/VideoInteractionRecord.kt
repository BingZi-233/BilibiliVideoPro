package online.bingzi.bilibili.video.pro.internal.database.entity

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import online.bingzi.bilibili.video.pro.internal.config.DatabaseConfig
import java.util.*

/**
 * 视频互动记录实体
 * 存储玩家对视频的互动状态记录
 */
@DatabaseTable(tableName = "video_interaction_record")
class VideoInteractionRecord {
    
    companion object {
        const val TABLE_NAME = "video_interaction_record"
        const val FIELD_ID = "id"
        const val FIELD_PLAYER_UUID = "player_uuid"
        const val FIELD_BILIBILI_UID = "bilibili_uid"
        const val FIELD_VIDEO_BVID = "video_bvid"
        const val FIELD_VIDEO_AID = "video_aid"
        const val FIELD_VIDEO_TITLE = "video_title"
        const val FIELD_UP_MID = "up_mid"
        const val FIELD_IS_LIKED = "is_liked"
        const val FIELD_IS_COINED = "is_coined"
        const val FIELD_COIN_COUNT = "coin_count"
        const val FIELD_IS_FAVORITED = "is_favorited"
        const val FIELD_IS_FOLLOWING_UP = "is_following_up"
        const val FIELD_HAS_COMMENTED = "has_commented"
        const val FIELD_CHECK_TIME = "check_time"
        const val FIELD_CREATED_TIME = "created_time"
        const val FIELD_UPDATED_TIME = "updated_time"
        
        /**
         * 获取完整的表名（包含前缀）
         */
        fun getFullTableName(): String {
            return DatabaseConfig.getTableName(TABLE_NAME)
        }
    }
    
    /**
     * 主键ID
     */
    @DatabaseField(generatedId = true, columnName = FIELD_ID)
    var id: Long = 0
    
    /**
     * 玩家UUID
     */
    @DatabaseField(columnName = FIELD_PLAYER_UUID, canBeNull = false, index = true, width = 36)
    var playerUuid: String = ""
    
    /**
     * Bilibili用户ID
     */
    @DatabaseField(columnName = FIELD_BILIBILI_UID, canBeNull = false, index = true)
    var bilibiliUid: Long = 0
    
    /**
     * 视频BVID
     */
    @DatabaseField(columnName = FIELD_VIDEO_BVID, canBeNull = false, index = true, width = 20)
    var videoBvid: String = ""
    
    /**
     * 视频AID
     */
    @DatabaseField(columnName = FIELD_VIDEO_AID, canBeNull = false, index = true)
    var videoAid: Long = 0
    
    /**
     * 视频标题
     */
    @DatabaseField(columnName = FIELD_VIDEO_TITLE, canBeNull = true, width = 500)
    var videoTitle: String? = null
    
    /**
     * UP主MID
     */
    @DatabaseField(columnName = FIELD_UP_MID, canBeNull = false, index = true)
    var upMid: Long = 0
    
    /**
     * 是否点赞
     */
    @DatabaseField(columnName = FIELD_IS_LIKED, canBeNull = false)
    var isLiked: Boolean = false
    
    /**
     * 是否投币
     */
    @DatabaseField(columnName = FIELD_IS_COINED, canBeNull = false)
    var isCoined: Boolean = false
    
    /**
     * 投币数量
     */
    @DatabaseField(columnName = FIELD_COIN_COUNT, canBeNull = false)
    var coinCount: Int = 0
    
    /**
     * 是否收藏
     */
    @DatabaseField(columnName = FIELD_IS_FAVORITED, canBeNull = false)
    var isFavorited: Boolean = false
    
    /**
     * 是否关注UP主
     */
    @DatabaseField(columnName = FIELD_IS_FOLLOWING_UP, canBeNull = false)
    var isFollowingUp: Boolean = false
    
    /**
     * 是否评论
     */
    @DatabaseField(columnName = FIELD_HAS_COMMENTED, canBeNull = false)
    var hasCommented: Boolean = false
    
    /**
     * 检查时间
     */
    @DatabaseField(columnName = FIELD_CHECK_TIME, canBeNull = false)
    var checkTime: Date = Date()
    
    /**
     * 创建时间
     */
    @DatabaseField(columnName = FIELD_CREATED_TIME, canBeNull = false)
    var createdTime: Date = Date()
    
    /**
     * 更新时间
     */
    @DatabaseField(columnName = FIELD_UPDATED_TIME, canBeNull = false)
    var updatedTime: Date = Date()
    
    /**
     * 默认构造函数（ORMLite需要）
     */
    constructor()
    
    /**
     * 创建新记录的构造函数
     */
    constructor(
        playerUuid: String,
        bilibiliUid: Long,
        videoBvid: String,
        videoAid: Long,
        videoTitle: String?,
        upMid: Long,
        isLiked: Boolean,
        isCoined: Boolean,
        coinCount: Int,
        isFavorited: Boolean,
        isFollowingUp: Boolean,
        hasCommented: Boolean
    ) {
        this.playerUuid = playerUuid
        this.bilibiliUid = bilibiliUid
        this.videoBvid = videoBvid
        this.videoAid = videoAid
        this.videoTitle = videoTitle
        this.upMid = upMid
        this.isLiked = isLiked
        this.isCoined = isCoined
        this.coinCount = coinCount
        this.isFavorited = isFavorited
        this.isFollowingUp = isFollowingUp
        this.hasCommented = hasCommented
        this.checkTime = Date()
        this.createdTime = Date()
        this.updatedTime = Date()
    }
    
    /**
     * 更新互动状态
     */
    fun updateInteractionStatus(
        isLiked: Boolean,
        isCoined: Boolean,
        coinCount: Int,
        isFavorited: Boolean,
        isFollowingUp: Boolean,
        hasCommented: Boolean
    ) {
        this.isLiked = isLiked
        this.isCoined = isCoined
        this.coinCount = coinCount
        this.isFavorited = isFavorited
        this.isFollowingUp = isFollowingUp
        this.hasCommented = hasCommented
        this.checkTime = Date()
        this.updatedTime = Date()
    }
    
    /**
     * 获取三连状态
     */
    fun getTripleActionStatus(): String {
        val actions = mutableListOf<String>()
        if (isLiked) actions.add("点赞")
        if (isCoined) actions.add("投币($coinCount)")
        if (isFavorited) actions.add("收藏")
        
        return if (actions.isEmpty()) "未三连" else actions.joinToString(", ")
    }
    
    /**
     * 检查是否完成三连
     */
    fun isTripleCompleted(): Boolean {
        return isLiked && isCoined && isFavorited
    }
    
    /**
     * 获取互动分数（用于统计）
     */
    fun getInteractionScore(): Int {
        var score = 0
        if (isLiked) score += 1
        if (isCoined) score += coinCount
        if (isFavorited) score += 1
        if (isFollowingUp) score += 2
        if (hasCommented) score += 1
        return score
    }
    
    override fun toString(): String {
        return "VideoInteractionRecord(id=$id, playerUuid='$playerUuid', " +
                "videoBvid='$videoBvid', videoTitle='$videoTitle', " +
                "isLiked=$isLiked, isCoined=$isCoined, coinCount=$coinCount, " +
                "isFavorited=$isFavorited, isFollowingUp=$isFollowingUp, " +
                "hasCommented=$hasCommented, checkTime=$checkTime)"
    }
} 