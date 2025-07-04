package online.bingzi.bilibili.video.pro.internal.command

import online.bingzi.bilibili.video.pro.internal.entity.netwrk.auth.QRCodeResult
import online.bingzi.bilibili.video.pro.internal.helper.MapItemHelper
import online.bingzi.bilibili.video.pro.internal.network.BilibiliApiClient
import online.bingzi.bilibili.video.pro.internal.network.auth.QRCodeLoginService
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.module.chat.colored
import taboolib.platform.util.bukkitPlugin
import java.util.*

/**
 * Bilibili登录命令处理器
 * 使用TabooLib框架实现命令注册和处理
 */
@CommandHeader(name = "bilibililogin", aliases = ["blogin", "bililogin"])
object LoginCommand {

    // 使用现有的Bilibili服务
    private val apiClient = BilibiliApiClient()
    private val loginService = QRCodeLoginService(apiClient)
    
    // 存储玩家的登录状态
    private val loginSessions = mutableMapOf<UUID, LoginSession>()

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val login = subCommand {
        execute<Player> { sender, _, _ ->
            val player = sender.cast<Player>()
            startLogin(player)
        }
    }

    @CommandBody
    val status = subCommand {
        execute<Player> { sender, _, _ ->
            val player = sender.cast<Player>()
            checkLoginStatus(player)
        }
    }

    @CommandBody
    val logout = subCommand {
        execute<Player> { sender, _, _ ->
            val player = sender.cast<Player>()
            logout(player)
        }
    }

    /**
     * 开始登录流程
     */
    private fun startLogin(player: Player) {
        val playerUUID = player.uniqueId
        
        // 检查是否已有登录会话
        if (loginSessions.containsKey(playerUUID)) {
            player.sendMessage("&c您已有正在进行的登录会话，请先使用 /bilibililogin status 检查状态".colored())
            return
        }

        player.sendMessage("&a正在获取登录二维码，请稍候...".colored())

        // 异步开始登录流程
        Bukkit.getScheduler().runTaskAsynchronously(bukkitPlugin) {
            loginService.startQRCodeLogin(
                onQRCodeGenerated = { qrData ->
                    val session = LoginSession(
                        oauthKey = qrData.qrcodeKey,
                        qrCodeUrl = qrData.url,
                        startTime = System.currentTimeMillis()
                    )
                    loginSessions[playerUUID] = session
                    
                    // 在主线程中创建地图物品
                    Bukkit.getScheduler().runTask(bukkitPlugin) {
                        val mapItem = createQRCodeMap(qrData.url, qrData.qrcodeKey)
                        player.inventory.addItem(mapItem)
                        player.sendMessage("&a二维码已生成！请查看您的物品栏中的地图物品".colored())
                        player.sendMessage("&e请使用Bilibili手机APP扫描二维码进行登录".colored())
                        player.sendMessage("&7提示：使用 /bilibililogin status 检查登录状态".colored())
                    }
                },
                onStatusChanged = { statusMessage ->
                    Bukkit.getScheduler().runTask(bukkitPlugin) {
                        player.sendMessage("&e$statusMessage".colored())
                    }
                },
                onLoginSuccess = {
                    Bukkit.getScheduler().runTask(bukkitPlugin) {
                        player.sendMessage("&a登录成功！欢迎回来！".colored())
                        loginSessions[playerUUID]?.isLoggedIn = true
                    }
                },
                onError = { errorMessage ->
                    Bukkit.getScheduler().runTask(bukkitPlugin) {
                        player.sendMessage("&c$errorMessage".colored())
                        loginSessions.remove(playerUUID)
                    }
                }
            )
        }
    }



    /**
     * 创建二维码地图物品
     */
    private fun createQRCodeMap(qrUrl: String, oauthKey: String) = 
        MapItemHelper.createBilibiliLoginQRCodeMap(qrUrl, oauthKey)





    /**
     * 检查玩家登录状态
     */
    private fun checkLoginStatus(player: Player) {
        val session = loginSessions[player.uniqueId]
        val isLoggedIn = apiClient.isLoggedIn()
        
        if (session == null && !isLoggedIn) {
            player.sendMessage("&c您当前没有登录会话".colored())
            return
        }

        player.sendMessage("&a登录状态：".colored())
        
        if (session != null) {
            val elapsed = (System.currentTimeMillis() - session.startTime) / 1000
            player.sendMessage("&7- 登录会话已用时：${elapsed}秒".colored())
            player.sendMessage("&7- 会话状态：${if (session.isLoggedIn) "&a已登录" else "&e等待扫码"}".colored())
        }
        
        player.sendMessage("&7- Bilibili账户状态：${if (isLoggedIn) "&a已登录" else "&c未登录"}".colored())
    }

    /**
     * 登出
     */
    private fun logout(player: Player) {
        val session = loginSessions.remove(player.uniqueId)
        if (session != null) {
            apiClient.clearCookies()
            player.sendMessage("&a已登出".colored())
        } else {
            player.sendMessage("&c您当前没有登录会话".colored())
        }
    }

    /**
     * 登录会话数据类
     */
    data class LoginSession(
        val oauthKey: String,
        val qrCodeUrl: String,
        val startTime: Long,
        var isLoggedIn: Boolean = false
    )


}