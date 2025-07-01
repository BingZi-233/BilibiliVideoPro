package online.bingzi.bilibili.video.pro.internal.entity.netwrk.video

/**
 * 完整视频互动状态
 */
data class VideoInteractionStatus(
    val videoData: VideoData,
    val tripleAction: TripleActionStatus,
    val isFollowingUp: Boolean,
    val hasCommented: Boolean
)