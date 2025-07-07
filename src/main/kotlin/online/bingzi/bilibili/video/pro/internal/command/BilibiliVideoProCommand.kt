package online.bingzi.bilibili.video.pro.internal.command

import online.bingzi.bilibili.video.pro.api.entity.event.ErrorType
import online.bingzi.bilibili.video.pro.api.entity.event.GuiType
import online.bingzi.bilibili.video.pro.api.entity.event.RewardType
import online.bingzi.bilibili.video.pro.api.event.*
import online.bingzi.bilibili.video.pro.internal.cache.CacheCleanupManager
import online.bingzi.bilibili.video.pro.internal.database.service.PlayerBilibiliService
import online.bingzi.bilibili.video.pro.internal.database.service.VideoInteractionService
import online.bingzi.bilibili.video.pro.internal.entity.netwrk.auth.LoginPollResult
import online.bingzi.bilibili.video.pro.internal.entity.netwrk.auth.QRCodeResult
import online.bingzi.bilibili.video.pro.internal.entity.netwrk.video.TripleActionResult
import online.bingzi.bilibili.video.pro.internal.helper.MapItemHelper
import online.bingzi.bilibili.video.pro.internal.helper.ketherEval
import online.bingzi.bilibili.video.pro.internal.manager.PluginManager
import online.bingzi.bilibili.video.pro.internal.monitor.SystemMonitor
import online.bingzi.bilibili.video.pro.internal.network.BilibiliNetworkManager
import online.bingzi.bilibili.video.pro.internal.validation.InputValidator
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.lang.asLangText
import taboolib.platform.util.asLangText
import taboolib.platform.util.sendLang

/**
 * BilibiliVideoPro 主命令处理器
 * 提供登录、一键三联检查、状态管理等功能
 */
@CommandHeader(name = "bilibilipro", aliases = ["bvp", "bilibili"], permission = "bilibilipro.use")
object BilibiliVideoProCommand {

    @Config("config.yml")
    lateinit var config: Configuration

