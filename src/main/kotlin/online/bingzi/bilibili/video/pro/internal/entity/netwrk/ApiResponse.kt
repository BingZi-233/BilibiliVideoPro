package online.bingzi.bilibili.video.pro.internal.entity.netwrk

import com.google.gson.Gson
import com.google.gson.JsonObject

/**
 * API响应包装类
 */
data class ApiResponse(
    val isSuccess: Boolean,
    val data: String,
    val error: String?
) {
    companion object {
        fun success(data: String) = ApiResponse(true, data, null)
        fun error(error: String) = ApiResponse(false, "", error)
    }

    /**
     * 解析为JSON对象
     */
    fun asJsonObject(): JsonObject? {
        return try {
            Gson().fromJson(data, JsonObject::class.java)
        } catch (e: Exception) {
            null
        }
    }
}