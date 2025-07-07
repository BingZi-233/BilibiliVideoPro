package online.bingzi.bilibili.video.pro.api.event

import org.bukkit.entity.Player

/**
 * 玩家登录完成事件
 *
 * 当玩家的Bilibili登录流程完成时触发此事件，无论成功还是失败。
 * 可以用于后续处理，如数据库更新、统计记录、奖励发放等。
 *
 * @param player 登录的玩家
 * @param bilibiliUsername B站用户名，登录失败时可能为空
 * @param bilibiliUid B站用户ID，登录失败时可能为空
 * @param success 是否登录成功
 * @param errorMessage 错误消息，仅在登录失败时有值
 *
 * @author BilibiliVideoPro
 * @since 2.0.0
 */
class PlayerLoginCompleteEvent(
    /**
     * 登录的玩家
     */
    val player: Player,

    /**
     * B站用户名
     * 登录成功时包含实际用户名，失败时可能为空字符串
     */
    val bilibiliUsername: String,

    /**
     * B站用户ID
     * 登录成功时包含实际UID，失败时可能为空字符串
     */
    val bilibiliUid: String,

    /**
     * 是否登录成功
     */
    val success: Boolean,

    /**
     * 错误消息
     * 仅在登录失败时包含具体的错误信息
     */
    val errorMessage: String? = null
) : BilibiliVideoProxyEvent()