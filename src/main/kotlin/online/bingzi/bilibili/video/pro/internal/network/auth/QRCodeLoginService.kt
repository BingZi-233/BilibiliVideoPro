package online.bingzi.bilibili.video.pro.internal.network.auth

import online.bingzi.bilibili.video.pro.internal.entity.netwrk.auth.LoginPollResult
import online.bingzi.bilibili.video.pro.internal.entity.netwrk.auth.QRCodeData
import online.bingzi.bilibili.video.pro.internal.entity.netwrk.auth.QRCodeResult
import online.bingzi.bilibili.video.pro.internal.network.BilibiliApiClient
import online.bingzi.bilibili.video.pro.internal.database.service.PlayerBilibiliService
import taboolib.common.platform.function.info
import java.util.*

/**
 * 二维码登录服务
 * 实现Bilibili的二维码扫码登录功能
 */
class QRCodeLoginService(private val apiClient: BilibiliApiClient) {

    companion object {
        // 二维码登录相关API
        private const val QR_CODE_GENERATE_URL = "${BilibiliApiClient.PASSPORT_BASE_URL}/x/passport-login/web/qrcode/generate"
        private const val QR_CODE_POLL_URL = "${BilibiliApiClient.PASSPORT_BASE_URL}/x/passport-login/web/qrcode/poll"
        private const val USER_INFO_URL = "${BilibiliApiClient.API_BASE_URL}/x/web-interface/nav"

        // 二维码状态码
        const val STATUS_SUCCESS = 0          // 登录成功
        const val STATUS_EXPIRED = 86038      // 二维码已失效
        const val STATUS_NOT_CONFIRMED = 86090 // 二维码有效，等待用户确认登录
        const val STATUS_NOT_SCANNED = 86101   // 二维码有效，等待用户扫码
    }

    /**
     * 生成登录二维码
     * @return QRCodeData 包含二维码URL和key的数据类
     */
    fun generateQRCode(): QRCodeResult {
        try {
            val response = apiClient.get(QR_CODE_GENERATE_URL)

            if (!response.isSuccess) {
                return QRCodeResult.Error("网络请求失败: ${response.error}")
            }

            val jsonResponse = response.asJsonObject()
            if (jsonResponse == null) {
                return QRCodeResult.Error("响应数据解析失败")
            }

            val code = jsonResponse.get("code")?.asInt ?: -1
            if (code != 0) {
                val message = jsonResponse.get("message")?.asString ?: "未知错误"
                return QRCodeResult.Error("API错误: $message")
            }

            val data = jsonResponse.getAsJsonObject("data")
            if (data == null) {
                return QRCodeResult.Error("响应数据格式错误")
            }

            val url = data.get("url")?.asString
            val qrcodeKey = data.get("qrcode_key")?.asString

            if (url.isNullOrEmpty() || qrcodeKey.isNullOrEmpty()) {
                return QRCodeResult.Error("二维码数据不完整")
            }

            return QRCodeResult.Success(QRCodeData(url, qrcodeKey))

        } catch (e: Exception) {
            return QRCodeResult.Error("生成二维码失败: ${e.message}")
        }
    }

    /**
     * 轮询二维码登录状态
     * @param qrcodeKey 二维码密钥
     * @param playerUuid 玩家UUID（用于绑定）
     * @param playerName 玩家名称（用于绑定）
     * @return LoginPollResult 登录轮询结果
     */
    fun pollLoginStatus(qrcodeKey: String, playerUuid: String? = null, playerName: String? = null): LoginPollResult {
        try {
            val params = mapOf("qrcode_key" to qrcodeKey)
            val response = apiClient.get(QR_CODE_POLL_URL, params)

            if (!response.isSuccess) {
                return LoginPollResult.Error("网络请求失败: ${response.error}")
            }

            val jsonResponse = response.asJsonObject()
            if (jsonResponse == null) {
                return LoginPollResult.Error("响应数据解析失败")
            }

            val code = jsonResponse.get("code")?.asInt ?: -1
            val message = jsonResponse.get("message")?.asString ?: ""

            return when (code) {
                STATUS_SUCCESS -> {
                    info("QR码登录成功，开始处理Cookie...")
                    // 登录成功，检查Cookie是否已被自动保存到CookieJar
                    val cookieData = getCookiesFromJar()
                    
                    info("登录成功，玩家信息 - UUID: $playerUuid, 名称: $playerName")
                    
                    // 如果提供了玩家信息，保存到数据库
                    if (!playerUuid.isNullOrEmpty() && !playerName.isNullOrEmpty() && cookieData != null) {
                        info("开始保存Cookie到数据库...")
                        saveCookiesToDatabase(playerUuid, playerName, cookieData)
                    } else {
                        info("跳过数据库保存:")
                        info("  - 玩家UUID: ${if (playerUuid.isNullOrEmpty()) "空" else "有值"}")
                        info("  - 玩家名称: ${if (playerName.isNullOrEmpty()) "空" else "有值"}")
                        info("  - Cookie数据: ${if (cookieData == null) "空" else "有值"}")
                    }

                    LoginPollResult.Success("登录成功")
                }

                STATUS_EXPIRED -> LoginPollResult.Expired("二维码已失效，请重新获取")
                STATUS_NOT_CONFIRMED -> LoginPollResult.WaitingConfirm("二维码已扫描，等待确认登录")
                STATUS_NOT_SCANNED -> LoginPollResult.WaitingScan("等待扫描二维码")
                else -> LoginPollResult.Error("未知状态码: $code, 消息: $message")
            }

        } catch (e: Exception) {
            return LoginPollResult.Error("轮询登录状态失败: ${e.message}")
        }
    }

