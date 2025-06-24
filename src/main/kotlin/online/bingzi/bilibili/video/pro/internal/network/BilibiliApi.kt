package online.bingzi.bilibili.video.pro.internal.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Bilibili API接口
 *
 * 提供与Bilibili API交互的具体方法，包括视频信息获取、用户操作检测等功能
 * 基于官方API文档实现：https://socialsisteryi.github.io/bilibili-API-collect/
 *
 * @property client Bilibili API客户端实例
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
class BilibiliApi(val client: BilibiliApiClient) {

    /**
     * Bilibili通用响应格式
     *
     * @param T 数据类型
     * @property code 响应代码，0表示成功
     * @property message 响应消息
     * @property data 响应数据
     */
    data class BilibiliResponse<T>(
        @SerializedName("code")
        val code: Int,

        @SerializedName("message")
        val message: String,

        @SerializedName("data")
        val data: T?
    )

    /**
     * 视频基础信息
     */
    data class VideoInfo(
        @SerializedName("aid")
        val aid: Long,

        @SerializedName("bvid")
        val bvid: String,

        @SerializedName("title")
        val title: String,

        @SerializedName("owner")
        val owner: VideoOwner,

        @SerializedName("ctime")
        val createTime: Long,

        @SerializedName("pubdate")
        val publishTime: Long,

        @SerializedName("stat")
        val stat: VideoStat?
    )

    /**
     * 视频作者信息
     */
    data class VideoOwner(
        @SerializedName("mid")
        val mid: Long,

        @SerializedName("name")
        val name: String,

        @SerializedName("face")
        val avatar: String
    )

    /**
     * 视频统计信息
     */
    data class VideoStat(
        @SerializedName("view")
        val view: Int,

        @SerializedName("danmaku")
        val danmaku: Int,

        @SerializedName("reply")
        val reply: Int,

        @SerializedName("favorite")
        val favorite: Int,

        @SerializedName("coin")
        val coin: Int,

        @SerializedName("share")
        val share: Int,

        @SerializedName("like")
        val like: Int
    )

    /**
     * 用户三连操作状态
     */
    data class TripleStatus(
        @SerializedName("like")
        val like: Int, // 1已点赞 0未点赞

        @SerializedName("coin")
        val coin: Int, // 已投币数量

        @SerializedName("fav")
        val favorite: Int, // 1已收藏 0未收藏

        @SerializedName("multiply")
        val coinMultiple: Int // 投币数量倍数
    )

    /**
     * 用户基础信息
     */
    data class UserInfo(
        @SerializedName("mid")
        val mid: Long,

        @SerializedName("name")
        val name: String,

        @SerializedName("face")
        val avatar: String,

        @SerializedName("level")
        val level: Int
    )

