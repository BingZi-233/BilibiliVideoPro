package online.bingzi.bilibili.video.pro.internal.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Bilibili Cookie管理器
 * 用于存储和管理登录状态相关的Cookie
 * 线程安全实现
 */
class BilibiliCookieJar : CookieJar {

    private val cookieStore = ConcurrentHashMap<String, Cookie>()
    private val lock = ReentrantReadWriteLock()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        lock.write {
            cookies.forEach { cookie ->
                // 只保存bilibili域名下的cookie
                if (cookie.domain.contains("bilibili.com")) {
                    cookieStore[cookie.name] = cookie
                }
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return lock.read {
            val validCookies = mutableListOf<Cookie>()
            val expiredKeys = mutableListOf<String>()

            // 收集有效和过期的cookie
            cookieStore.forEach { (key, cookie) ->
                if (cookie.expiresAt < System.currentTimeMillis()) {
                    expiredKeys.add(key)
                } else if (cookie.matches(url)) {
                    validCookies.add(cookie)
                }
            }

            // 如果有过期的cookie，使用写锁移除它们
            if (expiredKeys.isNotEmpty()) {
                lock.write {
                    expiredKeys.forEach { key ->
                        cookieStore.remove(key)
                    }
                }
            }

            validCookies
        }
    }

    /**
     * 手动设置Cookie
     */
    fun setCookies(cookies: Map<String, String>) {
        lock.write {
            cookies.forEach { (name, value) ->
                // 为每个重要的Bilibili域名设置Cookie
                val domains = listOf(
                    "api.bilibili.com",
                    "passport.bilibili.com", 
                    "www.bilibili.com",
                    ".bilibili.com"  // 通配符域名
                )
                
                domains.forEach { domain ->
                    val cookie = Cookie.Builder()
                        .name(name)
                        .value(value)
                        .domain(domain)
                        .path("/")
                        .build()
                    cookieStore["${name}_${domain}"] = cookie
                }
            }
        }
    }

    /**
     * 获取所有Cookie作为Map
     */
    fun getCookies(): Map<String, String> {
        return lock.read {
            val result = mutableMapOf<String, String>()
            cookieStore.values.forEach { cookie ->
                // 去重，优先保留最新的值
                result[cookie.name] = cookie.value
            }
            result
        }
    }

    /**
     * 获取特定Cookie
     */
    fun getCookie(name: String): String? {
        return lock.read {
            cookieStore.values.find { it.name == name && !isExpired(it) }?.value
        }
    }

    /**
     * 清除所有Cookie
     */
    fun clear() {
        lock.write {
            cookieStore.clear()
        }
    }

    /**
     * 移除特定Cookie
     */
    fun removeCookie(name: String) {
        lock.write {
            val keysToRemove = cookieStore.keys.filter { key ->
                cookieStore[key]?.name == name
            }
            keysToRemove.forEach { key ->
                cookieStore.remove(key)
            }
        }
    }

    /**
     * 检查是否包含特定Cookie
     */
    fun hasCookie(name: String): Boolean {
        return lock.read {
            cookieStore.values.any { it.name == name && !isExpired(it) }
        }
    }

    /**
     * 检查Cookie是否过期
     */
    private fun isExpired(cookie: Cookie): Boolean {
        return cookie.expiresAt < System.currentTimeMillis()
    }

    /**
     * 获取Cookie字符串形式（用于手动设置请求头）
     */
    fun getCookieString(): String {
        return lock.read {
            cookieStore.values
                .filter { !isExpired(it) }
                .joinToString("; ") { "${it.name}=${it.value}" }
        }
    }

    /**
     * 清理过期的Cookie
     */
    fun cleanupExpiredCookies() {
        lock.write {
            val expiredKeys = cookieStore.filterValues { isExpired(it) }.keys
            expiredKeys.forEach { key ->
                cookieStore.remove(key)
            }
        }
    }

    /**
     * 获取Cookie统计信息
     */
    fun getCookieStats(): Map<String, Any> {
        return lock.read {
            val totalCookies = cookieStore.size
            val expiredCookies = cookieStore.values.count { isExpired(it) }
            val validCookies = totalCookies - expiredCookies

            mapOf(
                "total" to totalCookies,
                "valid" to validCookies,
                "expired" to expiredCookies
            )
        }
    }
} 