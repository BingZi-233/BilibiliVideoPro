package online.bingzi.bilibili.video.pro.internal.network.auth

import online.bingzi.bilibili.video.pro.internal.entity.netwrk.auth.CookieRefreshResult
import online.bingzi.bilibili.video.pro.internal.entity.netwrk.auth.LoginStatusResult
import online.bingzi.bilibili.video.pro.internal.entity.netwrk.auth.LoginUserInfo
import online.bingzi.bilibili.video.pro.internal.network.BilibiliApiClient
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Cookie刷新服务
 * 提供Web端Cookie自动刷新功能，保持登录状态有效
 */
class CookieRefreshService(private val apiClient: BilibiliApiClient) {

    companion object {
        // Cookie刷新相关API
        private const val COOKIE_REFRESH_URL = "${BilibiliApiClient.PASSPORT_BASE_URL}/x/passport-login/web/cookie/refresh"
        private const val LOGIN_INFO_URL = "${BilibiliApiClient.API_BASE_URL}/x/web-interface/nav"

        // 刷新间隔（默认每25天刷新一次，Cookie有效期为30天）
        private const val DEFAULT_REFRESH_INTERVAL_DAYS = 25L
        private const val DEFAULT_REFRESH_INTERVAL_HOURS = DEFAULT_REFRESH_INTERVAL_DAYS * 24
    }

    private var refreshExecutor: ScheduledExecutorService? = null
    private var isAutoRefreshEnabled = false

    /**
     * 手动刷新Cookie
     * @return CookieRefreshResult 刷新结果
     */
    fun refreshCookie(): CookieRefreshResult {
        try {
            // 检查是否已登录
            if (!apiClient.isLoggedIn()) {
                return CookieRefreshResult.Error("用户未登录，无法刷新Cookie")
            }

            // 获取当前Cookie中的csrf
            val currentCookies = apiClient.getCookies()
            val csrf = currentCookies["bili_jct"]

            if (csrf.isNullOrEmpty()) {
                return CookieRefreshResult.Error("缺少必要的CSRF令牌")
            }

            // 发送刷新请求
            val data = mapOf(
                "csrf" to csrf,
                "refresh_csrf" to csrf
            )

            val response = apiClient.post(COOKIE_REFRESH_URL, data)

            if (!response.isSuccess) {
                return CookieRefreshResult.Error("刷新请求失败: ${response.error}")
            }

            val jsonResponse = response.asJsonObject()
            if (jsonResponse == null) {
                return CookieRefreshResult.Error("响应数据解析失败")
            }

            val code = jsonResponse.get("code")?.asInt ?: -1
            val message = jsonResponse.get("message")?.asString ?: "未知错误"

            when (code) {
                0 -> {
                    // 刷新成功，更新Cookie信息
                    val dataObj = jsonResponse.getAsJsonObject("data")
                    val refreshToken = dataObj?.get("refresh_token")?.asString

                    // 这里需要重新获取用户信息来更新所有Cookie
                    val loginInfoResult = checkLoginStatus()
                    if (loginInfoResult is LoginStatusResult.Success) {
                        return CookieRefreshResult.Success("Cookie刷新成功", refreshToken)
                    } else {
                        return CookieRefreshResult.Success("Cookie刷新成功（部分信息未更新）", refreshToken)
                    }
                }

                -101 -> return CookieRefreshResult.Error("账号未登录")
                -111 -> return CookieRefreshResult.Error("csrf校验失败")
                -400 -> return CookieRefreshResult.Error("请求错误")
                86095 -> return CookieRefreshResult.Error("refresh_csrf错误或refresh_token已过期")
                else -> return CookieRefreshResult.Error("刷新失败: $message (code: $code)")
            }

        } catch (e: Exception) {
            return CookieRefreshResult.Error("Cookie刷新异常: ${e.message}")
        }
    }

