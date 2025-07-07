package online.bingzi.bilibili.video.pro.api.event

import org.bukkit.entity.Player

/**
 * 玩家登录开始事件
 *
 * 当玩家开始执行登录流程时触发此事件。
 * 通常在生成二维码之前发布，可以用于预处理、验证或日志记录。
 *
 * @param player 执行登录的玩家
 * @param qrcodeKey 二维码密钥，如果是初始触发可能为空字符串
 *
 * @author BilibiliVideoPro
 * @since 2.0.0
 */
class PlayerLoginStartEvent(
    /**
     * 执行登录的玩家
     */
    val player: Player,

    /**
     * 二维码密钥
     * 在登录流程开始时可能为空，在二维码生成后会包含实际的密钥
     */
    val qrcodeKey: String
) : BilibiliVideoProxyEvent()