    /**
     * 主命令 - 显示帮助信息
     */
    @CommandBody
    val main = mainCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendLang("commandHelpTitle")
            sender.sendLang("commandHelpLogin")
            sender.sendLang("commandHelpCheck")
            sender.sendLang("commandHelpStatus")
            sender.sendLang("commandHelpInfo")
            sender.sendLang("commandHelpGui")
            if (sender.hasPermission("bilibilipro.admin")) {
                sender.sendLang("commandHelpUnbind")
                sender.sendLang("commandHelpReload")
                sender.sendLang("commandHelpSystemStatus")
                sender.sendLang("commandHelpReport")
                sender.sendLang("commandHelpCleanStats")
            }
        }
    }

    /**
     * 登录命令 - 开始QR码登录流程
     */
    @CommandBody
    val login = subCommand {
        execute<Player> { player, _, _ ->
            submit(async = true) {
                // 发布登录开始事件
                val loginStartEvent = PlayerLoginStartEvent(player, "")
                loginStartEvent.call()

                if (!loginStartEvent.isCancelled) {
                    startLogin(player)
                }
            }
        }
    }

    /**
     * 检查命令 - 检查指定BV号是否完成一键三联
     */
    @CommandBody
    val check = subCommand {
        dynamic("bvid") {
            execute<Player> { player, _, argument ->
                val bvid = argument
                submit(async = true) {
                    // 发布三联检查开始事件
                    val tripleCheckEvent = TripleActionCheckEvent(player, bvid, false, false, false)
                    tripleCheckEvent.call()

                    if (!tripleCheckEvent.isCancelled) {
                        checkTripleAction(player, bvid)
                    }
                }
            }
        }
    }

    /**
     * 状态命令 - 查看玩家绑定状态
     */
    @CommandBody
    val status = subCommand {
        execute<Player> { player, _, _ ->
            submit(async = true) {
                // 发布状态查询事件
                val statusEvent = PlayerStatusQueryEvent(player, null, null, 0, 0)
                statusEvent.call()

                if (!statusEvent.isCancelled) {
                    showPlayerStatus(player)
                }
            }
        }
    }

    /**
     * 解绑命令 - 管理员解绑玩家账户
     */
    @CommandBody
    val unbind = subCommand {
        dynamic("player") {
            execute<CommandSender> { sender, _, argument ->
                if (!sender.hasPermission("bilibilipro.admin")) {
                    sender.sendLang("noPermission")
                    return@execute
                }

                val targetPlayer = argument
                submit(async = true) {
                    unbindPlayer(sender, targetPlayer)
                }
            }
        }
    }

    /**
     * 插件信息命令
     */
    @CommandBody
    val info = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendLang("infoTitle")
            sender.sendLang("infoVersion")
            sender.sendLang("infoAuthor")
            sender.sendLang("infoDescription")
            sender.sendLang("infoInitialized", if (PluginManager.isInitialized()) sender.asLangText("infoYes") else sender.asLangText("infoNo"))
        }
    }

    /**
     * GUI命令 - 打开主菜单GUI
     */
    @CommandBody
    val gui = subCommand {
        execute<Player> { player, _, _ ->
            submit(async = false) {
                // 发布GUI操作事件
                val guiEvent = GuiActionEvent(player, GuiType.MAIN_MENU, GuiActionType.OPEN)
                guiEvent.call()

                if (!guiEvent.isCancelled) {
                    online.bingzi.bilibili.video.pro.internal.gui.GuiManager.showMainMenu(player)
                }
            }
        }
    }

    /**
     * GUI主题命令
     */
    @CommandBody
    val theme = subCommand {
        dynamic("theme_name") {
            execute<CommandSender> { sender, _, argument ->
                if (!sender.hasPermission("bilibilipro.admin")) {
                    sender.sendLang("noPermission")
                    return@execute
                }

                val themeName = argument
                submit(async = true) {
                    switchGuiTheme(sender, themeName)
                }
            }
        }

        execute<CommandSender> { sender, _, _ ->
            submit(async = false) {
                sender.sendMessage(online.bingzi.bilibili.video.pro.internal.gui.GuiManager.getCurrentThemeInfo())
            }
        }
    }

    /**
     * 系统状态命令 - 管理员查看系统状态
     */
    @CommandBody
    val status_admin = subCommand {
        literal("admin") {
            execute<CommandSender> { sender, _, _ ->
                if (!sender.hasPermission("bilibilipro.admin")) {
                    sender.sendLang("noPermission")
                    return@execute
                }

                submit(async = true) {
                    // 发布系统状态查询事件
                    val systemStatusEvent = SystemStatusQueryEvent(sender, emptyMap(), emptyMap())
                    systemStatusEvent.call()

                    if (!systemStatusEvent.isCancelled) {
                        val healthStatus = SystemMonitor.getHealthStatus()
                        val performanceStats = SystemMonitor.getPerformanceStats()

                        submit(async = false) {
                            sender.sendLang("systemStatusTitle")
                            sender.sendLang("systemStatusOverall", healthStatus.overall.name)
                            sender.sendLang("systemStatusDatabase", healthStatus.database.name)
                            sender.sendLang("systemStatusSecurity", healthStatus.security.name)
                            sender.sendLang("systemStatusNetwork", healthStatus.network.name)
                            sender.sendLang("systemStatusMemory", healthStatus.memory.name)
                            sender.sendLang("systemStatusUptime", (performanceStats.uptime / 1000).toString())
                            sender.sendLang("systemStatusMemoryUsage", String.format("%.2f", performanceStats.memoryUsage.usagePercentage))

                            if (performanceStats.requestCounts.isNotEmpty()) {
                                sender.sendLang("systemStatusRequestStats")
                                performanceStats.requestCounts.forEach { (operation, count) ->
                                    val avgTime = performanceStats.averageExecutionTimes[operation] ?: 0.0
                                    sender.sendLang("systemStatusRequestDetail", operation, count.toString(), String.format("%.2f", avgTime))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 系统报告命令 - 生成详细报告
     */
    @CommandBody
    val report = subCommand {
        execute<CommandSender> { sender, _, _ ->
            if (!sender.hasPermission("bilibilipro.admin")) {
                sender.sendLang("noPermission")
                return@execute
            }

            submit(async = true) {
                val report = SystemMonitor.generateSystemReport()

                submit(async = false) {
                    report.lines().forEach { line ->
                        sender.sendMessage(line)
                    }
                }
            }
        }
    }

    /**
     * 清理统计命令
     */
    @CommandBody
    val clean = subCommand {
        literal("stats") {
            execute<CommandSender> { sender, _, _ ->
                if (!sender.hasPermission("bilibilipro.admin")) {
                    sender.sendLang("noPermission")
                    return@execute
                }

                submit(async = true) {
                    SystemMonitor.clearStatistics()
                    online.bingzi.bilibili.video.pro.internal.error.ErrorHandler.clearErrorStatistics()

                    submit(async = false) {
                        sender.sendLang("cleanStatsSuccess")
                    }
                }
            }
        }
    }

    /**
     * 切换GUI主题
     */
    private fun switchGuiTheme(sender: CommandSender, themeName: String) {
        // 实现主题切换逻辑
        submit(async = false) {
            sender.sendLang("themeSwitchInDevelopment")
        }
    }

    /**
     * 开始登录流程
     */
    private fun startLogin(player: Player) {
        try {
            // 检查是否已经绑定
            val existingBinding = PlayerBilibiliService.findByPlayerUuid(player.uniqueId.toString())
            if (existingBinding != null) {
                submit(async = false) {
                    player.sendLang("loginAlreadyBound", existingBinding.bilibiliUsername)
                }
                return
            }

            // 检查是否已经在登录中
            val existingSession = CacheCleanupManager.getLoginSession(player.uniqueId.toString())
            if (existingSession != null) {
                // 移除现有会话
                CacheCleanupManager.removeLoginSession(player.uniqueId.toString())
                submit(async = false) {
                    player.sendLang("loginCancelled")
                }
            }

            val networkManager = BilibiliNetworkManager.getInstance()

            submit(async = false) {
                player.sendLang("loginStarting")
            }

            // 生成QR码
            val qrResult = networkManager.qrCodeLogin.generateQRCode()
            when (qrResult) {
                is QRCodeResult.Success -> {
                    val qrData = qrResult.data

                    // 发布登录开始事件（更新qrcode key）
                    val loginStartEvent = PlayerLoginStartEvent(player, qrData.qrcodeKey)
                    loginStartEvent.call()

                    // 创建并给予QR码地图物品
                    val mapItem = MapItemHelper.createQRCodeMapItem(qrData.url, console().asLangText("loginQRCodeTitle"))
                    submit(async = false) {
                        player.inventory.addItem(mapItem)
                        player.sendLang("loginQRCodeGenerated")
                    }

                    // 开始轮询登录状态
                    startLoginPolling(player, qrData.qrcodeKey)
                }

                is QRCodeResult.Error -> {
                    // 发布错误事件
                    val errorEvent = ErrorHandlingEvent(
                        player, ErrorType.AUTHENTICATION_ERROR,
                        RuntimeException(qrResult.message),
                        qrResult.message,
                        console().asLangText("loginQRCodeContext")
                    )
                    errorEvent.call()

                    submit(async = false) {
                        player.sendLang("loginQRCodeFailed", qrResult.message)
                    }
                }
            }

        } catch (e: Exception) {
            // 发布错误事件
            val errorEvent = ErrorHandlingEvent(
                player, ErrorType.SYSTEM_ERROR,
                e,
                e.message ?: "Unknown error",
                console().asLangText("loginFlowContext")
            )
            errorEvent.call()

            submit(async = false) {
                player.sendLang("loginError", e.message ?: "Unknown error")
            }
        }
    }

    /**
     * 开始轮询登录状态
     */
    private fun startLoginPolling(player: Player, qrcodeKey: String) {
        val networkManager = BilibiliNetworkManager.getInstance()
        val checkInterval = config.getLong("login.check_interval", 2000)
        val maxAttempts = config.getInt("login.max_check_attempts", 90)
        val playerUuid = player.uniqueId.toString()

        // 注册登录会话
        CacheCleanupManager.setLoginSession(playerUuid, qrcodeKey)

        var attempts = 0

        fun checkLoginStatus() {
            submit(async = true) {
                attempts++

                // 检查是否已经超时
                if (attempts > maxAttempts) {
                    // 清理会话
                    CacheCleanupManager.removeLoginSession(playerUuid)

                    submit(async = false) {
                        player.sendLang("loginTimeout")
                    }
                    return@submit
                }

                // 检查会话是否仍然存在（可能被取消）
                if (!CacheCleanupManager.hasLoginSession(playerUuid)) {
                    return@submit
                }

                val statusResult = networkManager.qrCodeLogin.pollLoginStatus(qrcodeKey)
                when (statusResult) {
                    is LoginPollResult.Success -> {
                        // 清理会话
                        CacheCleanupManager.removeLoginSession(playerUuid)

                        // 发布登录完成事件
                        val loginCompleteEvent = PlayerLoginCompleteEvent(
                            player,
                            console().asLangText("loginSuccessMessage"),
                            console().asLangText("loginSuccessUser"),
                            true
                        )
                        loginCompleteEvent.call()

                        // 暂时简化处理，只显示成功消息
                        submit(async = false) {
                            player.sendLang("loginSuccess", console().asLangText("loginSuccessMessage"))
                        }
                    }

                    is LoginPollResult.WaitingScan,
                    is LoginPollResult.WaitingConfirm -> {
                        // 检查会话是否仍然存在（可能被取消）
                        if (CacheCleanupManager.hasLoginSession(playerUuid)) {
                            // 继续等待
                            submit(async = false, delay = checkInterval) {
                                checkLoginStatus()
                            }
                        }
                    }

                    is LoginPollResult.Expired -> {
                        // 清理会话
                        CacheCleanupManager.removeLoginSession(playerUuid)

                        // 发布登录完成事件（失败）
                        val loginCompleteEvent = PlayerLoginCompleteEvent(
                            player,
                            "",
                            "",
                            false,
                            console().asLangText("loginExpiredContext")
                        )
                        loginCompleteEvent.call()

                        submit(async = false) {
                            player.sendLang("loginExpired")
                        }
                    }

                    is LoginPollResult.Error -> {
                        // 清理会话
                        CacheCleanupManager.removeLoginSession(playerUuid)

                        // 发布错误事件
                        val errorEvent = ErrorHandlingEvent(
                            player, ErrorType.AUTHENTICATION_ERROR,
                            RuntimeException(statusResult.message),
                            statusResult.message,
                            console().asLangText("loginPollingContext")
                        )
                        errorEvent.call()

                        submit(async = false) {
                            player.sendLang("loginError", statusResult.message)
                        }
                    }
                }
            }
        }

        // 开始第一次检查
        submit(async = false, delay = checkInterval) {
            checkLoginStatus()
        }
    }

    /**
     * 检查一键三联状态
     */
    private fun checkTripleAction(player: Player, bvid: String) {
        try {
            // 验证输入参数
            val bvidValidation = InputValidator.validateBvid(bvid)
            if (!bvidValidation.isValid) {
                submit(async = false) {
                    player.sendLang("invalidBvid", bvidValidation.errorMessage ?: "BV号格式错误")
                }
                return
            }

            val uuidValidation = InputValidator.validatePlayerUuid(player.uniqueId.toString())
            if (!uuidValidation.isValid) {
                submit(async = false) {
                    player.sendLang("systemError", console().asLangText("playerUuidValidationFailed"))
                }
                return
            }

            // 检查玩家是否已绑定
            val binding = PlayerBilibiliService.findByPlayerUuid(player.uniqueId.toString())
            if (binding == null) {
                submit(async = false) {
                    player.sendLang("notBoundToBilibili")
                }
                return
            }

            // 检查冷却时间
            if (isOnCooldown(player, bvid)) {
                return
            }

            submit(async = false) {
                player.sendLang("checkingTripleAction", bvid)
            }

            val networkManager = BilibiliNetworkManager.getInstance()
            val videoService = networkManager.videoInteraction

            // 检查一键三联状态
            val tripleResult = videoService.getTripleActionStatus(bvid)
            when (tripleResult) {
                is TripleActionResult.Success -> {
                    val status = tripleResult.status

                    // 发布三联检查结果事件
                    val tripleCheckEvent = TripleActionCheckEvent(
                        player, bvid, status.isLiked, status.isCoined, status.isFavorited
                    )
                    tripleCheckEvent.call()

                    // 记录交互数据
                    VideoInteractionService.recordInteraction(
                        playerUuid = player.uniqueId.toString(),
                        bvid = bvid,
                        videoTitle = bvid, // 暂时使用bvid作为标题
                        isLiked = status.isLiked,
                        isCoined = status.isCoined,
                        isFavorited = status.isFavorited
                    )

                    // 检查是否完成一键三联
                    if (status.isLiked && status.isCoined && status.isFavorited) {
                        // 发布三联完成事件
                        val tripleCompleteEvent = TripleActionCompleteEvent(player, bvid, bvid)
                        tripleCompleteEvent.call()

                        submit(async = false) {
                            player.sendLang("tripleActionCompleted", bvid)
                        }

                        // 设置冷却时间
                        setCooldown(player, bvid)

                        // 执行奖励脚本
                        executeRewardScript(player, bvid)
                    } else {
                        submit(async = false) {
                            player.sendLang(
                                "tripleActionIncomplete",
                                if (status.isLiked) "✓" else "✗",
                                if (status.isCoined) "✓" else "✗",
                                if (status.isFavorited) "✓" else "✗"
                            )
                        }
                    }
                }

                is TripleActionResult.Error -> {
                    // 发布错误事件
                    val errorEvent = ErrorHandlingEvent(
                        player, ErrorType.NETWORK_ERROR,
                        RuntimeException(tripleResult.message),
                        tripleResult.message,
                        console().asLangText("tripleActionCheckContext")
                    )
                    errorEvent.call()

                    submit(async = false) {
                        player.sendLang("checkTripleActionFailed", tripleResult.message)
                    }
                }
            }

        } catch (e: Exception) {
            // 发布错误事件
            val errorEvent = ErrorHandlingEvent(
                player, ErrorType.SYSTEM_ERROR,
                e,
                e.message ?: "Unknown error",
                "一键三联检查"
            )
            errorEvent.call()

            submit(async = false) {
                player.sendLang("checkTripleActionError", e.message ?: "Unknown error")
            }
        }
    }

    /**
     * 检查冷却时间
     */
    private fun isOnCooldown(player: Player, bvid: String): Boolean {
        val playerUuid = player.uniqueId.toString()

        // 检查全局冷却
        if (CacheCleanupManager.isPlayerOnCooldown(playerUuid)) {
            val remainingTime = CacheCleanupManager.getPlayerCooldownRemaining(playerUuid)
            submit(async = false) {
                player.sendLang("globalCooldown", remainingTime)
            }
            return true
        }

        // 检查视频冷却
        if (CacheCleanupManager.isVideoOnCooldown(playerUuid, bvid)) {
            val remainingTime = CacheCleanupManager.getVideoCooldownRemaining(playerUuid, bvid)
            submit(async = false) {
                player.sendLang("videoCooldown", remainingTime)
            }
            return true
        }

        return false
    }

    /**
     * 设置冷却时间
     */
    private fun setCooldown(player: Player, bvid: String) {
        val playerUuid = player.uniqueId.toString()
        val globalCooldown = config.getInt("triple_action_rewards.cooldown.global", 300)
        val videoCooldown = config.getInt("triple_action_rewards.cooldown.per_video", 3600)

        CacheCleanupManager.setPlayerCooldown(playerUuid, globalCooldown)
        CacheCleanupManager.setVideoCooldown(playerUuid, bvid, videoCooldown)
    }

    /**
     * 执行奖励脚本
     */
    private fun executeRewardScript(player: Player, bvid: String) {
        if (!config.getBoolean("triple_action_rewards.enabled", true)) {
            return
        }

        // 首先检查特定BV号的配置
        val specificPath = "triple_action_rewards.specific_videos.$bvid"
        val specificEnabled = config.getBoolean("$specificPath.enabled", true)

        val script = if (config.contains("$specificPath.reward_script")) {
            // 使用特定BV号的配置
            if (!specificEnabled) {
                // 如果特定BV号被禁用，则不执行奖励
                return
            }
            config.getString("$specificPath.reward_script", "")
        } else {
            // 使用默认配置
            if (!config.getBoolean("triple_action_rewards.default.enabled", true)) {
                return
            }
            config.getString("triple_action_rewards.default.reward_script", "")
        }

        if (script.isNullOrBlank()) return

        // 发布奖励发放事件
        val rewardEvent = RewardGrantEvent(
            player, bvid, RewardType.SCRIPT, 0, script
        )
        rewardEvent.call()

        submit(async = false) {
            try {
                // 使用KetherHelper执行脚本
                script.ketherEval(player as ProxyPlayer)
            } catch (e: Exception) {
                // 发布脚本错误事件
                val errorEvent = ErrorHandlingEvent(
                    player, ErrorType.SCRIPT_ERROR,
                    e,
                    e.message ?: "Unknown error",
                    console().asLangText("tripleActionRewardContext")
                )
                errorEvent.call()

                player.sendLang("scriptExecutionError", e.message ?: "Unknown error")
            }
        }
    }

    /**
     * 显示玩家状态
     */
    private fun showPlayerStatus(player: Player) {
        try {
            val binding = PlayerBilibiliService.findByPlayerUuid(player.uniqueId.toString())
            if (binding == null) {
                submit(async = false) {
                    player.sendLang("notBoundToBilibili")
                }
                return
            }

            // 获取交互记录统计
            val stats = VideoInteractionService.getPlayerStatistics(player.uniqueId.toString())

            // 发布状态查询事件
            val statusEvent = PlayerStatusQueryEvent(
                player,
                binding.bilibiliUsername,
                binding.bilibiliUid.toString(),
                stats.totalVideos.toInt(),
                stats.tripleCompletedVideos.toInt()
            )
            statusEvent.call()

            submit(async = false) {
                player.sendLang("statusTitle")
                player.sendLang("statusBoundAccount", binding.bilibiliUsername, binding.bilibiliUid)
                player.sendLang("statusBindTime", binding.createdTime)
                player.sendLang("statusLastLogin", binding.lastLoginTime ?: "从未")
                player.sendLang("statusInteractionCount", stats.totalVideos)
                player.sendLang("statusTripleActionCount", stats.tripleCompletedVideos)
            }

        } catch (e: Exception) {
            // 发布错误事件
            val errorEvent = ErrorHandlingEvent(
                player, ErrorType.DATABASE_ERROR,
                e,
                e.message ?: "Unknown error",
                console().asLangText("statusQueryContext")
            )
            errorEvent.call()

            submit(async = false) {
                player.sendLang("statusError", e.message ?: "Unknown error")
            }
        }
    }

    /**
     * 解绑玩家账户
     */
    private fun unbindPlayer(sender: CommandSender, targetPlayerName: String) {
        try {
            // 暂时不支持按玩家名解绑，因为findByPlayerName不存在
            submit(async = false) {
                sender.sendLang("unbindPlayerNotFound", targetPlayerName)
            }

        } catch (e: Exception) {
            submit(async = false) {
                sender.sendLang("unbindError", e.message ?: "Unknown error")
            }
        }
    }
}