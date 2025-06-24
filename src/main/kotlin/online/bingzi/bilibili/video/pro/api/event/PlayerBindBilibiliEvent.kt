package online.bingzi.bilibili.video.pro.api.event

import jdk.jfr.internal.handlers.EventHandler.timestamp
import online.bingzi.bilibili.video.pro.api.entity.BilibiliUser
import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

/**
 * 玩家绑定Bilibili账号事件
 *
 * 当Minecraft玩家成功绑定Bilibili账号时触发此事件
 * 该事件可被其他插件监听，用于处理绑定后的逻辑
 *
 * @property player 绑定的Minecraft玩家
 * @property bilibiliUser 绑定的Bilibili用户信息
 * @property isFirstTime 是否为首次绑定
 * @property timestamp 绑定时间戳
 *
 * @constructor 创建一个玩家绑定Bilibili账号事件
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
class PlayerBindBilibiliEvent(
    /**
     * 绑定的Minecraft玩家
     *
     * 执行绑定操作的玩家对象
     */
    val player: Player,

    /**
     * 绑定的Bilibili用户信息
     *
     * 包含Bilibili用户的基本信息
     */
    val bilibiliUser: BilibiliUser,

    /**
     * 是否为首次绑定
     *
     * true表示该玩家首次绑定Bilibili账号
     * false表示该玩家重新绑定或更换绑定账号
     */
    val isFirstTime: Boolean = true,

    /**
     * 绑定时间戳
     *
     * 记录绑定操作的具体时间
     */
    timestamp: Long = System.currentTimeMillis()
) : BukkitProxyEvent() {

    /**
     * 获取玩家名称
     *
     * @return Minecraft玩家的名称
     */
    fun getPlayerName(): String = player.name

    /**
     * 获取玩家UUID
     *
     * @return Minecraft玩家的UUID
     */
    fun getPlayerUuid(): String = player.uniqueId.toString()

    /**
     * 获取绑定状态描述
     *
     * @return 描述绑定状态的文字
     */
    fun getBindingDescription(): String {
        val status = if (isFirstTime) "首次绑定" else "重新绑定"
        return "玩家 ${player.name} ${status}Bilibili账号 ${bilibiliUser.nickname}(${bilibiliUser.uid})"
    }

    /**
     * 获取格式化的绑定时间
     *
     * @return 格式化后的时间字符串
     */
    fun getFormattedTime(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(timestamp))
    }

    override fun toString(): String {
        return "PlayerBindBilibiliEvent(${getBindingDescription()}, time=${getFormattedTime()})"
    }
} 