    /**
     * 从CookieJar获取登录Cookie
     * @return Map<String, String>? 返回Cookie数据，失败时返回null
     */
    private fun getCookiesFromJar(): Map<String, String>? {
        try {
            val cookies = apiClient.getCookies()
            info("从CookieJar获取到的所有Cookie: $cookies")
            
            // 检查必需的Cookie是否存在
            val requiredCookies = listOf("SESSDATA", "bili_jct", "DedeUserID", "DedeUserID__ckMd5")
            val availableCookies = mutableMapOf<String, String>()
            
            requiredCookies.forEach { cookieName ->
                val cookieValue = cookies[cookieName]
                if (!cookieValue.isNullOrEmpty()) {
                    availableCookies[cookieName] = cookieValue
                    info("找到Cookie: $cookieName = ${cookieValue.take(10)}...")
                } else {
                    info("缺少Cookie: $cookieName")
                }
            }
            
            return if (availableCookies.size >= 3) { // 至少需要SESSDATA、bili_jct、DedeUserID
                info("Cookie数据完整，可以保存到数据库")
                availableCookies
            } else {
                info("Cookie数据不完整: 只有 ${availableCookies.keys}")
                null
            }
        } catch (e: Exception) {
            info("从CookieJar获取Cookie失败: ${e.message}")
            return null
        }
    }

    /**
     * 从重定向URL中提取并保存Cookie
     * @return Map<String, String>? 返回提取的Cookie数据，失败时返回null
     */
    private fun extractAndSaveCookies(url: String): Map<String, String>? {
        try {
            // 解析URL中的Cookie参数
            val uri = java.net.URI(url)
            val query = uri.query ?: return null

            val params = query.split("&").associate { param ->
                val parts = param.split("=", limit = 2)
                if (parts.size == 2) {
                    parts[0] to java.net.URLDecoder.decode(parts[1], "UTF-8")
                } else {
                    parts[0] to ""
                }
            }

            // 常见的重要Cookie字段
            val importantCookies = mutableMapOf<String, String>()

            // 检查并提取重要的登录Cookie
            listOf("SESSDATA", "bili_jct", "DedeUserID", "DedeUserID__ckMd5").forEach { cookieName ->
                params[cookieName]?.let { value ->
                    if (value.isNotEmpty()) {
                        importantCookies[cookieName] = value
                    }
                }
            }

            if (importantCookies.isNotEmpty()) {
                apiClient.setCookies(importantCookies)
                return importantCookies
            }

            return null

        } catch (e: Exception) {
            // 静默处理Cookie提取失败
            info("提取Cookie失败: ${e.message}")
            return null
        }
    }

