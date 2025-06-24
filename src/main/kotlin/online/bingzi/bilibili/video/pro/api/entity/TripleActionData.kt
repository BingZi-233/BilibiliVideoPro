package online.bingzi.bilibili.video.pro.api.entity

/**
 * 三连动作数据
 *
 * 记录用户对视频进行三连操作（点赞、投币、收藏）的具体数据
 *
 * @property liked 是否已点赞
 * @property coined 是否已投币
 * @property coinCount 投币数量（1-2个）
 * @property favorited 是否已收藏
 * @property timestamp 操作时间戳
 *
 * @constructor 创建三连动作数据
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
data class TripleActionData(
    /**
     * 是否已点赞
     *
     * true表示用户已对该视频点赞
     */
    val liked: Boolean = false,

    /**
     * 是否已投币
     *
     * true表示用户已对该视频投币
     */
    val coined: Boolean = false,

    /**
     * 投币数量
     *
     * 用户对该视频投币的数量，范围为0-2
     * 0表示未投币，1或2表示投币数量
     */
    val coinCount: Int = 0,

    /**
     * 是否已收藏
     *
     * true表示用户已收藏该视频
     */
    val favorited: Boolean = false,

    /**
     * 操作时间戳
     *
     * 记录最后一次操作的时间
     */
    val timestamp: Long = System.currentTimeMillis()
) {

    /**
     * 检查是否完成完整三连
     *
     * 完整三连要求：点赞 + 投币（至少1个） + 收藏
     *
     * @return true表示完成了完整三连
     */
    fun isFullTriple(): Boolean = liked && coined && favorited

    /**
     * 检查是否完成部分三连
     *
     * 部分三连要求：至少完成两项操作
     *
     * @return true表示完成了部分三连
     */
    fun isPartialTriple(): Boolean {
        val actions = listOf(liked, coined, favorited)
        return actions.count { it } >= 2
    }

    /**
     * 获取完成的操作数量
     *
     * @return 完成的操作数量（0-3）
     */
    fun getCompletedActionsCount(): Int {
        return listOf(liked, coined, favorited).count { it }
    }

    /**
     * 获取操作得分
     *
     * 根据操作类型计算得分：点赞(1分) + 投币(coinCount分) + 收藏(1分)
     *
     * @return 总得分
     */
    fun getScore(): Int {
        var score = 0
        if (liked) score += 1
        if (coined) score += coinCount
        if (favorited) score += 1
        return score
    }

    /**
     * 获取操作描述
     *
     * @return 描述当前完成的操作
     */
    fun getActionDescription(): String {
        val actions = mutableListOf<String>()
        if (liked) actions.add("点赞")
        if (coined) actions.add("投币${coinCount}个")
        if (favorited) actions.add("收藏")

        return if (actions.isEmpty()) "无操作" else actions.joinToString("、")
    }

    override fun toString(): String {
        return "TripleActionData(${getActionDescription()}, 得分=${getScore()})"
    }
} 