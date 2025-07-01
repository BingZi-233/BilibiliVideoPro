package online.bingzi.bilibili.video.pro.internal.entity.netwrk.video

/**
 * 视频互动状态结果
 */
sealed class VideoInteractionResult {
    data class Success(val status: VideoInteractionStatus) : VideoInteractionResult()
    data class Error(val message: String) : VideoInteractionResult()
}