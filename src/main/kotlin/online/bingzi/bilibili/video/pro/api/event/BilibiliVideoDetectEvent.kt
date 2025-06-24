package online.bingzi.bilibili.video.pro.api.event

import online.bingzi.bilibili.video.pro.api.entity.BilibiliUser
import online.bingzi.bilibili.video.pro.api.entity.BilibiliVideo

/**
 * Bilibili视频检测事件
 *
 * 当系统检测到用户的新视频操作或指定UP主发布新视频时触发此事件
 * 该事件可用于监控特定内容并触发相应的奖励机制
 *
 * @property user 相关的Bilibili用户
 * @property video 检测到的视频
 * @property detectType 检测类型
 * @property detectReason 检测原因
 * @property timestamp 检测时间戳
 *
 * @constructor 创建一个Bilibili视频检测事件
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
class BilibiliVideoDetectEvent(
    /**
     * 相关的Bilibili用户
     *
     * 可能是操作视频的用户或视频的作者
     */
    override val user: BilibiliUser,

    /**
     * 检测到的视频
     *
     * 触发检测的视频信息
     */
    val video: BilibiliVideo,

    /**
     * 检测类型
     *
     * 描述检测的具体类型
     */
    val detectType: DetectType,

    /**
     * 检测原因
     *
     * 详细说明触发检测的原因
     */
    val detectReason: String,

    /**
     * 检测时间戳
     *
     * 记录检测发生的时间
     */
    timestamp: Long = System.currentTimeMillis()
) : BilibiliEvent(user, timestamp) {

    /**
     * 检测类型枚举
     *
     * 定义了所有可能的视频检测类型
     */
    enum class DetectType(
        /**
         * 类型名称
         */
        val typeName: String,

        /**
         * 类型描述
         */
        val description: String
    ) {
        /**
         * 新视频发布
         *
         * 检测到特定UP主发布了新视频
         */
        NEW_VIDEO_UPLOAD("新视频发布", "检测到UP主发布了新视频"),

        /**
         * 用户观看视频
         *
         * 检测到用户观看了特定视频
         */
        USER_WATCH_VIDEO("用户观看", "检测到用户观看了视频"),

        /**
         * 热门视频
         *
         * 检测到视频进入了热门榜单
         */
        TRENDING_VIDEO("热门视频", "检测到视频进入热门榜单"),

        /**
         * 目标视频
         *
         * 检测到与任务目标相关的视频
         */
        TARGET_VIDEO("目标视频", "检测到与任务目标相关的视频"),

        /**
         * 特殊关注
         *
         * 检测到需要特别关注的视频内容
         */
        SPECIAL_ATTENTION("特殊关注", "检测到需要特别关注的视频内容")
    }

    /**
     * 检查是否为最近发布的视频
     *
     * @return true表示视频是最近发布的
     */
    fun isRecentVideo(): Boolean = video.isRecentlyUploaded()

    /**
     * 检查是否为特定作者的视频
     *
     * @param authorUid 作者UID
     * @return true表示视频属于指定作者
     */
    fun isVideoByAuthor(authorUid: String): Boolean = video.authorUid == authorUid

    /**
     * 检查是否需要立即处理
     *
     * @return true表示需要立即处理该检测事件
     */
    fun requiresImmediateAttention(): Boolean {
        return detectType in listOf(
            DetectType.NEW_VIDEO_UPLOAD,
            DetectType.TARGET_VIDEO,
            DetectType.SPECIAL_ATTENTION
        )
    }

    /**
     * 获取检测优先级
     *
     * @return 检测优先级，数值越大优先级越高
     */
    fun getDetectPriority(): Int {
        return when (detectType) {
            DetectType.SPECIAL_ATTENTION -> 10
            DetectType.TARGET_VIDEO -> 9
            DetectType.NEW_VIDEO_UPLOAD -> 8
            DetectType.TRENDING_VIDEO -> 7
            DetectType.USER_WATCH_VIDEO -> 6
        }
    }

    /**
     * 获取检测摘要
     *
     * @return 检测的简短摘要
     */
    fun getDetectSummary(): String {
        return "${detectType.typeName}: ${video.title} - ${video.authorName}"
    }

    override fun getEventDescription(): String {
        return "${detectType.typeName} - 用户 ${user.nickname} 相关视频《${video.title}》: $detectReason"
    }

    /**
     * 获取详细的检测报告
     *
     * @return 包含所有检测详情的报告
     */
    fun getDetailedDetectReport(): String {
        return buildString {
            appendLine("=== Bilibili视频检测报告 ===")
            appendLine("检测类型：${detectType.typeName}")
            appendLine("相关用户：${user.nickname} (${user.uid})")
            appendLine("视频标题：${video.title}")
            appendLine("视频BV号：${video.bvid}")
            appendLine("视频作者：${video.authorName}")
            appendLine("检测原因：$detectReason")
            appendLine("优先级：${getDetectPriority()}")
            appendLine("需要立即处理：${if (requiresImmediateAttention()) "是" else "否"}")
            appendLine("检测时间：${getFormattedTime()}")
            appendLine("视频链接：${video.getVideoUrl()}")
        }
    }

    override fun toString(): String {
        return "BilibiliVideoDetectEvent(${detectType.typeName}: ${video.bvid} by ${user.nickname})"
    }
} 