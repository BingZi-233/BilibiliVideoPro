package online.bingzi.bilibili.video.pro.internal.entity.netwrk.video

/**
 * 关注状态结果
 */
sealed class FollowResult {
    data class Success(val isFollowing: Boolean) : FollowResult()
    data class Error(val message: String) : FollowResult()
}