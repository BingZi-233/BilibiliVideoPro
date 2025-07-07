package online.bingzi.bilibili.video.pro.internal.listener

import online.bingzi.bilibili.video.pro.api.event.CacheCleanupEvent
import online.bingzi.bilibili.video.pro.api.event.ErrorHandlingEvent
import online.bingzi.bilibili.video.pro.api.entity.event.ErrorType
import online.bingzi.bilibili.video.pro.api.event.GuiActionEvent
import online.bingzi.bilibili.video.pro.api.event.PlayerLoginCompleteEvent
import online.bingzi.bilibili.video.pro.api.event.PlayerLoginStartEvent
import online.bingzi.bilibili.video.pro.api.event.PlayerStatusQueryEvent
import online.bingzi.bilibili.video.pro.api.event.RewardGrantEvent
import online.bingzi.bilibili.video.pro.api.event.SystemStatusQueryEvent
import online.bingzi.bilibili.video.pro.api.event.TripleActionCheckEvent
import online.bingzi.bilibili.video.pro.api.event.TripleActionCompleteEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit

/**
 * BilibiliVideoPro事件监听器
 * 使用TabooLib的@SubscribeEvent注解
 */
object BilibiliEventListener {

    /**
     * 监听登录开始事件
     */
    @SubscribeEvent
    fun onPlayerLoginStart(event: PlayerLoginStartEvent) {
        console().sendMessage("§7[BilibiliVideoPro] 玩家 ${event.player.name} 开始登录")
    }

    /**
     * 监听登录完成事件
     */
    @SubscribeEvent
    fun onPlayerLoginComplete(event: PlayerLoginCompleteEvent) {
        if (event.success) {
            console().sendMessage("§a[BilibiliVideoPro] 玩家 ${event.player.name} 登录成功: ${event.bilibiliUsername}")
            submit(async = false) {
                event.player.sendLang("loginWelcome", event.bilibiliUsername)
            }
        } else {
            console().sendMessage("§c[BilibiliVideoPro] 玩家 ${event.player.name} 登录失败: ${event.errorMessage}")
            submit(async = false) {
                event.player.sendLang("loginFailed", event.errorMessage ?: "未知错误")
            }
        }
    }

    /**
     * 监听一键三联检查事件
     */
    @SubscribeEvent
    fun onTripleActionCheck(event: TripleActionCheckEvent) {
        console().sendMessage("§7[BilibiliVideoPro] 玩家 ${event.player.name} 检查视频 ${event.bvid} 三联状态")
    }

    /**
     * 监听一键三联完成事件
     */
    @SubscribeEvent
    fun onTripleActionComplete(event: TripleActionCompleteEvent) {
        console().sendMessage("§a[BilibiliVideoPro] 玩家 ${event.player.name} 完成视频 ${event.bvid} 一键三联")

        submit(async = false) {
            event.player.sendLang("tripleActionRewardAwarded", event.bvid)
        }
    }

    /**
     * 监听奖励发放事件
     */
    @SubscribeEvent
    fun onRewardGrant(event: RewardGrantEvent) {
        console().sendMessage("§6[BilibiliVideoPro] 为玩家 ${event.player.name} 发放奖励: ${event.rewardType.name}")
    }

    /**
     * 监听玩家状态查询事件
     */
    @SubscribeEvent
    fun onPlayerStatusQuery(event: PlayerStatusQueryEvent) {
        console().sendMessage("§7[BilibiliVideoPro] 玩家 ${event.player.name} 查询状态")
    }

    /**
     * 监听系统状态查询事件
     */
    @SubscribeEvent
    fun onSystemStatusQuery(event: SystemStatusQueryEvent) {
        console().sendMessage("§7[BilibiliVideoPro] 系统状态查询")
    }

    /**
     * 监听缓存清理事件
     */
    @SubscribeEvent
    fun onCacheCleanup(event: CacheCleanupEvent) {
        console().sendMessage("§7[BilibiliVideoPro] 缓存清理: ${event.cleanupType.name}, 清理项目: ${event.itemsRemoved}, 释放内存: ${event.memoryFreed} bytes")
    }

    /**
     * 监听错误处理事件
     */
    @SubscribeEvent
    fun onErrorHandling(event: ErrorHandlingEvent) {
        val playerName = event.player?.name ?: "Console"
        console().sendMessage("§c[BilibiliVideoPro] 错误处理: ${event.errorType.name} - 玩家: $playerName, 上下文: ${event.context}, 消息: ${event.errorMessage}")

        // 对于严重错误，记录更多信息
        if (event.errorType in listOf(ErrorType.AUTHENTICATION_ERROR, ErrorType.PERMISSION_ERROR, ErrorType.SYSTEM_ERROR)) {
            console().sendMessage("§4[BilibiliVideoPro] 严重错误: ${event.originalError.stackTraceToString()}")
        }
    }

    /**
     * 监听GUI操作事件
     */
    @SubscribeEvent
    fun onGuiAction(event: GuiActionEvent) {
        console().sendMessage("§7[BilibiliVideoPro] 玩家 ${event.player.name} GUI操作: ${event.guiType.name} - ${event.actionType.name}")
    }
}