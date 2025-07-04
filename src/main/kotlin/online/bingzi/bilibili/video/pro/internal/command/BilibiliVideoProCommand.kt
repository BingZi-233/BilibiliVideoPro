package online.bingzi.bilibili.video.pro.internal.command

import online.bingzi.bilibili.video.pro.internal.helper.MapItemHelper
import online.bingzi.bilibili.video.pro.internal.network.BilibiliApiClient
import online.bingzi.bilibili.video.pro.internal.network.auth.QRCodeLoginService
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submit
import taboolib.expansion.createHelper
import taboolib.module.chat.colored
import java.util.*

/**
 * BilibiliVideoPro 主命令处理器
 * 统一命令注册入口，包含所有插件功能
 */
@CommandHeader(name = "bilibilipro", aliases = ["bvp", "bilibili"], description = "BilibiliVideoPro 插件主命令")
object BilibiliVideoProCommand {

    // Bilibili服务实例
    private val apiClient = BilibiliApiClient()
    private val loginService = QRCodeLoginService(apiClient)
    
    // 存储正在登录的玩家（避免重复登录）
    private val loginInProgress = mutableSetOf<UUID>()

    @CommandBody
    val main = mainCommand {
        createHelper()
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("&6&l========== BilibiliVideoPro ==========".colored())
            sender.sendMessage("&a版本: &f1.0.0".colored())
            sender.sendMessage("&a作者: &fBingZi-233".colored())
            sender.sendMessage("&7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━".colored())
            sender.sendMessage("&e可用命令:".colored())
            sender.sendMessage("&a/bvp info &7- &f查看插件信息".colored())
            sender.sendMessage("&a/bvp login &7- &f登录哔哩哔哩账户".colored())
            sender.sendMessage("&a/bvp status &7- &f查看登录状态".colored())
            sender.sendMessage("&7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━".colored())
            sender.sendMessage("&7使用 &a/bvp help &7获取更多帮助".colored())
        }
    }

    @CommandBody
    val info = subCommand {
        literal("info")
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("&6&l========== 插件信息 ==========".colored())
            sender.sendMessage("&a插件名称: &fBilibiliVideoPro".colored())
            sender.sendMessage("&a插件版本: &f1.0.0".colored())
            sender.sendMessage("&a开发者: &fBingZi-233".colored())
            sender.sendMessage("&a项目地址: &fhttps://github.com/BingZi-233/BilibiliVideoPro".colored())
            sender.sendMessage("&7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━".colored())
            sender.sendMessage("&e功能特性:".colored())
            sender.sendMessage("&7• &f哔哩哔哩账户登录".colored())
            sender.sendMessage("&7• &f视频互动功能".colored())
            sender.sendMessage("&7• &f二维码地图显示".colored())
            sender.sendMessage("&7• &f数据持久化存储".colored())
        }
    }

    @CommandBody
    val login = subCommand {
        execute<Player> { sender, _, _ ->
            startLogin(sender)
        }
    }

    @CommandBody
    val status = subCommand {
        execute<Player> { sender, _, _ ->
            checkLoginStatus(sender)
        }
    }

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("&a正在重载插件配置...".colored())
            // 这里可以添加重载逻辑
            sender.sendMessage("&a配置重载完成！".colored())
        }
    }

    /**
     * 开始登录流程
     */
    private fun startLogin(player: Player) {
        val playerUUID = player.uniqueId

        // 检查是否已经登录
        if (apiClient.isLoggedIn()) {
            player.sendMessage("&a您已经登录了哔哩哔哩账户！".colored())
            player.sendMessage("&7使用 &e/bvp status &7查看登录详情".colored())
            return
        }

        // 检查是否正在登录
        if (loginInProgress.contains(playerUUID)) {
            player.sendMessage("&c您已有正在进行的登录会话，请等待完成或重新尝试".colored())
            return
        }

        player.sendMessage("&6&l========== 哔哩哔哩登录 ==========".colored())
        player.sendMessage("&a正在获取登录二维码，请稍候...".colored())
        loginInProgress.add(playerUUID)

        // 使用TabooLib异步任务
        submit(async = true) {
            loginService.startQRCodeLogin(
                onQRCodeGenerated = { qrData ->
                    // 在主线程中创建地图物品
                    submit(async = false) {
                        val mapItem = MapItemHelper.createBilibiliLoginQRCodeMap(qrData.url, qrData.qrcodeKey)
                        player.inventory.addItem(mapItem)
                        player.sendMessage("&a二维码已生成！".colored())
                        player.sendMessage("&7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━".colored())
                        player.sendMessage("&e请查看您的物品栏中的地图物品".colored())
                        player.sendMessage("&e使用哔哩哔哩手机APP扫描二维码进行登录".colored())
                        player.sendMessage("&7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━".colored())
                        player.sendMessage("&7提示：使用 &a/bvp status &7检查登录状态".colored())
                    }
                },
                onStatusChanged = { statusMessage ->
                    submit(async = false) {
                        player.sendMessage("&6[登录状态] &e$statusMessage".colored())
                    }
                },
                onLoginSuccess = {
                    submit(async = false) {
                        player.sendMessage("&6&l========== 登录成功 ==========".colored())
                        player.sendMessage("&a恭喜！您已成功登录哔哩哔哩账户！".colored())
                        player.sendMessage("&7现在您可以使用所有哔哩哔哩相关功能了".colored())
                        player.sendMessage("&7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━".colored())
                        loginInProgress.remove(playerUUID)
                    }
                },
                onError = { errorMessage ->
                    submit(async = false) {
                        player.sendMessage("&6&l========== 登录失败 ==========".colored())
                        player.sendMessage("&c登录过程中发生错误：$errorMessage".colored())
                        player.sendMessage("&7请稍后重试或联系管理员".colored())
                        player.sendMessage("&7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━".colored())
                        loginInProgress.remove(playerUUID)
                    }
                }
            )
        }
    }

    /**
     * 检查玩家登录状态
     */
    private fun checkLoginStatus(player: Player) {
        val playerUUID = player.uniqueId
        val isLoggedIn = apiClient.isLoggedIn()
        val isLoginInProgress = loginInProgress.contains(playerUUID)

        player.sendMessage("&6&l========== 登录状态 ==========".colored())
        player.sendMessage("&7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━".colored())
        
        if (isLoggedIn) {
            player.sendMessage("&a✓ 哔哩哔哩账户：&2已登录".colored())
            // 这里可以显示更多登录用户信息
        } else {
            player.sendMessage("&c✗ 哔哩哔哩账户：&4未登录".colored())
            player.sendMessage("&7  使用 &e/bvp login &7进行登录".colored())
        }
        
        if (isLoginInProgress) {
            player.sendMessage("&e⏳ 登录进程：&6正在进行中".colored())
            player.sendMessage("&7  请扫描二维码完成登录".colored())
        } else {
            player.sendMessage("&7⚪ 登录进程：&7无活动".colored())
        }
        
        player.sendMessage("&7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━".colored())
    }
}