    /**
     * 保存Cookie到数据库
     * @param playerUuid 玩家UUID
     * @param playerName 玩家名称
     * @param cookieData Cookie数据
     */
    private fun saveCookiesToDatabase(playerUuid: String, playerName: String, cookieData: Map<String, String>) {
        try {
            info("开始保存Cookie到数据库...")
            info("玩家UUID: $playerUuid")
            info("玩家名称: $playerName")
            info("Cookie数据: ${cookieData.keys}")
            
            // 获取Bilibili用户信息
            val userInfo = fetchBilibiliUserInfo()
            if (userInfo == null) {
                info("无法获取Bilibili用户信息，跳过数据库保存")
                return
            }
            
            info("获取到用户信息: $userInfo")
            
            val sessdata = cookieData["SESSDATA"] ?: ""
            val biliJct = cookieData["bili_jct"] ?: ""
            val dedeUserId = cookieData["DedeUserID"] ?: ""
            val dedeUserIdMd5 = cookieData["DedeUserID__ckMd5"] ?: ""
            
            // 检查Cookie是否完整
            if (sessdata.isEmpty() || biliJct.isEmpty() || dedeUserId.isEmpty()) {
                info("Cookie数据不完整，无法保存到数据库")
                info("SESSDATA: ${if (sessdata.isEmpty()) "空" else "有值"}")
                info("bili_jct: ${if (biliJct.isEmpty()) "空" else "有值"}")
                info("DedeUserID: ${if (dedeUserId.isEmpty()) "空" else "有值"}")
                info("DedeUserID__ckMd5: ${if (dedeUserIdMd5.isEmpty()) "空" else "有值"}")
                return
            }

            info("Cookie验证通过，准备保存到数据库...")

            // 检查是否已存在绑定
            val existingBinding = PlayerBilibiliService.findByPlayerUuid(playerUuid)
            if (existingBinding != null) {
                // 更新现有绑定的Cookie
                info("更新现有绑定的Cookie...")
                PlayerBilibiliService.updateCookie(playerUuid, sessdata, biliJct, dedeUserId, dedeUserIdMd5)
                info("已更新玩家 $playerName 的Bilibili登录信息")
            } else {
                // 创建新绑定
                info("创建新的绑定记录...")
                val bilibiliUid = userInfo["mid"]?.toLongOrNull() ?: 0L
                val bilibiliUsername = userInfo["uname"] ?: ""
                
                info("B站UID: $bilibiliUid")
                info("B站用户名: $bilibiliUsername")
                
                if (bilibiliUid > 0 && bilibiliUsername.isNotEmpty()) {
                    val result = PlayerBilibiliService.createBinding(
                        playerUuid = playerUuid,
                        playerName = playerName,
                        bilibiliUserId = bilibiliUid,
                        bilibiliUsername = bilibiliUsername,
                        sessdata = sessdata,
                        biliJct = biliJct,
                        dedeUserId = dedeUserId,
                        dedeUserIdMd5 = dedeUserIdMd5
                    )
                    if (result != null) {
                        info("已为玩家 $playerName 创建Bilibili绑定: $bilibiliUsername")
                    } else {
                        info("创建绑定失败")
                    }
                } else {
                    info("无法获取有效的Bilibili用户信息: UID=$bilibiliUid, 用户名=$bilibiliUsername")
                }
            }
        } catch (e: Exception) {
            info("保存Cookie到数据库失败: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 获取Bilibili用户信息
     * @return Map<String, String>? 用户信息，失败时返回null
     */
    private fun fetchBilibiliUserInfo(): Map<String, String>? {
        try {
            info("正在获取Bilibili用户信息...")
            val response = apiClient.get(USER_INFO_URL)
            
            if (!response.isSuccess) {
                info("获取用户信息失败: ${response.error}")
                return null
            }

            val jsonResponse = response.asJsonObject()
            if (jsonResponse == null) {
                info("用户信息响应解析失败")
                return null
            }
            
            val code = jsonResponse.get("code")?.asInt ?: -1
            info("用户信息API响应码: $code")
            
            if (code != 0) {
                val message = jsonResponse.get("message")?.asString ?: "unknown"
                info("用户信息API错误: $message")
                return null
            }

            val data = jsonResponse.getAsJsonObject("data")
            if (data == null) {
                info("用户信息数据为空")
                return null
            }
            
            val userInfo = mapOf(
                "mid" to (data.get("mid")?.asString ?: ""),
                "uname" to (data.get("uname")?.asString ?: ""),
                "face" to (data.get("face")?.asString ?: ""),
                "level" to (data.get("level_info")?.asJsonObject?.get("current_level")?.asString ?: "0"),
                "vip_status" to (data.get("vip")?.asJsonObject?.get("status")?.asString ?: "0")
            )
            
            info("成功获取用户信息: $userInfo")
            return userInfo
        } catch (e: Exception) {
            info("获取用户信息异常: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    /**
     * 开始二维码登录流程
     * @param onQRCodeGenerated 二维码生成成功回调
     * @param onStatusChanged 状态改变回调
     * @param onLoginSuccess 登录成功回调
     * @param onError 错误回调
     * @param playerUuid 玩家UUID（可选，用于保存Cookie到数据库）
     * @param playerName 玩家名称（可选，用于保存Cookie到数据库）
     */
    fun startQRCodeLogin(
        onQRCodeGenerated: (QRCodeData) -> Unit,
        onStatusChanged: (String) -> Unit,
        onLoginSuccess: () -> Unit,
        onError: (String) -> Unit,
        playerUuid: String? = null,
        playerName: String? = null
    ) {
        // 生成二维码
        when (val qrResult = generateQRCode()) {
            is QRCodeResult.Success -> {
                onQRCodeGenerated(qrResult.data)

                // 开始轮询
                val qrcodeKey = qrResult.data.qrcodeKey
                startPolling(qrcodeKey, onStatusChanged, onLoginSuccess, onError, playerUuid, playerName)
            }

            is QRCodeResult.Error -> {
                onError(qrResult.message)
            }
        }
    }

    /**
     * 开始轮询登录状态
     */
    private fun startPolling(
        qrcodeKey: String,
        onStatusChanged: (String) -> Unit,
        onLoginSuccess: () -> Unit,
        onError: (String) -> Unit,
        playerUuid: String? = null,
        playerName: String? = null
    ) {
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                when (val result = pollLoginStatus(qrcodeKey, playerUuid, playerName)) {
                    is LoginPollResult.Success -> {
                        timer.cancel()
                        onLoginSuccess()
                    }

                    is LoginPollResult.Expired -> {
                        timer.cancel()
                        onError(result.message)
                    }

                    is LoginPollResult.Error -> {
                        timer.cancel()
                        onError(result.message)
                    }

                    is LoginPollResult.WaitingScan -> {
                        onStatusChanged(result.message)
                    }

                    is LoginPollResult.WaitingConfirm -> {
                        onStatusChanged(result.message)
                    }
                }
            }
        }, 0, 3000) // 每3秒轮询一次
    }
}