    /**
     * 检查登录状态并获取用户信息
     * @return LoginStatusResult 登录状态结果
     */
    fun checkLoginStatus(): LoginStatusResult {
        try {
            val response = apiClient.get(LOGIN_INFO_URL)

            if (!response.isSuccess) {
                return LoginStatusResult.Error("获取登录信息失败: ${response.error}")
            }

            val jsonResponse = response.asJsonObject()
            if (jsonResponse == null) {
                return LoginStatusResult.Error("响应数据解析失败")
            }

            val code = jsonResponse.get("code")?.asInt ?: -1
            if (code != 0) {
                val message = jsonResponse.get("message")?.asString ?: "未知错误"
                return LoginStatusResult.Error("API错误: $message")
            }

            val data = jsonResponse.getAsJsonObject("data")
            if (data == null) {
                return LoginStatusResult.Error("响应数据格式错误")
            }

            val isLogin = data.get("isLogin")?.asBoolean ?: false
            if (!isLogin) {
                return LoginStatusResult.NotLoggedIn("用户未登录")
            }

            // 提取用户信息
            val mid = data.get("mid")?.asLong ?: 0L
            val uname = data.get("uname")?.asString ?: ""
            val face = data.get("face")?.asString ?: ""
            val level = data.get("level_info")?.asJsonObject?.get("current_level")?.asInt ?: 0
            val vipStatus = data.get("vipStatus")?.asInt ?: 0

            val userInfo = LoginUserInfo(mid, uname, face, level, vipStatus > 0)
            return LoginStatusResult.Success(userInfo)

        } catch (e: Exception) {
            return LoginStatusResult.Error("检查登录状态异常: ${e.message}")
        }
    }

    /**
     * 启动自动Cookie刷新
     * @param intervalHours 刷新间隔（小时），默认为25天
     */
    fun startAutoRefresh(intervalHours: Long = DEFAULT_REFRESH_INTERVAL_HOURS) {
        if (isAutoRefreshEnabled) {
            return // 已经启动了
        }

        refreshExecutor = Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "BilibiliCookieRefreshThread").apply {
                isDaemon = true
            }
        }

        refreshExecutor?.scheduleAtFixedRate({
            try {
                val result = refreshCookie()
                when (result) {
                    is CookieRefreshResult.Success -> {
                        println("自动Cookie刷新成功: ${result.message}")
                    }

                    is CookieRefreshResult.Error -> {
                        println("自动Cookie刷新失败: ${result.message}")
                        // 如果是登录状态失效，可以考虑停止自动刷新
                        if (result.message.contains("未登录") || result.message.contains("过期")) {
                            stopAutoRefresh()
                        }
                    }
                }
            } catch (e: Exception) {
                println("自动Cookie刷新异常: ${e.message}")
            }
        }, intervalHours, intervalHours, TimeUnit.HOURS)

        isAutoRefreshEnabled = true
        println("已启动自动Cookie刷新，间隔: ${intervalHours}小时")
    }

    /**
     * 停止自动Cookie刷新
     */
    fun stopAutoRefresh() {
        refreshExecutor?.shutdown()
        refreshExecutor = null
        isAutoRefreshEnabled = false
        println("已停止自动Cookie刷新")
    }

    /**
     * 检查自动刷新是否启用
     */
    fun isAutoRefreshEnabled(): Boolean {
        return isAutoRefreshEnabled
    }

    /**
     * 验证Cookie有效性
     * @return Boolean Cookie是否有效
     */
    fun validateCookie(): Boolean {
        val loginStatus = checkLoginStatus()
        return loginStatus is LoginStatusResult.Success
    }

    /**
     * 获取Cookie过期时间估算
     * 注意：这个方法只是估算，实际过期时间可能有差异
     * @return Long? 估算的过期时间戳（毫秒），如果无法估算则返回null
     */
    fun estimateCookieExpiration(): Long? {
        try {
            val cookies = apiClient.getCookies()
            if (!cookies.containsKey("SESSDATA")) return null

            // SESSDATA的过期时间通常编码在其中，但具体解析方式可能会变化
            // 这里使用一个简单的估算方法：从当前时间开始计算30天
            val currentTime = System.currentTimeMillis()
            val thirtyDaysInMillis = 30L * 24L * 60L * 60L * 1000L

            return currentTime + thirtyDaysInMillis

        } catch (e: Exception) {
            return null
        }
    }

    /**
     * 检查Cookie是否即将过期
     * @param thresholdDays 阈值天数，默认5天
     * @return Boolean 是否即将过期
     */
    fun isCookieExpiringSoon(thresholdDays: Int = 5): Boolean {
        val expirationTime = estimateCookieExpiration() ?: return true
        val currentTime = System.currentTimeMillis()
        val thresholdTime = thresholdDays * 24L * 60L * 60L * 1000L

        return (expirationTime - currentTime) <= thresholdTime
    }
}

