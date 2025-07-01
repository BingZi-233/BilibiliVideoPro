package online.bingzi.bilibili.video.pro.internal.entity.netwrk.video

/**
 * 视频数据
 */
data class VideoData(
    val aid: Long,        // 视频aid
    val bvid: String,     // 视频bvid
    val title: String,    // 视频标题
    val ownerMid: Long    // UP主mid
)