package online.bingzi.bilibili.video.pro.api.event

import online.bingzi.bilibili.video.pro.api.entity.BilibiliUser
import taboolib.platform.type.BukkitProxyEvent

/**
 * Bilibili事件基类
 *
 * 所有与Bilibili相关的事件都应继承此类
 * 提供了用户信息和时间戳的基础功能
 *
 * @property user 触发事件的Bilibili用户
 * @property timestamp 事件发生的时间戳
 *
 * @constructor 创建一个Bilibili事件
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
abstract class BilibiliEvent(
    /**
     * 触发事件的Bilibili用户
     *
     * 包含用户的基本信息和绑定状态
     */
    open val user: BilibiliUser,

    /**
     * 事件发生的时间戳
     *
     * 记录事件的具体发生时间，用于日志和统计
     */
    open val timestamp: Long = System.currentTimeMillis()
) : BukkitProxyEvent() {

    /**
     * 检查事件是否为有效的绑定用户触发
     *
     * @return true表示用户已绑定Minecraft账号
     */
    fun isValidBoundUser(): Boolean = user.isBound()

    /**
     * 获取事件描述
     *
     * 子类可以重写此方法来提供更详细的事件描述
     *
     * @return 事件的文字描述
     */
    open fun getEventDescription(): String = "${this::class.simpleName} triggered by ${user.nickname}"

    /**
     * 获取事件发生的时间（格式化）
     *
     * @return 格式化后的时间字符串
     */
    fun getFormattedTime(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(timestamp))
    }

    override fun toString(): String {
        return "${this::class.simpleName}(user=${user.nickname}, time=${getFormattedTime()})"
    }
} 