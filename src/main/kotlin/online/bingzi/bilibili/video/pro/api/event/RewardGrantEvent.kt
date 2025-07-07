package online.bingzi.bilibili.video.pro.api.event

import online.bingzi.bilibili.video.pro.api.entity.event.RewardType
import org.bukkit.entity.Player

/**
 * 奖励发放事件
 *
 * 当系统为玩家发放奖励时触发此事件。
 * 可以用于记录奖励历史、统计分析或触发额外的奖励逻辑。
 *
 * @param player 接收奖励的玩家
 * @param bvid 相关的视频BV号
 * @param rewardType 奖励类型
 * @param rewardAmount 奖励数量，对于某些类型（如脚本）可能为0
 * @param rewardDescription 奖励描述信息
 *
 * @author BilibiliVideoPro
 * @since 2.0.0
 */
class RewardGrantEvent(
    /**
     * 接收奖励的玩家
     */
    val player: Player,

    /**
     * 相关的视频BV号
     * 触发奖励的源视频
     */
    val bvid: String,

    /**
     * 奖励类型
     * @see RewardType
     */
    val rewardType: RewardType,

    /**
     * 奖励数量
     * 对于金钱、经验等数值类奖励表示具体数量
     * 对于脚本、命令类奖励此值可能为0
     */
    val rewardAmount: Int = 0,

    /**
     * 奖励描述信息
     * 包含奖励的具体内容描述，如脚本内容、物品名称等
     */
    val rewardDescription: String = ""
) : BilibiliVideoProxyEvent()