package online.bingzi.bilibili.video.pro.api.event

import taboolib.platform.type.BukkitProxyEvent
import java.util.*

/**
 * BilibiliVideoPro事件基类
 * 
 * 所有BilibiliVideoPro相关事件的基类，使用TabooLib的代理事件系统。
 * 提供基础的事件标识和时间戳功能。
 * 
 * @author BilibiliVideoPro
 * @since 2.0.0
 */
abstract class BilibiliVideoProxyEvent : BukkitProxyEvent() {
    
    /**
     * 事件创建时间戳
     */
    val timestamp: Long = System.currentTimeMillis()
    
    /**
     * 事件唯一标识符
     */
    val eventId: String = UUID.randomUUID().toString()
}