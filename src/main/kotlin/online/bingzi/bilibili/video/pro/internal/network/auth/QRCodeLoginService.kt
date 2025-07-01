package online.bingzi.bilibili.video.pro.internal.network.auth

import online.bingzi.bilibili.video.pro.internal.entity.netwrk.auth.LoginPollResult
import online.bingzi.bilibili.video.pro.internal.entity.netwrk.auth.QRCodeData
import online.bingzi.bilibili.video.pro.internal.entity.netwrk.auth.QRCodeResult
import online.bingzi.bilibili.video.pro.internal.network.BilibiliApiClient
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
        
        // 二维码状态码
        const val STATUS_SUCCESS = 0          // 登录成功
        const val STATUS_WAITING = 86038      // 二维码已失效
        const val STATUS_NOT_CONFIRMED = 86090 // 二维码有效，等待用户扫码
        const val STATUS_NOT_SCANNED = 86101   // 二维码有效，等待用户确认登录
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
     * @return LoginPollResult 登录轮询结果
     */
    fun pollLoginStatus(qrcodeKey: String): LoginPollResult {
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
                    // 登录成功，提取用户信息
                    val data = jsonResponse.getAsJsonObject("data")
                    val url = data?.get("url")?.asString ?: ""
                    
                    // 从URL中提取Cookie参数（通常在URL的query参数中）
                    extractAndSaveCookies(url)
                    
                    LoginPollResult.Success("登录成功")
                }
                STATUS_WAITING -> LoginPollResult.Expired("二维码已失效，请重新获取")
                STATUS_NOT_CONFIRMED -> LoginPollResult.WaitingConfirm("二维码已扫描，等待确认登录")
                STATUS_NOT_SCANNED -> LoginPollResult.WaitingScan("等待扫描二维码")
                else -> LoginPollResult.Error("未知状态码: $code, 消息: $message")
            }
            
        } catch (e: Exception) {
            return LoginPollResult.Error("轮询登录状态失败: ${e.message}")
        }
    }
    
    /**
     * 从重定向URL中提取并保存Cookie
     */
    private fun extractAndSaveCookies(url: String) {
        try {
            // 解析URL中的Cookie参数
            val uri = java.net.URI(url)
            val query = uri.query ?: return
            
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
            }
            
        } catch (e: Exception) {
            // 静默处理Cookie提取失败
            println("提取Cookie失败: ${e.message}")
        }
    }
    
    /**
     * 开始二维码登录流程
     * @param onQRCodeGenerated 二维码生成成功回调
     * @param onStatusChanged 状态改变回调
     * @param onLoginSuccess 登录成功回调
     * @param onError 错误回调
     */
    fun startQRCodeLogin(
        onQRCodeGenerated: (QRCodeData) -> Unit,
        onStatusChanged: (String) -> Unit,
        onLoginSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // 生成二维码
        when (val qrResult = generateQRCode()) {
            is QRCodeResult.Success -> {
                onQRCodeGenerated(qrResult.data)
                
                // 开始轮询
                val qrcodeKey = qrResult.data.qrcodeKey
                startPolling(qrcodeKey, onStatusChanged, onLoginSuccess, onError)
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
        onError: (String) -> Unit
    ) {
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                when (val result = pollLoginStatus(qrcodeKey)) {
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

