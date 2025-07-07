package online.bingzi.bilibili.video.pro.internal.network

import com.google.gson.Gson
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import online.bingzi.bilibili.video.pro.internal.entity.netwrk.ApiResponse
import online.bingzi.bilibili.video.pro.internal.error.ErrorHandler
import taboolib.common.platform.function.console
import taboolib.module.lang.sendInfo
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
     * 执行请求（带重试机制）
     */
    private fun executeRequest(request: Request): ApiResponse {
        return executeRequestWithRetry(request, maxRetries = 3)
    }

    /**
     * 执行请求（带重试机制）
     */
    private fun executeRequestWithRetry(request: Request, maxRetries: Int): ApiResponse {
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                console().sendInfo("networkRequestExecuting", (attempt + 1).toString(), maxRetries.toString(), request.url.toString())

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    console().sendInfo("networkRequestSuccess", request.url.toString())
                    return ApiResponse.success(responseBody)
                } else {
                    val errorMsg = "HTTP ${response.code}: ${response.message}"
                    console().sendInfo("networkRequestFailed", errorMsg)

                    // 如果是客户端错误（4xx），不重试
                    if (response.code in 400..499) {
                        ErrorHandler.handleError(
                            type = ErrorHandler.ErrorType.NETWORK,
                            component = "BilibiliApiClient",
                            operation = "executeRequest",
                            exception = Exception(errorMsg),
                            metadata = mapOf(
                                "url" to request.url.toString(),
                                "method" to request.method,
                                "status_code" to response.code,
                                "attempt" to attempt + 1
                            )
                        )
                        return ApiResponse.error(errorMsg)
                    }

                    lastException = Exception(errorMsg)
                }

            } catch (e: Exception) {
                lastException = e
                console().sendInfo("networkRequestException", (attempt + 1).toString(), maxRetries.toString(), e.message ?: "unknown")

                // 记录错误但继续重试
                ErrorHandler.handleError(
                    type = ErrorHandler.ErrorType.NETWORK,
                    component = "BilibiliApiClient",
                    operation = "executeRequest",
                    exception = e,
                    metadata = mapOf(
                        "url" to request.url.toString(),
                        "method" to request.method,
                        "attempt" to attempt + 1,
                        "max_retries" to maxRetries
                    ),
                    shouldRetry = attempt < maxRetries - 1
                )

                // 如果不是最后一次尝试，等待后重试
                if (attempt < maxRetries - 1) {
                    try {
                        Thread.sleep((1000 * (attempt + 1)).toLong()) // 递增延迟
                    } catch (ie: InterruptedException) {
                        Thread.currentThread().interrupt()
                        return@repeat
                    }
                }
            }
        }

        // 所有重试都失败了
        val finalError = lastException ?: Exception("Unknown network error")
        console().sendInfo("networkRequestFinalFailure", finalError.message ?: "unknown")

        ErrorHandler.handleError(
            type = ErrorHandler.ErrorType.NETWORK,
            component = "BilibiliApiClient",
            operation = "executeRequest",
            exception = finalError,
            metadata = mapOf(
                "url" to request.url.toString(),
                "method" to request.method,
                "final_failure" to true
            )
        )

        return ApiResponse.error("Request failed after $maxRetries attempts: ${finalError.message}")
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

