package online.bingzi.bilibili.video.pro.internal.network

import kotlinx.coroutines.*
import taboolib.common.platform.function.console
import java.util.concurrent.ConcurrentHashMap

/**
 * Bilibili API管理器
 *
 * 提供单例的API客户端管理，支持多用户Cookie管理和API调用统计
 * 使用协程进行异步操作，避免阻塞主线程
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
object BilibiliApiManager {

    /**
     * API客户端实例
     */
    private val apiClient: BilibiliApiClient by lazy { BilibiliApiClient() }

    /**
     * API接口实例
     */
    private val api: BilibiliApi by lazy { BilibiliApi(apiClient) }

    /**
     * 用户Cookie缓存
     * key: 用户UID, value: Cookie字符串
     */
    private val userCookies = ConcurrentHashMap<String, String>()

    /**
     * API调用统计
     */
    private val apiStats = ConcurrentHashMap<String, Long>()

    /**
     * 协程作用域
     */
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * 添加用户Cookie
     *
     * @param uid 用户UID
     * @param cookie Cookie字符串
     */
    fun addUserCookie(uid: String, cookie: String) {
        userCookies[uid] = cookie
        console().sendMessage("已添加用户 $uid 的Cookie")
    }

    /**
     * 移除用户Cookie
     *
     * @param uid 用户UID
     */
    fun removeUserCookie(uid: String) {
        userCookies.remove(uid)
        console().sendMessage("已移除用户 $uid 的Cookie")
    }

    /**
     * 获取用户Cookie
     *
     * @param uid 用户UID
     * @return Cookie字符串，如果不存在返回null
     */
    fun getUserCookie(uid: String): String? = userCookies[uid]

    /**
     * 异步获取视频信息
     *
     * @param bvid 视频BV号
     * @param callback 回调函数
     */
    fun getVideoInfoAsync(bvid: String, callback: (BilibiliApi.VideoInfo?) -> Unit) {
        scope.launch {
            try {
                incrementApiCall("getVideoInfo")
                val videoInfo = api.getVideoInfo(bvid)
                withContext(Dispatchers.Main) {
                    callback(videoInfo)
                }
            } catch (e: Exception) {
                console().sendMessage("获取视频信息失败: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }

    /**
     * 异步获取用户三连状态
     *
     * @param uid 用户UID
     * @param bvid 视频BV号
     * @param callback 回调函数
     */
    fun getTripleStatusAsync(uid: String, bvid: String, callback: (BilibiliApi.TripleStatus?) -> Unit) {
        val cookie = getUserCookie(uid)
        if (cookie == null) {
            console().sendMessage("用户 $uid 未设置Cookie")
            callback(null)
            return
        }

        scope.launch {
            try {
                incrementApiCall("getTripleStatus")
                val tripleStatus = api.getTripleStatus(bvid, cookie)
                withContext(Dispatchers.Main) {
                    callback(tripleStatus)
                }
            } catch (e: Exception) {
                console().sendMessage("获取三连状态失败: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }

    /**
     * 异步获取用户信息
     *
     * @param mid 用户ID
     * @param callback 回调函数
     */
    fun getUserInfoAsync(mid: Long, callback: (BilibiliApi.UserInfo?) -> Unit) {
        scope.launch {
            try {
                incrementApiCall("getUserInfo")
                val userInfo = api.getUserInfo(mid)
                withContext(Dispatchers.Main) {
                    callback(userInfo)
                }
            } catch (e: Exception) {
                console().sendMessage("获取用户信息失败: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }

    /**
     * 检查API连接状态
     *
     * @param callback 回调函数
     */
    fun checkApiStatusAsync(callback: (Boolean) -> Unit) {
        scope.launch {
            try {
                val isOnline = api.checkApiStatus()
                withContext(Dispatchers.Main) {
                    callback(isOnline)
                }
            } catch (e: Exception) {
                console().sendMessage("API状态检查失败: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback(false)
                }
            }
        }
    }

    /**
     * 批量检查用户三连状态
     *
     * @param users 用户UID列表
     * @param bvid 视频BV号
     * @param callback 回调函数，返回Map<UID, TripleStatus?>
     */
    fun batchCheckTripleStatusAsync(
        users: List<String>,
        bvid: String,
        callback: (Map<String, BilibiliApi.TripleStatus?>) -> Unit
    ) {
        scope.launch {
            val results = mutableMapOf<String, BilibiliApi.TripleStatus?>()

            // 并行检查所有用户的状态
            val jobs = users.map { uid ->
                async {
                    val cookie = getUserCookie(uid)
                    if (cookie != null) {
                        try {
                            uid to api.getTripleStatus(bvid, cookie)
                        } catch (e: Exception) {
                            console().sendMessage("检查用户 $uid 三连状态失败: ${e.message}")
                            uid to null
                        }
                    } else {
                        uid to null
                    }
                }
            }

            // 等待所有任务完成
            jobs.awaitAll().forEach { (uid, status) ->
                results[uid] = status
            }

            withContext(Dispatchers.Main) {
                callback(results)
            }
        }
    }

    /**
     * 增加API调用计数
     *
     * @param apiName API名称
     */
    private fun incrementApiCall(apiName: String) {
        apiStats[apiName] = (apiStats[apiName] ?: 0) + 1
    }

    /**
     * 获取API调用统计
     *
     * @return API调用统计信息
     */
    fun getApiStats(): Map<String, Long> = apiStats.toMap()

    /**
     * 获取API调用统计报告
     *
     * @return 格式化的统计报告
     */
    fun getApiStatsReport(): String {
        return buildString {
            appendLine("=== Bilibili API调用统计 ===")
            if (apiStats.isEmpty()) {
                appendLine("暂无API调用记录")
            } else {
                apiStats.forEach { (apiName, count) ->
                    appendLine("$apiName: ${count}次")
                }
                appendLine("总调用次数: ${apiStats.values.sum()}")
            }
            appendLine("当前管理的用户数: ${userCookies.size}")
            appendLine("连接池状态: ${apiClient.getConnectionPoolStats()}")
        }
    }

    /**
     * 清理资源
     */
    fun cleanup() {
        scope.cancel()
        apiClient.close()
        userCookies.clear()
        apiStats.clear()
        console().sendMessage("Bilibili API管理器已清理")
    }

    /**
     * 重置API统计
     */
    fun resetApiStats() {
        apiStats.clear()
        console().sendMessage("API调用统计已重置")
    }

    /**
     * 获取在线用户数量（有Cookie的用户）
     *
     * @return 在线用户数量
     */
    fun getOnlineUserCount(): Int = userCookies.size

    /**
     * 获取所有用户UID列表
     *
     * @return 用户UID列表
     */
    fun getAllUserIds(): Set<String> = userCookies.keys.toSet()
} 