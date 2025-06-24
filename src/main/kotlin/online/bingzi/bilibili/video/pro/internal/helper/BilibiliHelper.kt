package online.bingzi.bilibili.video.pro.internal.helper

import online.bingzi.bilibili.video.pro.api.entity.BilibiliUser
import online.bingzi.bilibili.video.pro.api.entity.BilibiliVideo
import online.bingzi.bilibili.video.pro.api.entity.TripleActionData
import online.bingzi.bilibili.video.pro.internal.network.BilibiliApi
import java.util.*
import java.util.regex.Pattern

/**
 * Bilibili辅助工具类
 *
 * 提供Bilibili相关的工具方法，包括：
 * - BV号和AV号转换
 * - 数据类型转换
 * - 验证和格式化方法
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
object BilibiliHelper {

    /**
     * BV号正则表达式
     */
    private val BV_PATTERN = Pattern.compile("^BV[1-9a-km-zA-HJ-NP-Z]{10}$")

    /**
     * AV号正则表达式
     */
    private val AV_PATTERN = Pattern.compile("^av(\\d+)$", Pattern.CASE_INSENSITIVE)

    /**
     * 用户UID正则表达式
     */
    private val UID_PATTERN = Pattern.compile("^\\d{1,12}$")

    /**
     * BV号转AV号的字符表
     */
    private const val TABLE = "fZodR9XQDSUm21yCkr6zBqiveYah8bt4xsWpHnJE7jL5VG3guMTKNPAwcF"
    private val TR = IntArray(128)
    private val S = intArrayOf(11, 10, 3, 8, 4, 6)
    private const val XOR = 177451812L
    private const val ADD = 8728348608L

    init {
        for (i in TABLE.indices) {
            TR[TABLE[i].code] = i
        }
    }

    /**
     * 验证BV号格式是否正确
     *
     * @param bvid BV号
     * @return true表示格式正确
     */
    fun isValidBvid(bvid: String?): Boolean {
        return bvid != null && BV_PATTERN.matcher(bvid).matches()
    }

    /**
     * 验证AV号格式是否正确
     *
     * @param aid AV号字符串或数字
     * @return true表示格式正确
     */
    fun isValidAid(aid: String?): Boolean {
        return when {
            aid == null -> false
            aid.matches(Regex("^\\d{1,12}$")) -> true
            AV_PATTERN.matcher(aid).matches() -> true
            else -> false
        }
    }

    /**
     * 验证用户UID格式是否正确
     *
     * @param uid 用户UID
     * @return true表示格式正确
     */
    fun isValidUid(uid: String?): Boolean {
        return uid != null && UID_PATTERN.matcher(uid).matches()
    }

    /**
     * BV号转AV号
     *
     * @param bvid BV号
     * @return AV号，转换失败返回null
     */
    fun bv2av(bvid: String): Long? {
        if (!isValidBvid(bvid)) return null

        return try {
            var r = 0L
            for (i in 0..5) {
                r += TR[bvid[S[i]].code] * Math.pow(58.0, i.toDouble()).toLong()
            }
            (r - ADD) xor XOR
        } catch (e: Exception) {
            null
        }
    }

    /**
     * AV号转BV号
     *
     * @param aid AV号
     * @return BV号，转换失败返回null
     */
    fun av2bv(aid: Long): String? {
        return try {
            val x = (aid xor XOR) + ADD
            val r = CharArray(12)
            r[0] = 'B'
            r[1] = 'V'
            for (i in 0..5) {
                r[S[i]] = TABLE[(x / Math.pow(58.0, i.toDouble()).toLong() % 58).toInt()]
            }
            String(r)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 从字符串中提取BV号
     *
     * @param text 包含BV号的文本
     * @return BV号列表
     */
    fun extractBvids(text: String): List<String> {
        val bvids = mutableListOf<String>()
        val pattern = Pattern.compile("BV[1-9a-km-zA-HJ-NP-Z]{10}")
        val matcher = pattern.matcher(text)

        while (matcher.find()) {
            bvids.add(matcher.group())
        }

        return bvids
    }

    /**
     * 从字符串中提取AV号
     *
     * @param text 包含AV号的文本
     * @return AV号列表
     */
    fun extractAids(text: String): List<Long> {
        val aids = mutableListOf<Long>()
        val pattern = Pattern.compile("av(\\d+)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)

        while (matcher.find()) {
            try {
                aids.add(matcher.group(1).toLong())
            } catch (e: NumberFormatException) {
                // 忽略无效的数字
            }
        }

        return aids
    }

    /**
     * 将API响应的VideoInfo转换为实体类BilibiliVideo
     *
     * @param apiVideoInfo API响应的视频信息
     * @return BilibiliVideo实体
     */
    fun convertToEntity(apiVideoInfo: BilibiliApi.VideoInfo): BilibiliVideo {
        return BilibiliVideo(
            aid = apiVideoInfo.aid,
            bvid = apiVideoInfo.bvid,
            title = apiVideoInfo.title,
            authorUid = apiVideoInfo.owner.mid.toString(),
            authorName = apiVideoInfo.owner.name,
            uploadTime = apiVideoInfo.publishTime * 1000 // 转换为毫秒
        )
    }

    /**
     * 将API响应的UserInfo转换为实体类BilibiliUser
     *
     * @param apiUserInfo API响应的用户信息
     * @param minecraftUuid 绑定的Minecraft UUID（可选）
     * @return BilibiliUser实体
     */
    fun convertToEntity(apiUserInfo: BilibiliApi.UserInfo, minecraftUuid: UUID? = null): BilibiliUser {
        return BilibiliUser(
            uid = apiUserInfo.mid.toString(),
            nickname = apiUserInfo.name,
            minecraftUuid = minecraftUuid,
            bindTime = System.currentTimeMillis()
        )
    }

    /**
     * 将API响应的TripleStatus转换为实体类TripleActionData
     *
     * @param apiTripleStatus API响应的三连状态
     * @return TripleActionData实体
     */
    fun convertToEntity(apiTripleStatus: BilibiliApi.TripleStatus): TripleActionData {
        return TripleActionData(
            liked = apiTripleStatus.like == 1,
            coined = apiTripleStatus.coin > 0,
            coinCount = apiTripleStatus.coin,
            favorited = apiTripleStatus.favorite == 1,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * 格式化播放量数字
     *
     * @param count 播放量
     * @return 格式化后的字符串，如"1.2万"
     */
    fun formatViewCount(count: Int): String {
        return when {
            count >= 100_000_000 -> "%.1f亿".format(count / 100_000_000.0)
            count >= 10_000 -> "%.1f万".format(count / 10_000.0)
            else -> count.toString()
        }
    }

    /**
     * 格式化时间差
     *
     * @param timestamp 时间戳（毫秒）
     * @return 格式化后的时间差字符串，如"3小时前"
     */
    fun formatTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "刚刚"
            diff < 3_600_000 -> "${diff / 60_000}分钟前"
            diff < 86_400_000 -> "${diff / 3_600_000}小时前"
            diff < 2_592_000_000 -> "${diff / 86_400_000}天前"
            else -> "${diff / 2_592_000_000}个月前"
        }
    }

    /**
     * 生成视频的简短描述
     *
     * @param video 视频实体
     * @return 简短描述
     */
    fun generateVideoDescription(video: BilibiliVideo): String {
        return "${video.title} - ${video.authorName} (${video.bvid})"
    }

    /**
     * 检查视频标题是否包含关键词
     *
     * @param title 视频标题
     * @param keywords 关键词列表
     * @return true表示包含任一关键词
     */
    fun containsKeywords(title: String, keywords: List<String>): Boolean {
        val lowerTitle = title.lowercase()
        return keywords.any { keyword ->
            lowerTitle.contains(keyword.lowercase())
        }
    }

    /**
     * 计算三连操作的得分权重
     *
     * @param actionData 三连操作数据
     * @param likeWeight 点赞权重（默认1.0）
     * @param coinWeight 投币权重（默认2.0）
     * @param favoriteWeight 收藏权重（默认1.5）
     * @return 加权得分
     */
    fun calculateWeightedScore(
        actionData: TripleActionData,
        likeWeight: Double = 1.0,
        coinWeight: Double = 2.0,
        favoriteWeight: Double = 1.5
    ): Double {
        var score = 0.0

        if (actionData.liked) score += likeWeight
        if (actionData.coined) score += actionData.coinCount * coinWeight
        if (actionData.favorited) score += favoriteWeight

        return score
    }

    /**
     * 验证Cookie格式
     *
     * @param cookie Cookie字符串
     * @return true表示格式看起来正确
     */
    fun isValidCookie(cookie: String?): Boolean {
        if (cookie.isNullOrBlank()) return false

        // 检查是否包含必要的字段
        val requiredFields = listOf("SESSDATA", "bili_jct", "DedeUserID")
        return requiredFields.any { field ->
            cookie.contains("$field=")
        }
    }
} 