package online.bingzi.bilibili.video.pro.api.entity

/**
 * Bilibili视频实体
 *
 * 表示一个Bilibili视频的基本信息，用于插件中的视频管理和三连检测
 *
 * @property aid 视频AV号，数字格式的视频唯一标识符
 * @property bvid 视频BV号，字符串格式的视频唯一标识符
 * @property title 视频标题
 * @property authorUid 视频作者的UID
 * @property authorName 视频作者的昵称
 * @property uploadTime 视频上传时间戳
 *
 * @constructor 创建一个Bilibili视频实体
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
data class BilibiliVideo(
    /**
     * 视频AV号
     *
     * Bilibili视频的数字格式唯一标识符，通常以"av"开头
     * 例如：1234567890
     */
    val aid: Long,

    /**
     * 视频BV号
     *
     * Bilibili视频的字符串格式唯一标识符，通常以"BV"开头
     * 例如：BV1xx411c7mD
     */
    val bvid: String,

    /**
     * 视频标题
     *
     * 视频在Bilibili平台显示的标题
     */
    val title: String,

    /**
     * 视频作者UID
     *
     * 视频上传者在Bilibili平台的用户ID
     */
    val authorUid: String,

    /**
     * 视频作者昵称
     *
     * 视频上传者在Bilibili平台的显示名称
     */
    val authorName: String,

    /**
     * 视频上传时间
     *
     * 视频在Bilibili平台发布的时间戳
     */
    val uploadTime: Long = System.currentTimeMillis()
) {

    /**
     * 获取视频URL
     *
     * @return 基于BV号的视频链接
     */
    fun getVideoUrl(): String = "https://www.bilibili.com/video/$bvid"

    /**
     * 检查视频是否为最近上传（24小时内）
     *
     * @return true表示是最近上传的视频
     */
    fun isRecentlyUploaded(): Boolean {
        val twentyFourHours = 24 * 60 * 60 * 1000L
        return System.currentTimeMillis() - uploadTime < twentyFourHours
    }

    /**
     * 获取简短的视频描述
     *
     * @return 包含标题和作者的简短描述
     */
    fun getShortDescription(): String = "$title - $authorName"

    override fun toString(): String {
        return "BilibiliVideo(bvid='$bvid', title='$title', author='$authorName')"
    }
} 