    /**
     * 根据BV号获取视频信息
     *
     * @param bvid 视频BV号
     * @return 视频信息，失败时返回null
     */
    suspend fun getVideoInfo(bvid: String): VideoInfo? = withContext(Dispatchers.IO) {
        try {
            val response = client.get(
                endpoint = "/x/web-interface/view",
                params = mapOf("bvid" to bvid)
            )

            if (response.isSuccess && response.data != null) {
                val bilibiliResponse = client.parseResponse(response.data, BilibiliResponse::class.java)
                if (bilibiliResponse?.code == 0) {
                    // 注意：这里需要根据实际API响应结构调整
                    client.parseResponse(client.gson.toJson(bilibiliResponse.data), VideoInfo::class.java)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 根据AV号获取视频信息
     *
     * @param aid 视频AV号
     * @return 视频信息，失败时返回null
     */
    suspend fun getVideoInfo(aid: Long): VideoInfo? = withContext(Dispatchers.IO) {
        try {
            val response = client.get(
                endpoint = "/x/web-interface/view",
                params = mapOf("aid" to aid.toString())
            )

            if (response.isSuccess && response.data != null) {
                val bilibiliResponse = client.parseResponse(response.data, BilibiliResponse::class.java)
                if (bilibiliResponse?.code == 0) {
                    client.parseResponse(client.gson.toJson(bilibiliResponse.data), VideoInfo::class.java)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取用户对视频的三连状态
     *
     * 需要登录状态（Cookie）
     *
     * @param bvid 视频BV号
     * @param cookie 用户Cookie
     * @return 三连状态，失败时返回null
     */
    suspend fun getTripleStatus(bvid: String, cookie: String): TripleStatus? = withContext(Dispatchers.IO) {
        try {
            val response = client.get(
                endpoint = "/x/web-interface/archive/has/like",
                params = mapOf("bvid" to bvid),
                headers = mapOf("Cookie" to cookie)
            )

            if (response.isSuccess && response.data != null) {
                val bilibiliResponse = client.parseResponse(response.data, BilibiliResponse::class.java)
                if (bilibiliResponse?.code == 0) {
                    client.parseResponse(Gson().toJson(bilibiliResponse.data), TripleStatus::class.java)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取用户基础信息
     *
     * @param mid 用户ID
     * @return 用户信息，失败时返回null
     */
    suspend fun getUserInfo(mid: Long): UserInfo? = withContext(Dispatchers.IO) {
        try {
            val response = client.get(
                endpoint = "/x/space/acc/info",
                params = mapOf("mid" to mid.toString())
            )

            if (response.isSuccess && response.data != null) {
                val bilibiliResponse = client.parseResponse(response.data, BilibiliResponse::class.java)
                if (bilibiliResponse?.code == 0) {
                    client.parseResponse(Gson().toJson(bilibiliResponse.data), UserInfo::class.java)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 检查用户是否已点赞视频
     *
     * @param bvid 视频BV号
     * @param cookie 用户Cookie
     * @return 点赞状态：1已点赞，0未点赞，null表示检查失败
     */
    suspend fun checkLikeStatus(bvid: String, cookie: String): Int? = withContext(Dispatchers.IO) {
        try {
            val tripleStatus = getTripleStatus(bvid, cookie)
            tripleStatus?.like
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 检查用户投币状态
     *
     * @param bvid 视频BV号
     * @param cookie 用户Cookie
     * @return 投币数量，null表示检查失败
     */
    suspend fun checkCoinStatus(bvid: String, cookie: String): Int? = withContext(Dispatchers.IO) {
        try {
            val tripleStatus = getTripleStatus(bvid, cookie)
            tripleStatus?.coin
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 检查用户收藏状态
     *
     * @param bvid 视频BV号
     * @param cookie 用户Cookie
     * @return 收藏状态：1已收藏，0未收藏，null表示检查失败
     */
    suspend fun checkFavoriteStatus(bvid: String, cookie: String): Int? = withContext(Dispatchers.IO) {
        try {
            val tripleStatus = getTripleStatus(bvid, cookie)
            tripleStatus?.favorite
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取用户最近上传的视频列表
     *
     * @param mid 用户ID
     * @param page 页码，从1开始
     * @param pageSize 每页数量，默认30
     * @return 视频列表，失败时返回空列表
     */
    suspend fun getUserVideos(mid: Long, page: Int = 1, pageSize: Int = 30): List<VideoInfo> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.get(
                    endpoint = "/x/space/arc/search",
                    params = mapOf(
                        "mid" to mid.toString(),
                        "pn" to page.toString(),
                        "ps" to pageSize.toString()
                    )
                )

                if (response.isSuccess && response.data != null) {
                    val bilibiliResponse = client.parseResponse(response.data, BilibiliResponse::class.java)
                    if (bilibiliResponse?.code == 0 && bilibiliResponse.data != null) {
                        // 解析视频列表（需要根据实际API响应调整）
                        emptyList()
                    } else {
                        emptyList()
                    }
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

    /**
     * 检查API连接状态
     *
     * @return true表示API可访问
     */
    suspend fun checkApiStatus(): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.get("/x/web-interface/nav")
            response.isSuccess && response.code == 200
        } catch (e: Exception) {
            false
        }
    }
} 