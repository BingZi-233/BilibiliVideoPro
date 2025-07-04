package online.bingzi.bilibili.video.pro.internal.command

import online.bingzi.bilibili.video.pro.internal.database.service.PlayerBilibiliService
import online.bingzi.bilibili.video.pro.internal.database.service.VideoInteractionService
import online.bingzi.bilibili.video.pro.internal.entity.netwrk.auth.QRCodeResult
import online.bingzi.bilibili.video.pro.internal.entity.netwrk.auth.LoginPollResult
import online.bingzi.bilibili.video.pro.internal.entity.netwrk.video.TripleActionResult
import online.bingzi.bilibili.video.pro.internal.helper.ketherEval
import online.bingzi.bilibili.video.pro.internal.helper.MapItemHelper
import online.bingzi.bilibili.video.pro.internal.manager.PluginManager
import online.bingzi.bilibili.video.pro.internal.network.BilibiliNetworkManager
import online.bingzi.bilibili.video.pro.internal.network.auth.QRCodeLoginService
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.common.platform.function.submit
import taboolib.common.platform.ProxyPlayer
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import java.util.concurrent.ConcurrentHashMap

/**
 * BilibiliVideoPro 主命令处理器
 * 提供登录、一键三联检查、状态管理等功能
 */
@CommandHeader(name = "bilibilipro", aliases = ["bvp", "bilibili"], permission = "bilibilipro.use")
object BilibiliVideoProCommand {
    
    @Config("config.yml")
    lateinit var config: Configuration
    
    // 冷却时间管理
    private val playerCooldowns = ConcurrentHashMap<String, Long>()
    private val videoCooldowns = ConcurrentHashMap<String, Long>()
    
