package online.bingzi.bilibili.video.pro.internal.network

import online.bingzi.bilibili.video.pro.internal.network.auth.CookieRefreshService
import online.bingzi.bilibili.video.pro.internal.network.auth.QRCodeLoginService
import online.bingzi.bilibili.video.pro.internal.network.video.VideoInteractionService

/**
 * Bilibili网络管理器
 * 整合所有网络服务，提供统一的访问接口
 */
class BilibiliNetworkManager {
    
    // API客户端实例
    private val apiClient = BilibiliApiClient()
    
    // 各种服务实例
    val qrCodeLogin: QRCodeLoginService by lazy { QRCodeLoginService(apiClient) }
    val videoInteraction: VideoInteractionService by lazy { VideoInteractionService(apiClient) }
    val cookieRefresh: CookieRefreshService by lazy { CookieRefreshService(apiClient) }
    
    /**
     * 获取API客户端
     */
    fun getApiClient(): BilibiliApiClient = apiClient
    
    /**
     * 设置Cookie（用于手动设置登录状态）
     * @param cookies Cookie映射
     */
    fun setCookies(cookies: Map<String, String>) {
        apiClient.setCookies(cookies)
    }
    
    /**
     * 获取当前所有Cookie
     * @return Cookie映射
     */
    fun getCookies(): Map<String, String> {
        return apiClient.getCookies()
    }
    
    /**
     * 检查是否已登录
     * @return Boolean 是否已登录
     */
    fun isLoggedIn(): Boolean {
        return apiClient.isLoggedIn()
    }
    
    /**
     * 清除所有Cookie（登出）
     */
    fun logout() {
        apiClient.clearCookies()
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