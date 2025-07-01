package online.bingzi.bilibili.video.pro.internal.entity.netwrk.video

/**
 * 评论检查结果
 */
sealed class CommentCheckResult {
    data class Success(val hasCommented: Boolean) : CommentCheckResult()
    data class Error(val message: String) : CommentCheckResult()
}