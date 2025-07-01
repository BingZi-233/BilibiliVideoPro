package online.bingzi.bilibili.video.pro.internal.entity.netwrk.video

/**
 * 视频详情结果
 */
sealed class VideoDetailResult {
    data class Success(val data: VideoData) : VideoDetailResult()
    data class Error(val message: String) : VideoDetailResult()
}