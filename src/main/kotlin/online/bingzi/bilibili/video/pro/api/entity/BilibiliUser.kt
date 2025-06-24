package online.bingzi.bilibili.video.pro.api.entity

import java.util.*

/**
 * Bilibili用户实体
 *
 * 表示一个Bilibili用户的基本信息，用于插件中的用户管理和奖励发放
 *
 * @property uid Bilibili用户ID，唯一标识符
 * @property nickname 用户昵称
 * @property minecraftUuid 绑定的Minecraft玩家UUID，可为空表示未绑定
 * @property bindTime 绑定时间，用于跟踪绑定历史
 *
 * @constructor 创建一个Bilibili用户实体
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
data class BilibiliUser(
    /**
     * Bilibili用户ID
     *
     * 这是用户在Bilibili平台的唯一标识符，通常为数字字符串
     */
    val uid: String,

    /**
     * 用户昵称
     *
     * 用户在Bilibili平台显示的名称
     */
    val nickname: String,

    /**
     * 绑定的Minecraft玩家UUID
     *
     * 当用户绑定Minecraft账号时，存储对应的玩家UUID
     * null表示尚未绑定或已解绑
     */
    val minecraftUuid: UUID? = null,

    /**
     * 绑定时间
     *
     * 记录用户绑定Minecraft账号的时间戳
     * 用于统计和管理绑定历史
     */
    val bindTime: Long = System.currentTimeMillis()
) {

    /**
     * 检查用户是否已绑定Minecraft账号
     *
     * @return true表示已绑定，false表示未绑定
     */
    fun isBound(): Boolean = minecraftUuid != null

    /**
     * 获取绑定时长（毫秒）
     *
     * @return 从绑定到现在的时长，单位为毫秒
     */
    fun getBindDuration(): Long = System.currentTimeMillis() - bindTime

    override fun toString(): String {
        return "BilibiliUser(uid='$uid', nickname='$nickname', isBound=${isBound()})"
    }
} 