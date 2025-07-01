package online.bingzi.bilibili.video.pro.internal.entity.netwrk.video

/**
 * 三连状态结果
 */
sealed class TripleActionResult {
    data class Success(val status: TripleActionStatus) : TripleActionResult()
    data class Error(val message: String) : TripleActionResult()
}