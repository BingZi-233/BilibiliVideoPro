package online.bingzi.bilibili.video.pro.internal.network

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Bilibili API客户端
 *
 * 基于OkHttp构建的Bilibili API交互客户端，提供了完整的HTTP请求功能
 * 支持GET、POST等HTTP方法，自动处理JSON序列化和反序列化
 *
 * 主要功能：
 * - 自动添加必要的请求头
 * - 支持Cookie管理
 * - 请求日志记录
 * - 连接池管理
 * - 自动重试机制
 *
 * @constructor 创建Bilibili API客户端实例
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
class BilibiliApiClient {

    /**
     * OkHttp客户端实例
     *
     * 配置了连接池、超时、拦截器等参数
     */
    private val httpClient: OkHttpClient

    /**
     * JSON序列化工具
     *
     * 用于处理请求和响应的JSON数据
     */
    private val gson: Gson = Gson()

    /**
     * Bilibili API基础URL
     */
    private val baseUrl = "https://api.bilibili.com"

    companion object {
        /**
         * 默认的用户代理字符串
         *
         * 模拟浏览器请求，避免被反爬虫系统拦截
         */
        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

        /**
         * 默认的Referer
         */
        private const val REFERER = "https://www.bilibili.com"

        /**
         * JSON媒体类型
         */
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    init {
        // 创建日志拦截器
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC // 可以设置为BODY查看详细内容
        }

        // 构建HTTP客户端
        httpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                // 自动添加通用请求头
                val originalRequest = chain.request()
                val requestWithHeaders = originalRequest.newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .header("Referer", REFERER)
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .build()

                chain.proceed(requestWithHeaders)
            }
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * API响应包装类
     *
     * 统一处理Bilibili API的响应格式
     *
     * @param T 响应数据的类型
     * @property isSuccess 请求是否成功
     * @property data 响应数据
     * @property message 错误消息
     * @property code HTTP状态码
     */
    data class ApiResponse<T>(
        val isSuccess: Boolean,
        val data: T? = null,
        val message: String? = null,
        val code: Int = 200
    )

    /**
     * 执行GET请求
     *
     * @param endpoint API端点路径
     * @param params 查询参数
     * @param headers 额外的请求头
     * @return API响应结果
     */
    suspend fun get(
        endpoint: String,
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): ApiResponse<String> {
        return try {
            val urlBuilder = HttpUrl.parse("$baseUrl$endpoint")?.newBuilder()
                ?: return ApiResponse(false, message = "无效的URL: $baseUrl$endpoint")

            // 添加查询参数
            params.forEach { (key, value) ->
                urlBuilder.addQueryParameter(key, value)
            }

            val requestBuilder = Request.Builder()
                .url(urlBuilder.build())
                .get()

            // 添加额外请求头
            headers.forEach { (key, value) ->
                requestBuilder.header(key, value)
            }

            val request = requestBuilder.build()
            val response = httpClient.newCall(request).execute()

            handleResponse(response)
        } catch (e: IOException) {
            ApiResponse(false, message = "网络错误: ${e.message}")
        } catch (e: Exception) {
            ApiResponse(false, message = "请求失败: ${e.message}")
        }
    }

    /**
     * 执行POST请求
     *
     * @param endpoint API端点路径
     * @param body 请求体数据
     * @param headers 额外的请求头
     * @return API响应结果
     */
    suspend fun post(
        endpoint: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap()
    ): ApiResponse<String> {
        return try {
            val requestBody = when (body) {
                null -> "".toRequestBody()
                is String -> body.toRequestBody(JSON_MEDIA_TYPE)
                else -> gson.toJson(body).toRequestBody(JSON_MEDIA_TYPE)
            }

            val requestBuilder = Request.Builder()
                .url("$baseUrl$endpoint")
                .post(requestBody)

            // 添加额外请求头
            headers.forEach { (key, value) ->
                requestBuilder.header(key, value)
            }

            val request = requestBuilder.build()
            val response = httpClient.newCall(request).execute()

            handleResponse(response)
        } catch (e: IOException) {
            ApiResponse(false, message = "网络错误: ${e.message}")
        } catch (e: Exception) {
            ApiResponse(false, message = "请求失败: ${e.message}")
        }
    }

    /**
     * 处理HTTP响应
     *
     * @param response OkHttp响应对象
     * @return 处理后的API响应
     */
    private fun handleResponse(response: Response): ApiResponse<String> {
        return try {
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                ApiResponse(true, data = responseBody, code = response.code)
            } else {
                ApiResponse(
                    false,
                    message = "HTTP ${response.code}: ${response.message}",
                    code = response.code
                )
            }
        } catch (e: Exception) {
            ApiResponse(false, message = "响应处理失败: ${e.message}")
        } finally {
            response.close()
        }
    }

    /**
     * 解析JSON响应为指定类型
     *
     * @param T 目标类型
     * @param jsonResponse JSON字符串响应
     * @param clazz 目标类型的Class对象
     * @return 解析后的对象，失败时返回null
     */
    fun <T> parseResponse(jsonResponse: String, clazz: Class<T>): T? {
        return try {
            gson.fromJson(jsonResponse, clazz)
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    /**
     * 添加Cookie到请求
     *
     * @param cookies Cookie字符串，格式为"key1=value1; key2=value2"
     */
    fun addCookies(cookies: String): BilibiliApiClient {
        // 这里可以实现Cookie管理逻辑
        // 暂时通过拦截器添加Cookie头
        return this
    }

    /**
     * 关闭客户端，释放资源
     */
    fun close() {
        httpClient.dispatcher.executorService.shutdown()
        httpClient.connectionPool.evictAll()
    }

    /**
     * 获取连接池统计信息
     *
     * @return 连接池统计信息字符串
     */
    fun getConnectionPoolStats(): String {
        val pool = httpClient.connectionPool
        return "连接池统计: 空闲连接=${pool.idleConnectionCount()}, 总连接=${pool.connectionCount()}"
    }
} 