package online.bingzi.bilibili.video.pro.api.event

import org.bukkit.entity.Player

/**
 * 一键三联完成事件
 * 
 * 当玩家成功完成指定视频的一键三联（点赞、投币、收藏）时触发此事件。
 * 通常用于触发奖励系统、统计记录或其他后续处理。
 * 
 * @param player 完成三联的玩家
 * @param bvid 完成三联的视频BV号
 * @param videoTitle 视频标题
 * 
 * @author BilibiliVideoPro
 * @since 2.0.0
 */
class TripleActionCompleteEvent(
    /**
     * 完成三联的玩家
     */
    val player: Player,
    
    /**
     * 完成三联的视频BV号
     * 格式通常为 "BV" + 10位字符
     */
    val bvid: String,
    
    /**
     * 视频标题
     * 用于显示和日志记录
     */
    val videoTitle: String
) : BilibiliVideoProxyEvent()