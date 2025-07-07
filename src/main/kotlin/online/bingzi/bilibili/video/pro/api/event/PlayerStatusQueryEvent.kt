package online.bingzi.bilibili.video.pro.api.event

import org.bukkit.entity.Player

/**
 * 玩家状态查询事件
 *
 * 当玩家查询自己的绑定状态和统计信息时触发此事件。
 * 可以用于日志记录、统计分析或触发相关的状态更新逻辑。
 *
 * @param player 查询状态的玩家
 * @param bilibiliUsername B站用户名，如果未绑定则为null
 * @param bilibiliUid B站用户ID，如果未绑定则为null
 * @param totalVideos 总互动视频数量
 * @param tripleCompletedVideos 完成三联的视频数量
 *
 * @author BilibiliVideoPro
 * @since 2.0.0
 */
class PlayerStatusQueryEvent(
    /**
     * 查询状态的玩家
     */
    val player: Player,

    /**
     * B站用户名
     * 如果玩家未绑定B站账户则为null
     */
    val bilibiliUsername: String?,

    /**
     * B站用户ID
     * 如果玩家未绑定B站账户则为null
     */
    val bilibiliUid: String?,

    /**
     * 总互动视频数量
     * 包括所有有过交互记录的视频数量
     */
    val totalVideos: Int,

    /**
     * 完成三联的视频数量
     * 指同时完成点赞、投币、收藏的视频数量
     */
    val tripleCompletedVideos: Int
) : BilibiliVideoProxyEvent()