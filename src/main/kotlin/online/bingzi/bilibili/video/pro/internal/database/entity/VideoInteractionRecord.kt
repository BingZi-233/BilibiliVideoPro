package online.bingzi.bilibili.video.pro.internal.database.entity

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import online.bingzi.bilibili.video.pro.internal.config.DatabaseConfig
import java.util.*

/**
 * 视频互动记录实体
 * 存储玩家对视频的互动状态记录
 */
@DatabaseTable(tableName = VideoInteractionRecord.TABLE_NAME)
data class VideoInteractionRecord(
    /**
     * 主键ID
     */
    @DatabaseField(generatedId = true, columnName = ID)
    var id: Long = 0,
    /**
     * 玩家UUID
     */
    @DatabaseField(columnName = PLAYER_UUID, canBeNull = false, index = true, width = 36)
    var playerUuid: String = "",
    /**
     * Bilibili用户ID
     */
    @DatabaseField(columnName = BILIBILI_UID, canBeNull = false, index = true)
    var bilibiliUid: Long = 0,
    /**
     * 视频BVID
     */
    @DatabaseField(columnName = VIDEO_BVID, canBeNull = false, index = true, width = 20)
    var videoBvid: String = "",
    /**
     * 视频AID
     */
    @DatabaseField(columnName = VIDEO_AID, canBeNull = false, index = true)
    var videoAid: Long = 0,
    /**
     * 视频标题
     */
    @DatabaseField(columnName = VIDEO_TITLE, canBeNull = true, width = 500)
    var videoTitle: String? = null,
    /**
     * UP主MID
     */
    @DatabaseField(columnName = UP_MID, canBeNull = false, index = true)
    var upMid: Long = 0,
    /**
     * 是否点赞
     */
    @DatabaseField(columnName = IS_LIKED, canBeNull = false)
    var isLiked: Boolean = false,
    /**
     * 是否投币
     */
    @DatabaseField(columnName = IS_COINED, canBeNull = false)
    var isCoined: Boolean = false,
    /**
     * 投币数量
     */
    @DatabaseField(columnName = COIN_COUNT, canBeNull = false)
    var coinCount: Int = 0,
    /**
     * 是否收藏
     */
    @DatabaseField(columnName = IS_FAVORITED, canBeNull = false)
    var isFavorited: Boolean = false,
    /**
     * 是否关注UP主
     */
    @DatabaseField(columnName = IS_FOLLOWING_UP, canBeNull = false)
    var isFollowingUp: Boolean = false,
    /**
     * 是否评论
     */
    @DatabaseField(columnName = HAS_COMMENTED, canBeNull = false)
    var hasCommented: Boolean = false,
    /**
     * 检查时间
     */
    @DatabaseField(columnName = CHECK_TIME, canBeNull = false)
    var checkTime: Date = Date(),
    /**
     * 创建时间
     */
    @DatabaseField(columnName = CREATED_TIME, canBeNull = false)
    var createdTime: Date = Date(),
    /**
     * 更新时间
     */
    @DatabaseField(columnName = UPDATED_TIME, canBeNull = false)
    var updatedTime: Date = Date()
) {
    // ORMLite requires a no-arg constructor.
    // By providing default values for all properties in the data class's primary constructor,
    // the Kotlin compiler generates a no-arg constructor automatically.
    companion object {
        const val TABLE_NAME = "video_interaction_record"
        const val ID = "id"
        const val PLAYER_UUID = "player_uuid"
        const val BILIBILI_UID = "bilibili_uid"
        const val VIDEO_BVID = "video_bvid"
        const val VIDEO_AID = "video_aid"
        const val VIDEO_TITLE = "video_title"
        const val UP_MID = "up_mid"
        const val IS_LIKED = "is_liked"
        const val IS_COINED = "is_coined"
        const val COIN_COUNT = "coin_count"
        const val IS_FAVORITED = "is_favorited"
        const val IS_FOLLOWING_UP = "is_following_up"
        const val HAS_COMMENTED = "has_commented"
        const val CHECK_TIME = "check_time"
        const val CREATED_TIME = "created_time"
        const val UPDATED_TIME = "updated_time"

        /**
         * 获取完整的表名（包含前缀）
         */
        fun getFullTableName(): String {
            return DatabaseConfig.getTableName(TABLE_NAME)
        }
    }

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
    ) : this(
        playerUuid = playerUuid,
        bilibiliUid = bilibiliUid,
        videoBvid = videoBvid,
        videoAid = videoAid,
        videoTitle = videoTitle,
        upMid = upMid,
        isLiked = isLiked,
        isCoined = isCoined,
        coinCount = coinCount,
        isFavorited = isFavorited,
        isFollowingUp = isFollowingUp,
        hasCommented = hasCommented,
        checkTime = Date(),
        createdTime = Date(),
        updatedTime = Date()
    )

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
} 