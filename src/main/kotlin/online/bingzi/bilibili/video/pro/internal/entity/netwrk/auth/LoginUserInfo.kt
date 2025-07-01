package online.bingzi.bilibili.video.pro.internal.entity.netwrk.auth

/**
 * 登录用户信息
 */
data class LoginUserInfo(
    val mid: Long,          // 用户ID
    val username: String,   // 用户名
    val face: String,       // 头像URL
    val level: Int,         // 用户等级
    val isVip: Boolean      // 是否为大会员
)