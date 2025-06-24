package online.bingzi.bilibili.video.pro.api.event

import online.bingzi.bilibili.video.pro.api.entity.BilibiliUser
import online.bingzi.bilibili.video.pro.api.entity.BilibiliVideo
import online.bingzi.bilibili.video.pro.api.entity.TripleActionData
import org.bukkit.entity.Player

/**
 * Bilibili奖励事件
 *
 * 当系统准备向玩家发放Bilibili相关奖励时触发此事件
 * 该事件可被其他插件监听，用于自定义奖励逻辑或额外奖励
 *
 * @property player 接收奖励的Minecraft玩家
 * @property user 对应的Bilibili用户
 * @property video 相关的视频（如果有）
 * @property actionData 触发奖励的操作数据（如果有）
 * @property rewardType 奖励类型
 * @property rewardValue 奖励数值
 * @property rewardDescription 奖励描述
 * @property timestamp 奖励时间戳
 *
 * @constructor 创建一个Bilibili奖励事件
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
class BilibiliRewardEvent(
    /**
     * 接收奖励的Minecraft玩家
     *
     * 已绑定Bilibili账号的玩家
     */
    val player: Player,

    /**
     * 对应的Bilibili用户
     *
     * 玩家绑定的Bilibili账号信息
     */
    override val user: BilibiliUser,

    /**
     * 相关的视频
     *
     * 触发奖励的视频信息，某些奖励类型可能为null
     */
    val video: BilibiliVideo? = null,

    /**
     * 触发奖励的操作数据
     *
     * 导致奖励发放的具体操作，某些奖励类型可能为null
     */
    val actionData: TripleActionData? = null,

    /**
     * 奖励类型
     *
     * 描述奖励的种类，如"完整三连"、"部分三连"、"绑定奖励"等
     */
    val rewardType: RewardType,

    /**
     * 奖励数值
     *
     * 具体的奖励数量，含义取决于奖励类型
     */
    val rewardValue: Double,

    /**
     * 奖励描述
     *
     * 奖励的文字描述，用于显示给玩家
     */
    val rewardDescription: String,

    /**
     * 奖励时间戳
     *
     * 记录奖励发放的时间
     */
    timestamp: Long = System.currentTimeMillis()
) : BilibiliEvent(user, timestamp) {

    /**
     * 奖励类型枚举
     *
     * 定义了所有可能的奖励类型
     */
    enum class RewardType(
        /**
         * 类型名称
         */
        val typeName: String,

        /**
         * 类型描述
         */
        val description: String
    ) {
        /**
         * 绑定奖励
         *
         * 玩家首次绑定Bilibili账号时的奖励
         */
        BIND_REWARD("绑定奖励", "首次绑定Bilibili账号的奖励"),

        /**
         * 点赞奖励
         *
         * 对视频点赞时的奖励
         */
        LIKE_REWARD("点赞奖励", "对视频点赞的奖励"),

        /**
         * 投币奖励
         *
         * 对视频投币时的奖励
         */
        COIN_REWARD("投币奖励", "对视频投币的奖励"),

        /**
         * 收藏奖励
         *
         * 收藏视频时的奖励
         */
        FAVORITE_REWARD("收藏奖励", "收藏视频的奖励"),

        /**
         * 部分三连奖励
         *
         * 完成部分三连操作时的奖励
         */
        PARTIAL_TRIPLE_REWARD("部分三连奖励", "完成部分三连操作的奖励"),

        /**
         * 完整三连奖励
         *
         * 完成完整三连操作时的奖励
         */
        FULL_TRIPLE_REWARD("完整三连奖励", "完成完整三连操作的奖励"),

        /**
         * 特殊奖励
         *
         * 特殊情况下的额外奖励，如对特定UP主的支持等
         */
        SPECIAL_REWARD("特殊奖励", "特殊情况下的额外奖励"),

        /**
         * 连续奖励
         *
         * 连续进行三连操作的奖励
         */
        STREAK_REWARD("连续奖励", "连续进行三连操作的奖励")
    }

    /**
     * 检查是否为经济奖励
     *
     * @return true表示奖励涉及经济系统
     */
    fun isEconomicReward(): Boolean {
        return rewardValue > 0 && (rewardDescription.contains("金币") ||
                rewardDescription.contains("硬币") ||
                rewardDescription.contains("经验"))
    }

    /**
     * 检查是否为物品奖励
     *
     * @return true表示奖励涉及游戏物品
     */
    fun isItemReward(): Boolean {
        return rewardDescription.contains("物品") || rewardDescription.contains("道具")
    }

    /**
     * 获取奖励摘要
     *
     * @return 奖励的简短摘要
     */
    fun getRewardSummary(): String {
        val videoInfo = video?.let { " (视频: ${it.title})" } ?: ""
        return "${rewardType.typeName}: $rewardDescription$videoInfo"
    }

    /**
     * 计算奖励优先级
     *
     * 用于多个奖励同时触发时的排序
     *
     * @return 奖励优先级，数值越大优先级越高
     */
    fun getRewardPriority(): Int {
        return when (rewardType) {
            RewardType.FULL_TRIPLE_REWARD -> 10
            RewardType.SPECIAL_REWARD -> 9
            RewardType.STREAK_REWARD -> 8
            RewardType.PARTIAL_TRIPLE_REWARD -> 7
            RewardType.COIN_REWARD -> 6
            RewardType.FAVORITE_REWARD -> 5
            RewardType.LIKE_REWARD -> 4
            RewardType.BIND_REWARD -> 3
        }
    }

    override fun getEventDescription(): String {
        return "玩家 ${player.name} 获得${rewardType.typeName}：$rewardDescription"
    }

    /**
     * 获取详细的奖励信息
     *
     * @return 包含所有奖励详情的信息
     */
    fun getDetailedRewardInfo(): String {
        return buildString {
            appendLine("=== Bilibili奖励详情 ===")
            appendLine("玩家：${player.name}")
            appendLine("Bilibili：${user.nickname} (${user.uid})")
            appendLine("奖励类型：${rewardType.typeName}")
            appendLine("奖励描述：$rewardDescription")
            appendLine("奖励数值：$rewardValue")
            video?.let {
                appendLine("相关视频：${it.title}")
                appendLine("视频作者：${it.authorName}")
            }
            actionData?.let {
                appendLine("操作详情：${it.getActionDescription()}")
                appendLine("操作得分：${it.getScore()}")
            }
            appendLine("发放时间：${getFormattedTime()}")
        }
    }

    override fun toString(): String {
        return "BilibiliRewardEvent(${player.name} -> ${rewardType.typeName}: $rewardValue)"
    }
} 