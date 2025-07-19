package online.bingzi.bilibili.video.pro.internal.network.video

import online.bingzi.bilibili.video.pro.internal.entity.netwrk.video.*
import online.bingzi.bilibili.video.pro.internal.network.BilibiliApiClient

/**
 * 视频互动服务
 * 提供点赞、投币、收藏、关注、评论等互动功能
 */
class VideoInteractionService(private val apiClient: BilibiliApiClient) {

    companion object {
        // 视频互动相关API
        private const val VIDEO_DETAIL_URL = "${BilibiliApiClient.API_BASE_URL}/x/web-interface/view"
        private const val VIDEO_RELATION_URL = "${BilibiliApiClient.API_BASE_URL}/x/web-interface/archive/relation"
        private const val USER_RELATION_URL = "${BilibiliApiClient.API_BASE_URL}/x/relation"
        private const val COMMENT_LIST_URL = "${BilibiliApiClient.API_BASE_URL}/x/v2/reply"
        private const val USER_CARD_URL = "${BilibiliApiClient.API_BASE_URL}/x/web-interface/card"

        // 操作类型
        const val TYPE_VIDEO = 1  // 视频
        const val TYPE_ARTICLE = 12  // 专栏
    }

    /**
     * 获取视频的三连状态（点赞、投币、收藏）
     * @param bvid 视频的bvid
     * @return TripleActionStatus 三连状态
     */
    fun getTripleActionStatus(bvid: String): TripleActionResult {
        try {
            val params = mapOf("bvid" to bvid)
            val response = apiClient.get(VIDEO_RELATION_URL, params)

            if (!response.isSuccess) {
                return TripleActionResult.Error("获取视频状态失败: ${response.error}")
            }

            val jsonResponse = response.asJsonObject()
            if (jsonResponse == null) {
                return TripleActionResult.Error("响应数据解析失败")
            }

            val code = jsonResponse.get("code")?.asInt ?: -1
            if (code != 0) {
                val message = jsonResponse.get("message")?.asString ?: "未知错误"
                return TripleActionResult.Error("API错误: $message")
            }

            val data = jsonResponse.getAsJsonObject("data")
            if (data == null) {
                return TripleActionResult.Error("响应数据格式错误")
            }

            val like = data.get("like")?.let { 
                if (it.isJsonPrimitive && it.asJsonPrimitive.isBoolean) it.asBoolean else it.asInt == 1
            } ?: false
            val coin = data.get("coin")?.asInt ?: 0
            val favorite = data.get("favorite")?.let {
                if (it.isJsonPrimitive && it.asJsonPrimitive.isBoolean) it.asBoolean else it.asInt == 1
            } ?: false

            val status = TripleActionStatus(
                isLiked = like,
                isCoined = coin > 0,
                isFavorited = favorite,
                coinCount = coin
            )

            return TripleActionResult.Success(status)

        } catch (e: Exception) {
            return TripleActionResult.Error("获取视频状态异常: ${e.message}")
        }
    }

    /**
     * 检查是否关注UP主
     * @param mid UP主的用户ID
     * @return FollowResult 关注状态结果
     */
    fun checkFollowStatus(mid: Long): FollowResult {
        try {
            val params = mapOf("fid" to mid.toString())
            val response = apiClient.get(USER_RELATION_URL, params)

            if (!response.isSuccess) {
                return FollowResult.Error("获取关注状态失败: ${response.error}")
            }

            val jsonResponse = response.asJsonObject()
            if (jsonResponse == null) {
                return FollowResult.Error("响应数据解析失败")
            }

            val code = jsonResponse.get("code")?.asInt ?: -1
            if (code != 0) {
                val message = jsonResponse.get("message")?.asString ?: "未知错误"
                return FollowResult.Error("API错误: $message")
            }

            val data = jsonResponse.getAsJsonObject("data")
            if (data == null) {
                return FollowResult.Error("响应数据格式错误")
            }

            val attribute = data.get("attribute")?.asInt ?: 0
            val isFollowing = attribute != 0

            return FollowResult.Success(isFollowing)

        } catch (e: Exception) {
            return FollowResult.Error("检查关注状态异常: ${e.message}")
        }
    }

