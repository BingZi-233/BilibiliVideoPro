package online.bingzi.bilibili.video.pro.internal.entity.netwrk.auth

/**
 * 登录轮询结果
 */
sealed class LoginPollResult {
    data class Success(val message: String) : LoginPollResult()
    data class WaitingScan(val message: String) : LoginPollResult()
    data class WaitingConfirm(val message: String) : LoginPollResult()
    data class Expired(val message: String) : LoginPollResult()
    data class Error(val message: String) : LoginPollResult()
}