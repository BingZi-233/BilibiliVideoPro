package online.bingzi.bilibili.video.pro.internal.listener

import online.bingzi.bilibili.video.pro.api.entity.event.ErrorType
import online.bingzi.bilibili.video.pro.api.event.*
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import taboolib.module.lang.sendError
import taboolib.module.lang.sendInfo
import taboolib.module.lang.sendWarn
import taboolib.platform.util.asLangText
import taboolib.platform.util.sendLang

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
        console().sendInfo("eventPlayerLoginStart", event.player.name)
    }

    /**
     * 监听登录完成事件
     */
    @SubscribeEvent
    fun onPlayerLoginComplete(event: PlayerLoginCompleteEvent) {
        if (event.success) {
            console().sendInfo("eventPlayerLoginSuccess", event.player.name, event.bilibiliUsername)
            submit(async = false) {
                event.player.sendLang("loginWelcome", event.bilibiliUsername)
            }
        } else {
            console().sendWarn("eventPlayerLoginFailed", event.player.name, event.errorMessage ?: "")
            submit(async = false) {
                event.player.sendLang("loginFailed", event.errorMessage ?: console().asLangText("loginUnknownError"))
            }
        }
    }

    /**
     * 监听一键三联检查事件
     */
    @SubscribeEvent
    fun onTripleActionCheck(event: TripleActionCheckEvent) {
        console().sendInfo("eventTripleActionCheck", event.player.name, event.bvid)
    }

    /**
     * 监听一键三联完成事件
     */
    @SubscribeEvent
    fun onTripleActionComplete(event: TripleActionCompleteEvent) {
        console().sendInfo("eventTripleActionComplete", event.player.name, event.bvid)

        submit(async = false) {
            event.player.sendLang("tripleActionRewardAwarded", event.bvid)
        }
    }

    /**
     * 监听奖励发放事件
     */
    @SubscribeEvent
    fun onRewardGrant(event: RewardGrantEvent) {
        console().sendInfo("eventRewardGrant", event.player.name, event.rewardType.name)
    }

    /**
     * 监听玩家状态查询事件
     */
    @SubscribeEvent
    fun onPlayerStatusQuery(event: PlayerStatusQueryEvent) {
        console().sendInfo("eventPlayerStatusQuery", event.player.name)
    }

    /**
     * 监听系统状态查询事件
     */
    @SubscribeEvent
    fun onSystemStatusQuery(event: SystemStatusQueryEvent) {
        console().sendInfo("eventSystemStatusQuery")
    }

    /**
     * 监听缓存清理事件
     */
    @SubscribeEvent
    fun onCacheCleanup(event: CacheCleanupEvent) {
        console().sendInfo("eventCacheCleanup", event.cleanupType.name, event.itemsRemoved.toString(), event.memoryFreed.toString())
    }

    /**
     * 监听错误处理事件
     */
    @SubscribeEvent
    fun onErrorHandling(event: ErrorHandlingEvent) {
        val playerName = event.player?.name ?: "Console"
        console().sendWarn("eventErrorHandling", event.errorType.name, playerName, event.context, event.errorMessage)

        // 对于严重错误，记录更多信息
        if (event.errorType in listOf(ErrorType.AUTHENTICATION_ERROR, ErrorType.PERMISSION_ERROR, ErrorType.SYSTEM_ERROR)) {
            console().sendError("eventSeriousError", event.originalError.stackTraceToString())
        }
    }

    /**
     * 监听GUI操作事件
     */
    @SubscribeEvent
    fun onGuiAction(event: GuiActionEvent) {
        console().sendInfo("eventGuiAction", event.player.name, event.guiType.name, event.actionType.name)
    }
}