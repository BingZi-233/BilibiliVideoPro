package online.bingzi.bilibili.video.pro.internal.network

import com.google.gson.Gson
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import online.bingzi.bilibili.video.pro.internal.entity.netwrk.ApiResponse
import java.util.concurrent.TimeUnit

/**
 * Bilibili API 客户端
 * 提供基础的HTTP请求功能和Cookie管理
 */
class BilibiliApiClient {
    
    companion object {
        // API基础URL
        const val API_BASE_URL = "https://api.bilibili.com"
        const val PASSPORT_BASE_URL = "https://passport.bilibili.com"
        const val WEB_BASE_URL = "https://www.bilibili.com"
        
        // 请求头
        const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        const val REFERER = "https://www.bilibili.com/"
    }
    
    private val gson = Gson()
    private val cookieJar = BilibiliCookieJar()
    
    // HTTP客户端配置
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .cookieJar(cookieJar)
        .addInterceptor(createLoggingInterceptor())
        .addInterceptor(createHeaderInterceptor())
        .build()
    
    /**
     * 创建日志拦截器
     */
    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    /**
     * 创建请求头拦截器
     */
    private fun createHeaderInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .header("User-Agent", USER_AGENT)
                .header("Referer", REFERER)
                .header("Origin", "https://www.bilibili.com")
            
            chain.proceed(requestBuilder.build())
        }
    }
    
    /**
     * 执行GET请求
     */
    fun get(url: String, params: Map<String, String> = emptyMap()): ApiResponse {
        val urlBuilder = url.toHttpUrlOrNull()?.newBuilder() ?: return ApiResponse.error("Invalid URL")
        
        params.forEach { (key, value) ->
            urlBuilder.addQueryParameter(key, value)
        }
        
        val request = Request.Builder()
            .url(urlBuilder.build())
            .get()
            .build()
        
        return executeRequest(request)
    }
    
    /**
     * 执行POST请求
     */
    fun post(url: String, data: Map<String, String> = emptyMap()): ApiResponse {
        val formBody = FormBody.Builder()
        data.forEach { (key, value) ->
            formBody.add(key, value)
        }
        
        val request = Request.Builder()
            .url(url)
            .post(formBody.build())
            .build()
        
        return executeRequest(request)
    }
    
    /**
     * 执行POST JSON请求
     */
    fun postJson(url: String, json: String): ApiResponse {
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = json.toRequestBody(mediaType)
        
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        
        return executeRequest(request)
    }
    
    /**
     * 执行请求
     */
    private fun executeRequest(request: Request): ApiResponse {
        return try {
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            
            if (response.isSuccessful) {
                ApiResponse.success(responseBody)
            } else {
                ApiResponse.error("HTTP ${response.code}: ${response.message}")
            }
        } catch (e: Exception) {
            ApiResponse.error("Request failed: ${e.message}")
        }
    }
    
    /**
     * 设置Cookie
     */
    fun setCookies(cookies: Map<String, String>) {
        cookieJar.setCookies(cookies)
    }
    
    /**
     * 获取所有Cookie
     */
    fun getCookies(): Map<String, String> {
        return cookieJar.getCookies()
    }
    
    /**
     * 清除所有Cookie
     */
    fun clearCookies() {
        cookieJar.clear()
    }
    
    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean {
        return cookieJar.getCookies().containsKey("SESSDATA")
    }
}

