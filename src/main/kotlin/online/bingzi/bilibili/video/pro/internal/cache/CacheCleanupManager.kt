package online.bingzi.bilibili.video.pro.internal.cache

import online.bingzi.bilibili.video.pro.api.event.CacheCleanupEvent
import online.bingzi.bilibili.video.pro.api.entity.event.CacheCleanupType
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import taboolib.common.platform.event.EventBus
import taboolib.module.lang.sendInfo
import java.util.concurrent.ConcurrentHashMap

/**
 * 缓存清理管理器
 * 定期清理过期的缓存数据，防止内存泄漏
 */
object CacheCleanupManager {
    
    // 冷却时间缓存（玩家UUID -> 过期时间）
    private val playerCooldownCache = ConcurrentHashMap<String, Long>()
    // 视频冷却时间缓存（玩家UUID_BV号 -> 过期时间）
    private val videoCooldownCache = ConcurrentHashMap<String, Long>()
    // 登录会话缓存（玩家UUID -> 会话信息）
    private val loginSessionCache = ConcurrentHashMap<String, LoginSessionInfo>()
    
    // 清理间隔（毫秒）
    private const val CLEANUP_INTERVAL = 300000L // 5分钟
    // 数据过期时间
    private const val PLAYER_COOLDOWN_EXPIRE = 600000L // 10分钟
    private const val VIDEO_COOLDOWN_EXPIRE = 3600000L // 1小时
    private const val LOGIN_SESSION_EXPIRE = 600000L // 10分钟
    
    data class LoginSessionInfo(
        val qrcodeKey: String,
        val createTime: Long = System.currentTimeMillis()
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() - createTime > LOGIN_SESSION_EXPIRE
    }
    
    /**
     * 插件启用时开始定期清理
     */
    @Awake(LifeCycle.ENABLE)
    fun startCleanupTask() {
        console().sendInfo("cacheCleanupStarted")
        
        submit(async = true, delay = CLEANUP_INTERVAL, period = CLEANUP_INTERVAL) {
            performCleanup()
        }
    }
    
    /**
     * 执行清理操作
     */
    private fun performCleanup() {
        try {
            val now = System.currentTimeMillis()
            var totalCleaned = 0
            var memoryFreed = 0L
            
            // 清理过期的玩家冷却时间
            val expiredPlayerCooldowns = playerCooldownCache.filter { (_, expireTime) ->
                now > expireTime
            }.keys
            
            val playerCooldownsRemoved = expiredPlayerCooldowns.size
            expiredPlayerCooldowns.forEach { playerUuid ->
                playerCooldownCache.remove(playerUuid)
                totalCleaned++
            }
            
            if (playerCooldownsRemoved > 0) {
                val cleanupEvent = CacheCleanupEvent(
                    CacheCleanupType.PLAYER_COOLDOWN,
                    playerCooldownsRemoved,
                    playerCooldownsRemoved * 64L  // 估算内存释放
                )
                EventBus.callEvent(cleanupEvent)
                memoryFreed += cleanupEvent.memoryFreed
            }
            
            // 清理过期的视频冷却时间
            val expiredVideoCooldowns = videoCooldownCache.filter { (_, expireTime) ->
                now > expireTime
            }.keys
            
            val videoCooldownsRemoved = expiredVideoCooldowns.size
            expiredVideoCooldowns.forEach { videoKey ->
                videoCooldownCache.remove(videoKey)
                totalCleaned++
            }
            
            if (videoCooldownsRemoved > 0) {
                val cleanupEvent = CacheCleanupEvent(
                    CacheCleanupType.VIDEO_COOLDOWN,
                    videoCooldownsRemoved,
                    videoCooldownsRemoved * 128L  // 估算内存释放
                )
                EventBus.callEvent(cleanupEvent)
                memoryFreed += cleanupEvent.memoryFreed
            }
            
            // 清理过期的登录会话
            val expiredLoginSessions = loginSessionCache.filter { (_, sessionInfo) ->
                sessionInfo.isExpired()
            }.keys
            
            val loginSessionsRemoved = expiredLoginSessions.size
            expiredLoginSessions.forEach { playerUuid ->
                loginSessionCache.remove(playerUuid)
                totalCleaned++
            }
            
            if (loginSessionsRemoved > 0) {
                val cleanupEvent = CacheCleanupEvent(
                    CacheCleanupType.LOGIN_SESSION,
                    loginSessionsRemoved,
                    loginSessionsRemoved * 256L  // 估算内存释放
                )
                EventBus.callEvent(cleanupEvent)
                memoryFreed += cleanupEvent.memoryFreed
            }
            
            if (totalCleaned > 0) {
                console().sendInfo("cacheCleanupCompleted", totalCleaned.toString())
                
                // 发布总清理事件
                val totalCleanupEvent = CacheCleanupEvent(
                    CacheCleanupType.ALL,
                    totalCleaned,
                    memoryFreed
                )
                EventManager.publishEvent(totalCleanupEvent)
            }
            
            // 记录缓存统计信息
            logCacheStatistics()
            
        } catch (e: Exception) {
            console().sendInfo("cacheCleanupError", e.message ?: "unknown")
        }
    }
    
