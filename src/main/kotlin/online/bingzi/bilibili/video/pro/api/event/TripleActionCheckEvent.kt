package online.bingzi.bilibili.video.pro.api.event

import org.bukkit.entity.Player

/**
 * 一键三联检查事件
 * 
 * 当玩家检查指定视频的一键三联状态时触发此事件。
 * 包含检查结果的详细信息，可以用于统计、日志记录或触发后续逻辑。
 * 
 * @param player 执行检查的玩家
 * @param bvid 被检查的视频BV号
 * @param isLiked 是否已点赞
 * @param isCoined 是否已投币
 * @param isFavorited 是否已收藏
 * 
 * @author BilibiliVideoPro
 * @since 2.0.0
 */
class TripleActionCheckEvent(
    /**
     * 执行检查的玩家
     */
    val player: Player,
    
    /**
     * 被检查的视频BV号
     * 格式通常为 "BV" + 10位字符
     */
    val bvid: String,
    
    /**
     * 是否已点赞
     */
    val isLiked: Boolean,
    
    /**
     * 是否已投币
     */
    val isCoined: Boolean,
    
    /**
     * 是否已收藏
     */
    val isFavorited: Boolean
) : BilibiliVideoProxyEvent()