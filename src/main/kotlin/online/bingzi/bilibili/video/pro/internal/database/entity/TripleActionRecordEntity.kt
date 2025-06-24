package online.bingzi.bilibili.video.pro.internal.database.entity

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

/**
 * 三连操作记录数据库实体
 *
 * 使用ORMLite注解进行数据库映射
 * 对应数据库表：triple_action_records
 * 记录用户对视频的三连操作历史
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
@DatabaseTable(tableName = "triple_action_records")
class TripleActionRecordEntity {

    /**
     * 主键ID（自增）
     */
    @DatabaseField(generatedId = true, columnName = "id")
    var id: Long = 0

    /**
     * 用户UID
     */
    @DatabaseField(canBeNull = false, columnName = "user_uid", index = true)
    var userUid: String = ""

    /**
     * 视频BV号
     */
    @DatabaseField(canBeNull = false, columnName = "video_bvid", index = true)
    var videoBvid: String = ""

    /**
     * 视频AV号
     */
    @DatabaseField(canBeNull = false, columnName = "video_aid", index = true)
    var videoAid: Long = 0

    /**
     * 是否点赞（1：已点赞，0：未点赞）
     */
    @DatabaseField(canBeNull = false, columnName = "liked")
    var liked: Int = 0

    /**
     * 是否投币（1：已投币，0：未投币）
     */
    @DatabaseField(canBeNull = false, columnName = "coined")
    var coined: Int = 0

    /**
     * 投币数量
     */
    @DatabaseField(canBeNull = false, columnName = "coin_count")
    var coinCount: Int = 0

    /**
     * 是否收藏（1：已收藏，0：未收藏）
     */
    @DatabaseField(canBeNull = false, columnName = "favorited")
    var favorited: Int = 0

    /**
     * 操作时间（毫秒时间戳）
     */
    @DatabaseField(canBeNull = false, columnName = "action_time", index = true)
    var actionTime: Long = 0

    /**
     * 操作类型（1：新增操作，2：更新操作，3：撤销操作）
     */
    @DatabaseField(canBeNull = false, columnName = "action_type")
    var actionType: Int = 1

    /**
     * 操作来源（1：自动检测，2：手动触发，3：API同步）
     */
    @DatabaseField(canBeNull = false, columnName = "action_source")
    var actionSource: Int = 1

    /**
     * 操作前状态（JSON格式，记录变更前的状态）
     */
    @DatabaseField(columnName = "previous_state")
    var previousState: String? = null

    /**
     * 奖励状态（1：已发放，0：未发放，2：发放失败）
     */
    @DatabaseField(canBeNull = false, columnName = "reward_status")
    var rewardStatus: Int = 0

    /**
     * 奖励类型（参考BilibiliRewardEvent.RewardType）
     */
    @DatabaseField(columnName = "reward_type")
    var rewardType: String? = null

    /**
     * 奖励内容（JSON格式）
     */
    @DatabaseField(columnName = "reward_content")
    var rewardContent: String? = null

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
     * @param userUid 用户UID
     * @param videoBvid 视频BV号
     * @param videoAid 视频AV号
     * @param liked 是否点赞
     * @param coined 是否投币
     * @param coinCount 投币数量
     * @param favorited 是否收藏
     */
    constructor(
        userUid: String,
        videoBvid: String,
        videoAid: Long,
        liked: Boolean,
        coined: Boolean,
        coinCount: Int,
        favorited: Boolean
    ) {
        this.userUid = userUid
        this.videoBvid = videoBvid
        this.videoAid = videoAid
        this.liked = if (liked) 1 else 0
        this.coined = if (coined) 1 else 0
        this.coinCount = coinCount
        this.favorited = if (favorited) 1 else 0
        val now = System.currentTimeMillis()
        this.actionTime = now
        this.createTime = now
        this.lastUpdate = now
    }

    /**
     * 操作类型枚举
     */
    enum class ActionType(val code: Int, val description: String) {
        NEW(1, "新增操作"),
        UPDATE(2, "更新操作"),
        REVOKE(3, "撤销操作");

        companion object {
            fun fromCode(code: Int): ActionType? {
                return values().find { it.code == code }
            }
        }
    }

    /**
     * 操作来源枚举
     */
    enum class ActionSource(val code: Int, val description: String) {
        AUTO_DETECT(1, "自动检测"),
        MANUAL_TRIGGER(2, "手动触发"),
        API_SYNC(3, "API同步");

        companion object {
            fun fromCode(code: Int): ActionSource? {
                return values().find { it.code == code }
            }
        }
    }

    /**
     * 奖励状态枚举
     */
    enum class RewardStatus(val code: Int, val description: String) {
        NOT_REWARDED(0, "未发放"),
        REWARDED(1, "已发放"),
        REWARD_FAILED(2, "发放失败");

        companion object {
            fun fromCode(code: Int): RewardStatus? {
                return values().find { it.code == code }
            }
        }
    }

    /**
     * 更新最后更新时间
     */
    fun updateLastUpdateTime() {
        this.lastUpdate = System.currentTimeMillis()
    }

    /**
     * 是否已点赞
     *
     * @return true表示已点赞
     */
    fun isLiked(): Boolean {
        return liked == 1
    }

    /**
     * 设置点赞状态
     *
     * @param liked 是否点赞
     */
    fun setLiked(liked: Boolean) {
        this.liked = if (liked) 1 else 0
        updateLastUpdateTime()
    }

    /**
     * 是否已投币
     *
     * @return true表示已投币
     */
    fun isCoined(): Boolean {
        return coined == 1
    }

    /**
     * 设置投币状态
     *
     * @param coined 是否投币
     */
    fun setCoined(coined: Boolean) {
        this.coined = if (coined) 1 else 0
        updateLastUpdateTime()
    }

    /**
     * 是否已收藏
     *
     * @return true表示已收藏
     */
    fun isFavorited(): Boolean {
        return favorited == 1
    }

    /**
     * 设置收藏状态
     *
     * @param favorited 是否收藏
     */
    fun setFavorited(favorited: Boolean) {
        this.favorited = if (favorited) 1 else 0
        updateLastUpdateTime()
    }

    /**
     * 是否完成完整三连
     *
     * @return true表示完成完整三连
     */
    fun isFullTriple(): Boolean {
        return isLiked() && isCoined() && isFavorited()
    }

    /**
     * 是否完成部分三连
     *
     * @return true表示完成部分三连
     */
    fun isPartialTriple(): Boolean {
        val actionCount = listOf(isLiked(), isCoined(), isFavorited()).count { it }
        return actionCount in 1..2
    }

    /**
     * 获取操作类型
     *
     * @return 操作类型枚举
     */
    fun getActionType(): ActionType? {
        return ActionType.fromCode(actionType)
    }

    /**
     * 设置操作类型
     *
     * @param type 操作类型枚举
     */
    fun setActionType(type: ActionType) {
        this.actionType = type.code
        updateLastUpdateTime()
    }

    /**
     * 获取操作来源
     *
     * @return 操作来源枚举
     */
    fun getActionSource(): ActionSource? {
        return ActionSource.fromCode(actionSource)
    }

    /**
     * 设置操作来源
     *
     * @param source 操作来源枚举
     */
    fun setActionSource(source: ActionSource) {
        this.actionSource = source.code
        updateLastUpdateTime()
    }

    /**
     * 获取奖励状态
     *
     * @return 奖励状态枚举
     */
    fun getRewardStatus(): RewardStatus? {
        return RewardStatus.fromCode(rewardStatus)
    }

    /**
     * 设置奖励状态
     *
     * @param status 奖励状态枚举
     */
    fun setRewardStatus(status: RewardStatus) {
        this.rewardStatus = status.code
        updateLastUpdateTime()
    }

    /**
     * 是否已发放奖励
     *
     * @return true表示已发放奖励
     */
    fun isRewarded(): Boolean {
        return getRewardStatus() == RewardStatus.REWARDED
    }

    /**
     * 计算三连操作得分
     *
     * @param likeWeight 点赞权重
     * @param coinWeight 投币权重
     * @param favoriteWeight 收藏权重
     * @return 加权得分
     */
    fun calculateScore(
        likeWeight: Double = 1.0,
        coinWeight: Double = 2.0,
        favoriteWeight: Double = 1.5
    ): Double {
        var score = 0.0
        if (isLiked()) score += likeWeight
        if (isCoined()) score += coinCount * coinWeight
        if (isFavorited()) score += favoriteWeight
        return score
    }

    /**
     * 获取操作的唯一标识符（用户+视频）
     *
     * @return 唯一标识符
     */
    fun getUniqueKey(): String {
        return "${userUid}_${videoBvid}"
    }

    override fun toString(): String {
        return "TripleActionRecordEntity(id=$id, userUid='$userUid', videoBvid='$videoBvid', " +
                "liked=${isLiked()}, coined=${isCoined()}, coinCount=$coinCount, favorited=${isFavorited()}, " +
                "actionTime=$actionTime, rewardStatus=${getRewardStatus()})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TripleActionRecordEntity) return false
        return getUniqueKey() == other.getUniqueKey()
    }

    override fun hashCode(): Int {
        return getUniqueKey().hashCode()
    }
} 