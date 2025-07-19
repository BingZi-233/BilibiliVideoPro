package online.bingzi.bilibili.video.pro.internal.listener

import online.bingzi.bilibili.video.pro.api.entity.event.ErrorType
import online.bingzi.bilibili.video.pro.api.event.*
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import taboolib.module.lang.asLangText
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.common.platform.function.severe
import taboolib.platform.util.sendError
import taboolib.platform.util.sendInfo
import taboolib.platform.util.sendWarn

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
        info("Player ${event.player.name} started login")
    }

    /**
     * 监听登录完成事件
     */
    @SubscribeEvent
    fun onPlayerLoginComplete(event: PlayerLoginCompleteEvent) {
        if (event.success) {
            info("Player ${event.player.name} login success as ${event.bilibiliUsername}")
            submit(async = false) {
                event.player.sendInfo("loginWelcome", event.bilibiliUsername)
            }
        } else {
            warning("Player ${event.player.name} login failed: ${event.errorMessage ?: ""}")
            submit(async = false) {
                event.player.sendError("loginFailed", event.errorMessage ?: console().asLangText("loginUnknownError", listOf<String>()))
            }
        }
    }

    /**
     * 监听一键三联检查事件
     */
    @SubscribeEvent
    fun onTripleActionCheck(event: TripleActionCheckEvent) {
        info("Player ${event.player.name} checking triple action for ${event.bvid}")
    }

    /**
     * 监听一键三联完成事件
     */
    @SubscribeEvent
    fun onTripleActionComplete(event: TripleActionCompleteEvent) {
        info("Player ${event.player.name} completed triple action for ${event.bvid}")

        submit(async = false) {
            event.player.sendInfo("tripleActionRewardAwarded", event.bvid)
        }
    }

    /**
     * 监听奖励发放事件
     */
    @SubscribeEvent
    fun onRewardGrant(event: RewardGrantEvent) {
        info("Player ${event.player.name} granted reward: ${event.rewardType.name}")
    }

    /**
     * 监听玩家状态查询事件
     */
    @SubscribeEvent
    fun onPlayerStatusQuery(event: PlayerStatusQueryEvent) {
        info("Player ${event.player.name} status query")
    }

    /**
     * 监听系统状态查询事件
     */
    @SubscribeEvent
    fun onSystemStatusQuery(event: SystemStatusQueryEvent) {
        info("System status query")
    }

    /**
     * 监听缓存清理事件
     */
    @SubscribeEvent
    fun onCacheCleanup(event: CacheCleanupEvent) {
        info("Cache cleanup: ${event.cleanupType.name}, removed ${event.itemsRemoved} items, freed ${event.memoryFreed} bytes")
    }

    /**
     * 监听错误处理事件
     */
    @SubscribeEvent
    fun onErrorHandling(event: ErrorHandlingEvent) {
        val playerName = event.player?.name ?: "Console"
        warning("Error handling: ${event.errorType.name} for ${playerName} in ${event.context}: ${event.errorMessage}")

        // 对于严重错误，记录更多信息
        if (event.errorType in listOf(ErrorType.AUTHENTICATION_ERROR, ErrorType.PERMISSION_ERROR, ErrorType.SYSTEM_ERROR)) {
            severe("Serious error: ${event.originalError.stackTraceToString()}")
        }
    }

    /**
     * 监听GUI操作事件
     */
    @SubscribeEvent
    fun onGuiAction(event: GuiActionEvent) {
        info("GUI action: ${event.player.name} ${event.actionType.name} on ${event.guiType.name}")
    }
}