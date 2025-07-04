package online.bingzi.bilibili.video.pro.internal.command

import com.google.gson.JsonParser
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import okhttp3.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.adaptPlayer
import taboolib.module.chat.colored
import taboolib.platform.util.bukkitPlugin
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Bilibili登录命令处理器
 * 使用TabooLib框架实现命令注册和处理
 */
@CommandHeader(name = "bilibililogin", aliases = ["blogin", "bililogin"])
object LoginCommand {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = JsonParser()
    
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

        // 异步获取二维码
        Bukkit.getScheduler().runTaskAsynchronously(bukkitPlugin) {
            try {
                val qrData = getLoginQRCode()
                if (qrData != null) {
                    val session = LoginSession(
                        oauthKey = qrData.oauthKey,
                        qrCodeUrl = qrData.url,
                        startTime = System.currentTimeMillis()
                    )
                    loginSessions[playerUUID] = session
                    
                    // 在主线程中创建地图物品
                    Bukkit.getScheduler().runTask(bukkitPlugin) {
                        val mapItem = createQRCodeMap(qrData.url)
                        player.inventory.addItem(mapItem)
                        player.sendMessage("&a二维码已生成！请查看您的物品栏中的地图物品".colored())
                        player.sendMessage("&e请使用Bilibili手机APP扫描二维码进行登录".colored())
                        player.sendMessage("&7提示：使用 /bilibililogin status 检查登录状态".colored())
                        
                        // 开始轮询登录状态
                        startPollingLoginStatus(player, session)
                    }
                } else {
                    Bukkit.getScheduler().runTask(bukkitPlugin) {
                        player.sendMessage("&c获取登录二维码失败，请稍后重试".colored())
                    }
                }
            } catch (e: Exception) {
                Bukkit.getScheduler().runTask(bukkitPlugin) {
                    player.sendMessage("&c登录过程中发生错误：${e.message}".colored())
                }
            }
        }
    }

    /**
     * 获取登录二维码数据
     */
    private fun getLoginQRCode(): QRCodeData? {
        try {
            val request = Request.Builder()
                .url("https://passport.bilibili.com/x/passport-login/web/qrcode/generate")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: return null

            val jsonElement = gson.parse(responseBody)
            val jsonObject = jsonElement.asJsonObject

            if (jsonObject.get("code").asInt == 0) {
                val data = jsonObject.getAsJsonObject("data")
                return QRCodeData(
                    url = data.get("url").asString,
                    oauthKey = data.get("qrcode_key").asString
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 创建二维码地图物品
     */
    private fun createQRCodeMap(qrUrl: String): ItemStack {
        val mapItem = ItemStack(Material.FILLED_MAP)
        val mapMeta = mapItem.itemMeta
        
        // 创建地图视图
        val mapView = Bukkit.createMap(Bukkit.getWorlds()[0])
        mapMeta?.setDisplayName("&aBilibili登录二维码".colored())
        mapMeta?.lore = listOf(
            "&7请使用Bilibili手机APP扫描".colored(),
            "&7扫描后等待登录完成".colored()
        )
        
        mapItem.itemMeta = mapMeta

        // 清除默认渲染器
        mapView.renderers.clear()
        
        // 添加自定义二维码渲染器
        mapView.addRenderer(QRCodeMapRenderer(qrUrl))
        
        // 将地图ID存储到物品中
        val itemMeta = mapItem.itemMeta
        if (itemMeta is org.bukkit.inventory.meta.MapMeta) {
            itemMeta.mapView = mapView
            mapItem.itemMeta = itemMeta
        }
        
        return mapItem
    }

    /**
     * 开始轮询登录状态
     */
    private fun startPollingLoginStatus(player: Player, session: LoginSession) {
        val task = Bukkit.getScheduler().runTaskTimerAsynchronously(bukkitPlugin, {
            try {
                val status = checkBilibiliLoginStatus(session.oauthKey)
                when (status.code) {
                    0 -> {
                        // 登录成功
                        session.isLoggedIn = true
                        session.cookies = status.cookies
                        
                        Bukkit.getScheduler().runTask(bukkitPlugin) {
                            player.sendMessage("&a登录成功！欢迎回来！".colored())
                            loginSessions.remove(player.uniqueId)
                        }
                        return@runTaskTimerAsynchronously
                    }
                    86038 -> {
                        // 二维码已失效
                        Bukkit.getScheduler().runTask(bukkitPlugin) {
                            player.sendMessage("&c二维码已失效，请重新获取".colored())
                            loginSessions.remove(player.uniqueId)
                        }
                        return@runTaskTimerAsynchronously
                    }
                    86090 -> {
                        // 已扫码但未确认
                        Bukkit.getScheduler().runTask(bukkitPlugin) {
                            player.sendMessage("&e已扫码，请在手机上确认登录".colored())
                        }
                    }
                    86101 -> {
                        // 未扫码，继续等待
                    }
                }
                
                // 检查超时（5分钟）
                if (System.currentTimeMillis() - session.startTime > 300000) {
                    Bukkit.getScheduler().runTask(bukkitPlugin) {
                        player.sendMessage("&c登录超时，请重新尝试".colored())
                        loginSessions.remove(player.uniqueId)
                    }
                    return@runTaskTimerAsynchronously
                }
            } catch (e: Exception) {
                Bukkit.getScheduler().runTask(bukkitPlugin) {
                    player.sendMessage("&c检查登录状态时发生错误：${e.message}".colored())
                    loginSessions.remove(player.uniqueId)
                }
                return@runTaskTimerAsynchronously
            }
        }, 20L, 40L) // 每2秒检查一次
        
        session.pollingTask = task
    }

    /**
     * 检查Bilibili登录状态
     */
    private fun checkBilibiliLoginStatus(oauthKey: String): LoginStatusResult {
        val request = Request.Builder()
            .url("https://passport.bilibili.com/x/passport-login/web/qrcode/poll?qrcode_key=$oauthKey")
            .get()
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw IOException("响应为空")

        val jsonElement = gson.parse(responseBody)
        val jsonObject = jsonElement.asJsonObject

        val code = jsonObject.get("code").asInt
        val message = jsonObject.get("message").asString

        val cookies = if (code == 0) {
            // 登录成功，提取cookies
            response.headers("Set-Cookie").joinToString("; ")
        } else null

        return LoginStatusResult(code, message, cookies)
    }

    /**
     * 检查玩家登录状态
     */
    private fun checkLoginStatus(player: Player) {
        val session = loginSessions[player.uniqueId]
        if (session == null) {
            player.sendMessage("&c您当前没有登录会话".colored())
            return
        }

        val elapsed = (System.currentTimeMillis() - session.startTime) / 1000
        player.sendMessage("&a登录会话状态：".colored())
        player.sendMessage("&7- 已用时：${elapsed}秒".colored())
        player.sendMessage("&7- 状态：${if (session.isLoggedIn) "&a已登录" else "&e等待扫码"}".colored())
    }

    /**
     * 登出
     */
    private fun logout(player: Player) {
        val session = loginSessions.remove(player.uniqueId)
        if (session != null) {
            session.pollingTask?.cancel()
            player.sendMessage("&a已登出".colored())
        } else {
            player.sendMessage("&c您当前没有登录会话".colored())
        }
    }

    /**
     * 二维码数据类
     */
    data class QRCodeData(
        val url: String,
        val oauthKey: String
    )

    /**
     * 登录会话数据类
     */
    data class LoginSession(
        val oauthKey: String,
        val qrCodeUrl: String,
        val startTime: Long,
        var isLoggedIn: Boolean = false,
        var cookies: String? = null,
        var pollingTask: org.bukkit.scheduler.BukkitTask? = null
    )

    /**
     * 登录状态结果数据类
     */
    data class LoginStatusResult(
        val code: Int,
        val message: String,
        val cookies: String?
    )

    /**
     * 二维码地图渲染器
     */
    class QRCodeMapRenderer(private val qrUrl: String) : MapRenderer() {
        private var hasRendered = false

        override fun render(map: MapView, canvas: org.bukkit.map.MapCanvas, player: Player) {
            if (hasRendered) return
            
            try {
                val qrImage = generateQRCodeImage(qrUrl, 128, 128)
                
                for (x in 0 until 128) {
                    for (y in 0 until 128) {
                        val rgb = qrImage.getRGB(x, y)
                        val color = if (Color(rgb).red < 128) {
                            org.bukkit.map.MapPalette.BLACK
                        } else {
                            org.bukkit.map.MapPalette.WHITE
                        }
                        canvas.setPixel(x, y, color)
                    }
                }
                
                hasRendered = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun generateQRCodeImage(content: String, width: Int, height: Int): BufferedImage {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1
            )
            
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints)
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            
            for (x in 0 until width) {
                for (y in 0 until height) {
                    image.setRGB(x, y, if (bitMatrix[x, y]) Color.BLACK.rgb else Color.WHITE.rgb)
                }
            }
            
            return image
        }
    }
}