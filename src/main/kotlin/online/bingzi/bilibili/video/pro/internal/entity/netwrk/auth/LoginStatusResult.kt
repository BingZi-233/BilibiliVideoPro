package online.bingzi.bilibili.video.pro.internal.entity.netwrk.auth

/**
 * 登录状态结果
 */
sealed class LoginStatusResult {
    data class Success(val userInfo: LoginUserInfo) : LoginStatusResult()
    data class NotLoggedIn(val message: String) : LoginStatusResult()
    data class Error(val message: String) : LoginStatusResult()
}