    /**
     * 检查用户是否在指定视频评论区留言
     * @param bvid 视频的bvid
     * @param mid 用户ID，如果为null则使用当前登录用户
     * @return CommentCheckResult 评论检查结果
     */
    fun checkUserCommented(bvid: String, mid: Long? = null): CommentCheckResult {
        try {
            // 首先获取视频详情以获取aid
            val videoDetail = getVideoDetail(bvid)
            if (videoDetail !is VideoDetailResult.Success) {
                return CommentCheckResult.Error("获取视频详情失败")
            }

            val aid = videoDetail.data.aid
            val targetMid = mid ?: getCurrentUserMid()

            if (targetMid == null) {
                return CommentCheckResult.Error("无法获取用户ID，请确保已登录")
            }

            // 获取评论列表（分页获取，检查前几页）
            var hasCommented = false
            var pageNum = 1
            val maxPages = 5 // 最多检查前5页评论

            while (pageNum <= maxPages && !hasCommented) {
                val params = mapOf(
                    "type" to TYPE_VIDEO.toString(),
                    "oid" to aid.toString(),
                    "pn" to pageNum.toString(),
                    "ps" to "20",  // 每页20条评论
                    "sort" to "2"  // 按时间排序
                )

                val response = apiClient.get(COMMENT_LIST_URL, params)
                if (!response.isSuccess) {
                    break
                }

                val jsonResponse = response.asJsonObject()
                val code = jsonResponse?.get("code")?.asInt ?: -1
                if (code != 0) {
                    break
                }

                val data = jsonResponse?.getAsJsonObject("data")
                val replies = data?.getAsJsonArray("replies")

                if (replies == null || replies.size() == 0) {
                    break // 没有更多评论了
                }

                // 检查当前页是否有目标用户的评论
                for (i in 0 until replies.size()) {
                    val reply = replies[i].asJsonObject
                    val member = reply?.getAsJsonObject("member")
                    val commentMid = member?.get("mid")?.asLong

                    if (commentMid == targetMid) {
                        hasCommented = true
                        break
                    }
                }

                pageNum++
            }

            return CommentCheckResult.Success(hasCommented)

        } catch (e: Exception) {
            return CommentCheckResult.Error("检查评论状态异常: ${e.message}")
        }
    }

    /**
     * 获取视频详情
     */
    private fun getVideoDetail(bvid: String): VideoDetailResult {
        try {
            val params = mapOf("bvid" to bvid)
            val response = apiClient.get(VIDEO_DETAIL_URL, params)

            if (!response.isSuccess) {
                return VideoDetailResult.Error("获取视频详情失败: ${response.error}")
            }

            val jsonResponse = response.asJsonObject()
            if (jsonResponse == null) {
                return VideoDetailResult.Error("响应数据解析失败")
            }

            val code = jsonResponse.get("code")?.asInt ?: -1
            if (code != 0) {
                val message = jsonResponse.get("message")?.asString ?: "未知错误"
                return VideoDetailResult.Error("API错误: $message")
            }

            val data = jsonResponse.getAsJsonObject("data")
            if (data == null) {
                return VideoDetailResult.Error("响应数据格式错误")
            }

            val aid = data.get("aid")?.asLong ?: 0L
            val ownerMid = data.getAsJsonObject("owner")?.get("mid")?.asLong ?: 0L
            val title = data.get("title")?.asString ?: ""

            val videoData = VideoData(aid, bvid, title, ownerMid)
            return VideoDetailResult.Success(videoData)

        } catch (e: Exception) {
            return VideoDetailResult.Error("获取视频详情异常: ${e.message}")
        }
    }

    /**
     * 获取当前登录用户的mid
     */
    private fun getCurrentUserMid(): Long? {
        try {
            val response = apiClient.get(USER_CARD_URL)

            if (!response.isSuccess) {
                return null
            }

            val jsonResponse = response.asJsonObject()
            val code = jsonResponse?.get("code")?.asInt ?: -1
            if (code != 0) {
                return null
            }

            val data = jsonResponse?.getAsJsonObject("data")
            return data?.get("mid")?.asLong

        } catch (e: Exception) {
            return null
        }
    }

    /**
     * 获取视频完整互动状态（包含三连状态和UP主关注状态）
     * @param bvid 视频的bvid
     * @return VideoInteractionResult 完整互动状态结果
     */
    fun getVideoInteractionStatus(bvid: String): VideoInteractionResult {
        try {
            // 获取视频详情
            val videoDetailResult = getVideoDetail(bvid)
            if (videoDetailResult !is VideoDetailResult.Success) {
                return VideoInteractionResult.Error("获取视频详情失败")
            }

            val videoData = videoDetailResult.data

            // 获取三连状态
            val tripleResult = getTripleActionStatus(bvid)
            if (tripleResult !is TripleActionResult.Success) {
                return VideoInteractionResult.Error("获取三连状态失败: ${(tripleResult as TripleActionResult.Error).message}")
            }

            // 获取关注状态
            val followResult = checkFollowStatus(videoData.ownerMid)
            if (followResult !is FollowResult.Success) {
                return VideoInteractionResult.Error("获取关注状态失败: ${(followResult as FollowResult.Error).message}")
            }

            // 检查评论状态
            val commentResult = checkUserCommented(bvid)
            val hasCommented = when (commentResult) {
                is CommentCheckResult.Success -> commentResult.hasCommented
                is CommentCheckResult.Error -> false // 评论检查失败时默认为false
            }

            val interactionStatus = VideoInteractionStatus(
                videoData = videoData,
                tripleAction = tripleResult.status,
                isFollowingUp = followResult.isFollowing,
                hasCommented = hasCommented
            )

            return VideoInteractionResult.Success(interactionStatus)

        } catch (e: Exception) {
            return VideoInteractionResult.Error("获取视频互动状态异常: ${e.message}")
        }
    }
}