    /**
     * 记录缓存统计信息
     */
    private fun logCacheStatistics() {
        val playerCooldownSize = playerCooldownCache.size
        val videoCooldownSize = videoCooldownCache.size
        val loginSessionSize = loginSessionCache.size
        
        console().sendInfo("cacheStatistics", playerCooldownSize.toString(), videoCooldownSize.toString(), loginSessionSize.toString())
    }
    
    /**
     * 设置玩家冷却时间
     */
    fun setPlayerCooldown(playerUuid: String, cooldownSeconds: Int) {
        val expireTime = System.currentTimeMillis() + (cooldownSeconds * 1000)
        playerCooldownCache[playerUuid] = expireTime
    }
    
    /**
     * 检查玩家是否在冷却中
     */
    fun isPlayerOnCooldown(playerUuid: String): Boolean {
        val expireTime = playerCooldownCache[playerUuid] ?: return false
        if (System.currentTimeMillis() > expireTime) {
            playerCooldownCache.remove(playerUuid)
            return false
        }
        return true
    }
    
    /**
     * 获取玩家冷却剩余时间（秒）
     */
    fun getPlayerCooldownRemaining(playerUuid: String): Long {
        val expireTime = playerCooldownCache[playerUuid] ?: return 0
        val remaining = (expireTime - System.currentTimeMillis()) / 1000
        return if (remaining > 0) remaining else 0
    }
    
    /**
     * 设置视频冷却时间
     */
    fun setVideoCooldown(playerUuid: String, bvid: String, cooldownSeconds: Int) {
        val videoKey = "${playerUuid}_${bvid}"
        val expireTime = System.currentTimeMillis() + (cooldownSeconds * 1000)
        videoCooldownCache[videoKey] = expireTime
    }
    
    /**
     * 检查视频是否在冷却中
     */
    fun isVideoOnCooldown(playerUuid: String, bvid: String): Boolean {
        val videoKey = "${playerUuid}_${bvid}"
        val expireTime = videoCooldownCache[videoKey] ?: return false
        if (System.currentTimeMillis() > expireTime) {
            videoCooldownCache.remove(videoKey)
            return false
        }
        return true
    }
    
    /**
     * 获取视频冷却剩余时间（秒）
     */
    fun getVideoCooldownRemaining(playerUuid: String, bvid: String): Long {
        val videoKey = "${playerUuid}_${bvid}"
        val expireTime = videoCooldownCache[videoKey] ?: return 0
        val remaining = (expireTime - System.currentTimeMillis()) / 1000
        return if (remaining > 0) remaining else 0
    }
    
    /**
     * 设置登录会话
     */
    fun setLoginSession(playerUuid: String, qrcodeKey: String) {
        loginSessionCache[playerUuid] = LoginSessionInfo(qrcodeKey)
    }
    
    /**
     * 获取登录会话
     */
    fun getLoginSession(playerUuid: String): String? {
        val session = loginSessionCache[playerUuid]
        return if (session != null && !session.isExpired()) {
            session.qrcodeKey
        } else {
            loginSessionCache.remove(playerUuid)
            null
        }
    }
    
    /**
     * 移除登录会话
     */
    fun removeLoginSession(playerUuid: String) {
        loginSessionCache.remove(playerUuid)
    }
    
    /**
     * 检查登录会话是否存在
     */
    fun hasLoginSession(playerUuid: String): Boolean {
        return getLoginSession(playerUuid) != null
    }
    
    /**
     * 获取缓存统计信息
     */
    fun getCacheStatistics(): Map<String, Any> {
        return mapOf(
            "player_cooldowns" to playerCooldownCache.size,
            "video_cooldowns" to videoCooldownCache.size,
            "login_sessions" to loginSessionCache.size,
            "total_memory_usage" to estimateMemoryUsage()
        )
    }
    
    /**
     * 估算内存使用量（字节）
     */
    private fun estimateMemoryUsage(): Long {
        // 粗略估算每个缓存项的内存使用
        val playerCooldownMemory = playerCooldownCache.size * 100L // UUID字符串 + Long
        val videoCooldownMemory = videoCooldownCache.size * 150L // UUID_BV字符串 + Long
        val loginSessionMemory = loginSessionCache.size * 200L // UUID + SessionInfo
        
        return playerCooldownMemory + videoCooldownMemory + loginSessionMemory
    }
    
    /**
     * 手动清理所有缓存
     */
    fun clearAllCaches() {
        playerCooldownCache.clear()
        videoCooldownCache.clear()
        loginSessionCache.clear()
        console().sendInfo("cacheAllCleared")
    }
}