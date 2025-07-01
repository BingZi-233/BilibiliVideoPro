package online.bingzi.bilibili.video.pro.internal.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

/**
 * Bilibili Cookie管理器
 * 用于存储和管理登录状态相关的Cookie
 */
class BilibiliCookieJar : CookieJar {
    
    private val cookieStore = ConcurrentHashMap<String, Cookie>()
    
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookies.forEach { cookie ->
            // 只保存bilibili域名下的cookie
            if (cookie.domain.contains("bilibili.com")) {
                cookieStore[cookie.name] = cookie
            }
        }
    }
    
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val validCookies = mutableListOf<Cookie>()
        val iterator = cookieStore.values.iterator()
        
        while (iterator.hasNext()) {
            val cookie = iterator.next()
            // 检查cookie是否过期
            if (cookie.expiresAt < System.currentTimeMillis()) {
                iterator.remove()
                continue
            }
            
            // 检查域名匹配
            if (cookie.matches(url)) {
                validCookies.add(cookie)
            }
        }
        
        return validCookies
    }
    
    /**
     * 手动设置Cookie
     */
    fun setCookies(cookies: Map<String, String>) {
        cookies.forEach { (name, value) ->
            val cookie = Cookie.Builder()
                .name(name)
                .value(value)
                .domain(".bilibili.com")
                .path("/")
                .httpOnly()
                .secure()
                .build()
            cookieStore[name] = cookie
        }
    }
    
    /**
     * 获取所有Cookie作为Map
     */
    fun getCookies(): Map<String, String> {
        return cookieStore.mapValues { it.value.value }
    }
    
    /**
     * 获取特定Cookie
     */
    fun getCookie(name: String): String? {
        return cookieStore[name]?.value
    }
    
    /**
     * 清除所有Cookie
     */
    fun clear() {
        cookieStore.clear()
    }
    
    /**
     * 移除特定Cookie
     */
    fun removeCookie(name: String) {
        cookieStore.remove(name)
    }
    
    /**
     * 检查是否包含特定Cookie
     */
    fun hasCookie(name: String): Boolean {
        return cookieStore.containsKey(name) && !isExpired(cookieStore[name]!!)
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
        return cookieStore.values
            .filter { !isExpired(it) }
            .joinToString("; ") { "${it.name}=${it.value}" }
    }
} 