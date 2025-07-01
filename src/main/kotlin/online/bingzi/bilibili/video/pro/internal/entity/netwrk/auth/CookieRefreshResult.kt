package online.bingzi.bilibili.video.pro.internal.entity.netwrk.auth

/**
 * Cookie刷新结果
 */
sealed class CookieRefreshResult {
    data class Success(val message: String, val refreshToken: String?) : CookieRefreshResult()
    data class Error(val message: String) : CookieRefreshResult()
}