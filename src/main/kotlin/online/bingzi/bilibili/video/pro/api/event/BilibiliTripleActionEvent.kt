package online.bingzi.bilibili.video.pro.api.event

import online.bingzi.bilibili.video.pro.api.entity.BilibiliUser
import online.bingzi.bilibili.video.pro.api.entity.BilibiliVideo
import online.bingzi.bilibili.video.pro.api.entity.TripleActionData

/**
 * Bilibili三连动作事件
 *
 * 当用户对视频进行三连操作（点赞、投币、收藏）时触发此事件
 * 该事件包含详细的操作信息，用于奖励系统的判断和处理
 *
 * @property user 执行操作的Bilibili用户
 * @property video 被操作的视频信息
 * @property actionData 具体的操作数据
 * @property previousActionData 操作前的数据，用于判断新增的操作
 * @property timestamp 操作时间戳
 *
 * @constructor 创建一个Bilibili三连动作事件
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
class BilibiliTripleActionEvent(
    /**
     * 执行操作的Bilibili用户
     *
     * 包含用户的基本信息和绑定状态
     */
    override val user: BilibiliUser,

    /**
     * 被操作的视频信息
     *
     * 包含视频的基本信息如标题、作者等
     */
    val video: BilibiliVideo,

    /**
     * 当前的操作数据
     *
     * 包含用户对该视频的所有操作状态
     */
    val actionData: TripleActionData,

    /**
     * 操作前的数据
     *
     * 用于比较和判断本次新增的操作类型
     * null表示这是用户对该视频的首次操作
     */
    val previousActionData: TripleActionData? = null,

    /**
     * 操作时间戳
     *
     * 记录操作的具体时间
     */
    timestamp: Long = System.currentTimeMillis()
) : BilibiliEvent(user, timestamp) {

    /**
     * 获取新增的操作类型
     *
     * 通过比较当前数据和之前数据，确定本次新增的操作
     *
     * @return 新增操作的列表，可能包含"点赞"、"投币"、"收藏"
     */
    fun getNewActions(): List<String> {
        val newActions = mutableListOf<String>()
        val previous = previousActionData

        if (previous == null) {
            // 首次操作，所有当前操作都是新增的
            if (actionData.liked) newActions.add("点赞")
            if (actionData.coined) newActions.add("投币")
            if (actionData.favorited) newActions.add("收藏")
        } else {
            // 比较变化
            if (actionData.liked && !previous.liked) newActions.add("点赞")
            if (actionData.coined && !previous.coined) newActions.add("投币")
            if (actionData.favorited && !previous.favorited) newActions.add("收藏")
        }

        return newActions
    }

    /**
     * 检查是否刚完成完整三连
     *
     * @return true表示本次操作完成了完整三连
     */
    fun isJustCompletedFullTriple(): Boolean {
        val wasComplete = previousActionData?.isFullTriple() ?: false
        return actionData.isFullTriple() && !wasComplete
    }

    /**
     * 检查是否刚完成部分三连
     *
     * @return true表示本次操作完成了部分三连（但不是完整三连）
     */
    fun isJustCompletedPartialTriple(): Boolean {
        val wasPartial = previousActionData?.isPartialTriple() ?: false
        return actionData.isPartialTriple() && !wasPartial && !actionData.isFullTriple()
    }

    /**
     * 获取新增的得分
     *
     * @return 本次操作新增的得分
     */
    fun getNewScore(): Int {
        val previousScore = previousActionData?.getScore() ?: 0
        return actionData.getScore() - previousScore
    }

    /**
     * 检查视频是否属于特定作者
     *
     * @param authorUid 作者UID
     * @return true表示视频属于指定作者
     */
    fun isVideoByAuthor(authorUid: String): Boolean = video.authorUid == authorUid

    /**
     * 检查是否为最近上传的视频
     *
     * @return true表示视频是最近上传的
     */
    fun isRecentVideo(): Boolean = video.isRecentlyUploaded()

    override fun getEventDescription(): String {
        val newActions = getNewActions().joinToString("、")
        return "用户 ${user.nickname} 对视频《${video.title}》进行了：$newActions"
    }

    /**
     * 获取详细的操作报告
     *
     * @return 包含所有操作详情的报告
     */
    fun getDetailedReport(): String {
        return buildString {
            appendLine("=== Bilibili三连操作报告 ===")
            appendLine("用户：${user.nickname} (${user.uid})")
            appendLine("视频：${video.title}")
            appendLine("作者：${video.authorName}")
            appendLine("操作：${actionData.getActionDescription()}")
            appendLine("新增：${getNewActions().joinToString("、")}")
            appendLine("得分：${actionData.getScore()} (+${getNewScore()})")
            appendLine(
                "状态：${
                    when {
                        isJustCompletedFullTriple() -> "完成完整三连！"
                        isJustCompletedPartialTriple() -> "完成部分三连"
                        else -> "部分操作"
                    }
                }"
            )
            appendLine("时间：${getFormattedTime()}")
        }
    }

    override fun toString(): String {
        return "BilibiliTripleActionEvent(${user.nickname} -> ${video.bvid}, 新增: ${getNewActions().joinToString(",")})"
    }
} 