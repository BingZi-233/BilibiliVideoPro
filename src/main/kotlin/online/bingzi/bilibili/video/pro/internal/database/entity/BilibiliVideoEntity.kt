package online.bingzi.bilibili.video.pro.internal.database.entity

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

/**
 * Bilibili视频数据库实体
 *
 * 使用ORMLite注解进行数据库映射
 * 对应数据库表：bilibili_videos
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
@DatabaseTable(tableName = "bilibili_videos")
class BilibiliVideoEntity {

    /**
     * 主键ID（自增）
     */
    @DatabaseField(generatedId = true, columnName = "id")
    var id: Long = 0

    /**
     * 视频AV号（唯一索引）
     */
    @DatabaseField(canBeNull = false, unique = true, columnName = "aid", index = true)
    var aid: Long = 0

    /**
     * 视频BV号（唯一索引）
     */
    @DatabaseField(canBeNull = false, unique = true, columnName = "bvid", index = true)
    var bvid: String = ""

    /**
     * 视频标题
     */
    @DatabaseField(canBeNull = false, columnName = "title")
    var title: String = ""

    /**
     * 作者UID
     */
    @DatabaseField(canBeNull = false, columnName = "author_uid", index = true)
    var authorUid: String = ""

    /**
     * 作者昵称
     */
    @DatabaseField(canBeNull = false, columnName = "author_name")
    var authorName: String = ""

    /**
     * 视频上传时间（毫秒时间戳）
     */
    @DatabaseField(canBeNull = false, columnName = "upload_time", index = true)
    var uploadTime: Long = 0

    /**
     * 视频封面URL
     */
    @DatabaseField(columnName = "cover_url")
    var coverUrl: String? = null

    /**
     * 视频描述
     */
    @DatabaseField(columnName = "description")
    var description: String? = null

    /**
     * 视频时长（秒）
     */
    @DatabaseField(columnName = "duration")
    var duration: Int = 0

    /**
     * 播放量
     */
    @DatabaseField(columnName = "view_count")
    var viewCount: Int = 0

    /**
     * 弹幕数
     */
    @DatabaseField(columnName = "danmaku_count")
    var danmakuCount: Int = 0

    /**
     * 评论数
     */
    @DatabaseField(columnName = "reply_count")
    var replyCount: Int = 0

    /**
     * 点赞数
     */
    @DatabaseField(columnName = "like_count")
    var likeCount: Int = 0

    /**
     * 投币数
     */
    @DatabaseField(columnName = "coin_count")
    var coinCount: Int = 0

    /**
     * 收藏数
     */
    @DatabaseField(columnName = "favorite_count")
    var favoriteCount: Int = 0

    /**
     * 分享数
     */
    @DatabaseField(columnName = "share_count")
    var shareCount: Int = 0

    /**
     * 视频分区ID
     */
    @DatabaseField(columnName = "category_id")
    var categoryId: Int = 0

    /**
     * 视频分区名称
     */
    @DatabaseField(columnName = "category_name")
    var categoryName: String? = null

    /**
     * 视频标签（JSON数组格式）
     */
    @DatabaseField(columnName = "tags")
    var tags: String? = null

    /**
     * 是否为原创视频（1：原创，0：转载）
     */
    @DatabaseField(columnName = "is_original")
    var isOriginal: Int = 1

    /**
     * 视频状态（1：正常，0：删除/私密）
     */
    @DatabaseField(canBeNull = false, columnName = "status")
    var status: Int = 1

    /**
     * 扩展数据（JSON格式）
     */
    @DatabaseField(columnName = "extra_data")
    var extraData: String? = null

    /**
     * 数据创建时间（毫秒时间戳）
     */
    @DatabaseField(canBeNull = false, columnName = "create_time")
    var createTime: Long = 0

    /**
     * 最后更新时间（毫秒时间戳）
     */
    @DatabaseField(canBeNull = false, columnName = "last_update")
    var lastUpdate: Long = 0

    /**
     * 无参构造函数（ORMLite要求）
     */
    constructor()

    /**
     * 构造函数
     *
     * @param aid 视频AV号
     * @param bvid 视频BV号
     * @param title 视频标题
     * @param authorUid 作者UID
     * @param authorName 作者昵称
     * @param uploadTime 上传时间
     */
    constructor(
        aid: Long,
        bvid: String,
        title: String,
        authorUid: String,
        authorName: String,
        uploadTime: Long
    ) {
        this.aid = aid
        this.bvid = bvid
        this.title = title
        this.authorUid = authorUid
        this.authorName = authorName
        this.uploadTime = uploadTime
        val now = System.currentTimeMillis()
        this.createTime = now
        this.lastUpdate = now
    }

    /**
     * 更新最后更新时间
     */
    fun updateLastUpdateTime() {
        this.lastUpdate = System.currentTimeMillis()
    }

    /**
     * 是否为正常状态
     *
     * @return true表示正常状态
     */
    fun isActive(): Boolean {
        return status == 1
    }

    /**
     * 设置视频状态
     *
     * @param active true为正常，false为删除/私密
     */
    fun setActive(active: Boolean) {
        this.status = if (active) 1 else 0
        updateLastUpdateTime()
    }

    /**
     * 是否为原创视频
     *
     * @return true表示原创
     */
    fun isOriginalVideo(): Boolean {
        return isOriginal == 1
    }

    /**
     * 设置是否为原创视频
     *
     * @param original true为原创，false为转载
     */
    fun setOriginalVideo(original: Boolean) {
        this.isOriginal = if (original) 1 else 0
        updateLastUpdateTime()
    }

    /**
     * 获取视频URL
     *
     * @return 视频链接
     */
    fun getVideoUrl(): String {
        return "https://www.bilibili.com/video/$bvid"
    }

    /**
     * 是否为最近上传的视频
     *
     * @param hours 小时数（默认24小时）
     * @return true表示是最近上传的
     */
    fun isRecentlyUploaded(hours: Int = 24): Boolean {
        val hoursInMillis = hours * 60 * 60 * 1000L
        return System.currentTimeMillis() - uploadTime < hoursInMillis
    }

    /**
     * 获取总互动数（点赞+投币+收藏+分享）
     *
     * @return 总互动数
     */
    fun getTotalInteractions(): Int {
        return likeCount + coinCount + favoriteCount + shareCount
    }

    /**
     * 计算视频热度分数（简单算法）
     *
     * @return 热度分数
     */
    fun getHeatScore(): Double {
        val daysSinceUpload = (System.currentTimeMillis() - uploadTime) / (24 * 60 * 60 * 1000.0)
        if (daysSinceUpload <= 0) return 0.0

        // 热度 = (播放量*0.1 + 点赞数*2 + 投币数*3 + 收藏数*2 + 分享数*4) / 上传天数
        val interactionScore = viewCount * 0.1 + likeCount * 2 + coinCount * 3 + favoriteCount * 2 + shareCount * 4
        return interactionScore / daysSinceUpload
    }

    override fun toString(): String {
        return "BilibiliVideoEntity(id=$id, aid=$aid, bvid='$bvid', title='$title', authorName='$authorName', uploadTime=$uploadTime)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BilibiliVideoEntity) return false
        return bvid == other.bvid
    }

    override fun hashCode(): Int {
        return bvid.hashCode()
    }
} 