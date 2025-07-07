package online.bingzi.bilibili.video.pro.api.event

import online.bingzi.bilibili.video.pro.api.entity.event.CacheCleanupType

/**
 * 缓存清理事件
 *
 * 当系统执行缓存清理操作时触发此事件。
 * 包含清理的详细统计信息，可以用于监控内存使用和系统性能。
 *
 * @param cleanupType 清理类型
 * @param itemsRemoved 清理的项目数量
 * @param memoryFreed 释放的内存字节数（估算值）
 *
 * @author BilibiliVideoPro
 * @since 2.0.0
 */
class CacheCleanupEvent(
    /**
     * 清理类型
     * @see CacheCleanupType
     */
    val cleanupType: CacheCleanupType,

    /**
     * 清理的项目数量
     * 表示从缓存中移除的具体项目数
     */
    val itemsRemoved: Int,

    /**
     * 释放的内存字节数
     * 估算值，用于内存使用统计和监控
     */
    val memoryFreed: Long
) : BilibiliVideoProxyEvent()