    // 正在进行的登录会话
    private val loginSessions = ConcurrentHashMap<String, String>()
    
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
                startLogin(player)
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
                    checkTripleAction(player, bvid)
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
                showPlayerStatus(player)
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
            sender.sendLang("infoInitialized", if (PluginManager.isInitialized()) "是" else "否")
        }
    }
    
    /**
     * GUI命令 - 打开主菜单GUI
     */
    @CommandBody
    val gui = subCommand {
        execute<Player> { player, _, _ ->
            submit(async = false) {
                online.bingzi.bilibili.video.pro.internal.gui.GuiManager.showMainMenu(player)
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
     * 重载配置命令
     */
    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            if (!sender.hasPermission("bilibilipro.admin")) {
                sender.sendLang("noPermission")
                return@execute
            }
            
            submit(async = true) {
                try {
                    config.reload()
                    online.bingzi.bilibili.video.pro.internal.gui.GuiManager.reloadGuiConfig()
                    submit(async = false) {
                        sender.sendLang("reloadSuccess")
                    }
                } catch (e: Exception) {
                    submit(async = false) {
                        sender.sendLang("reloadError", e.message ?: "Unknown error")
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
            sender.sendMessage("§7主题切换功能开发中...")
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
            val existingSession = loginSessions[player.uniqueId.toString()]
            if (existingSession != null) {
                submit(async = false) {
                    player.sendLang("loginAlreadyInProgress")
                }
                return
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
                    loginSessions[player.uniqueId.toString()] = qrData.qrcodeKey
                    
                    // 创建并给予QR码地图物品
                    val mapItem = MapItemHelper.createQRCodeMapItem(qrData.url, "登录二维码")
                    submit(async = false) {
                        player.inventory.addItem(mapItem)
                        player.sendLang("loginQRCodeGenerated")
                    }
                    
                    // 开始轮询登录状态
                    startLoginPolling(player, qrData.qrcodeKey)
                }
                is QRCodeResult.Error -> {
                    submit(async = false) {
                        player.sendLang("loginQRCodeFailed", qrResult.message)
                    }
                }
            }
            
        } catch (e: Exception) {
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
        
        var attempts = 0
        
        fun checkLoginStatus() {
            submit(async = true) {
                attempts++
                
                if (attempts > maxAttempts) {
                    loginSessions.remove(player.uniqueId.toString())
                    submit(async = false) {
                        player.sendLang("loginTimeout")
                    }
                    return@submit
                }
                
                val statusResult = networkManager.qrCodeLogin.pollLoginStatus(qrcodeKey)
                when (statusResult) {
                    is LoginPollResult.Success -> {
                        loginSessions.remove(player.uniqueId.toString())
                        
                        // 暂时简化处理，只显示成功消息
                        submit(async = false) {
                            player.sendLang("loginSuccess", "登录成功")
                        }
                    }
                    is LoginPollResult.WaitingScan,
                    is LoginPollResult.WaitingConfirm -> {
                        // 继续等待
                        submit(async = false, delay = checkInterval) {
                            checkLoginStatus()
                        }
                    }
                    is LoginPollResult.Expired -> {
                        loginSessions.remove(player.uniqueId.toString())
                        submit(async = false) {
                            player.sendLang("loginExpired")
                        }
                    }
                    is LoginPollResult.Error -> {
                        loginSessions.remove(player.uniqueId.toString())
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
                        submit(async = false) {
                            player.sendLang("tripleActionCompleted", bvid)
                        }
                        
                        // 设置冷却时间
                        setCooldown(player, bvid)
                        
                        // 执行奖励脚本
                        executeRewardScript(player, bvid)
                    } else {
                        submit(async = false) {
                            player.sendLang("tripleActionIncomplete", 
                                if (status.isLiked) "✓" else "✗",
                                if (status.isCoined) "✓" else "✗",
                                if (status.isFavorited) "✓" else "✗"
                            )
                        }
                    }
                }
                is TripleActionResult.Error -> {
                    submit(async = false) {
                        player.sendLang("checkTripleActionFailed", tripleResult.message)
                    }
                }
            }
            
        } catch (e: Exception) {
            submit(async = false) {
                player.sendLang("checkTripleActionError", e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * 检查冷却时间
     */
    private fun isOnCooldown(player: Player, bvid: String): Boolean {
        val now = System.currentTimeMillis()
        val playerUuid = player.uniqueId.toString()
        
        // 检查全局冷却
        val globalCooldown = config.getInt("triple_action_rewards.cooldown.global", 300) * 1000
        val lastGlobalTime = playerCooldowns[playerUuid] ?: 0
        if (now - lastGlobalTime < globalCooldown) {
            val remainingTime = (globalCooldown - (now - lastGlobalTime)) / 1000
            submit(async = false) {
                player.sendLang("globalCooldown", remainingTime)
            }
            return true
        }
        
        // 检查视频冷却
        val videoCooldown = config.getInt("triple_action_rewards.cooldown.per_video", 3600) * 1000
        val videoKey = "${playerUuid}_${bvid}"
        val lastVideoTime = videoCooldowns[videoKey] ?: 0
        if (now - lastVideoTime < videoCooldown) {
            val remainingTime = (videoCooldown - (now - lastVideoTime)) / 1000
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
        val now = System.currentTimeMillis()
        val playerUuid = player.uniqueId.toString()
        val videoKey = "${playerUuid}_${bvid}"
        
        playerCooldowns[playerUuid] = now
        videoCooldowns[videoKey] = now
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
        
        submit(async = false) {
            try {
                // 使用KetherHelper执行脚本
                script.ketherEval(player as ProxyPlayer)
            } catch (e: Exception) {
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
            
            submit(async = false) {
                player.sendLang("statusTitle")
                player.sendLang("statusBoundAccount", binding.bilibiliUsername, binding.bilibiliUid)
                player.sendLang("statusBindTime", binding.createdTime)
                player.sendLang("statusLastLogin", binding.lastLoginTime ?: "从未")
                player.sendLang("statusInteractionCount", stats.totalVideos)
                player.sendLang("statusTripleActionCount", stats.tripleCompletedVideos)
            }
            
        } catch (e: Exception) {
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