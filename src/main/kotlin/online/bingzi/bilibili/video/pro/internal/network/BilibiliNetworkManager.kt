package online.bingzi.bilibili.video.pro.internal.network

import online.bingzi.bilibili.video.pro.internal.network.auth.CookieRefreshService
import online.bingzi.bilibili.video.pro.internal.network.auth.QRCodeLoginService
import online.bingzi.bilibili.video.pro.internal.network.video.VideoInteractionService
import online.bingzi.bilibili.video.pro.internal.database.service.PlayerBilibiliService
import taboolib.common.platform.function.info

/**
 * Bilibili网络管理器
 * 整合所有网络服务，提供统一的访问接口
 */
class BilibiliNetworkManager {

    // API客户端实例
    private val _apiClient = BilibiliApiClient()

    // 各种服务实例
    val qrCodeLogin: QRCodeLoginService by lazy { QRCodeLoginService(_apiClient) }
    val videoInteraction: VideoInteractionService by lazy { VideoInteractionService(_apiClient) }
    val cookieRefresh: CookieRefreshService by lazy { CookieRefreshService(_apiClient) }

    /**
     * 暴露apiClient属性，用于直接访问
     */
    val apiClient: BilibiliApiClient
        get() = _apiClient

    /**
     * 设置Cookie（用于手动设置登录状态）
     * @param cookies Cookie映射
     */
    fun setCookies(cookies: Map<String, String>) {
        _apiClient.setCookies(cookies)
    }

    /**
     * 获取当前所有Cookie
     * @return Cookie映射
     */
    fun getCookies(): Map<String, String> {
        return _apiClient.getCookies()
    }

    /**
     * 检查是否已登录
     * @return Boolean 是否已登录
     */
    fun isLoggedIn(): Boolean {
        return _apiClient.isLoggedIn()
    }

    /**
     * 为指定玩家加载Cookie到ApiClient
     * @param playerUuid 玩家UUID
     * @return Boolean 加载是否成功
     */
    fun loadPlayerCookies(playerUuid: String): Boolean {
        return try {
            val binding = PlayerBilibiliService.findByPlayerUuid(playerUuid)
            if (binding != null && binding.hasValidCookies()) {
                val cookieMap = binding.getCookieMap()
                _apiClient.setCookies(cookieMap)
                info("已为玩家 $playerUuid 加载Cookie")
                true
            } else {
                info("玩家 $playerUuid 没有有效的Cookie绑定")
                false
            }
        } catch (e: Exception) {
            info("加载玩家 $playerUuid 的Cookie失败: ${e.message}")
            false
        }
    }

    /**
     * 清除所有Cookie（登出）
     */
    fun logout() {
        _apiClient.clearCookies()
        cookieRefresh.stopAutoRefresh()
    }

    /**
     * 初始化网络管理器
     * 启动必要的后台服务
     */
    fun initialize() {
        // 如果已经登录，启动自动Cookie刷新
        if (isLoggedIn()) {
            cookieRefresh.startAutoRefresh()
        }
    }

    /**
     * 销毁网络管理器
     * 停止所有后台服务
     */
    fun destroy() {
        cookieRefresh.stopAutoRefresh()
    }

    companion object {
        @JvmStatic
        private var instance: BilibiliNetworkManager? = null

        /**
         * 获取单例实例
         */
        @JvmStatic
        fun getInstance(): BilibiliNetworkManager {
            if (instance == null) {
                synchronized(BilibiliNetworkManager::class.java) {
                    if (instance == null) {
                        instance = BilibiliNetworkManager()
                    }
                }
            }
            return instance!!
        }

        /**
         * 销毁单例实例
         */
        @JvmStatic
        fun destroyInstance() {
            instance?.destroy()
            instance = null
        }
    }
} 