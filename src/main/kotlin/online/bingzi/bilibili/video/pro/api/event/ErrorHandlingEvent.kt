package online.bingzi.bilibili.video.pro.api.event

import online.bingzi.bilibili.video.pro.api.entity.event.ErrorType
import org.bukkit.entity.Player

/**
 * 错误处理事件
 * 
 * 当插件内部发生错误时触发此事件。
 * 用于统一的错误处理、日志记录、监控报警和用户友好的错误展示。
 * 
 * @param player 相关的玩家，如果是系统级错误可能为null
 * @param errorType 错误类型
 * @param originalError 原始异常对象
 * @param errorMessage 错误消息
 * @param context 错误发生的上下文信息
 * 
 * @author BilibiliVideoPro
 * @since 2.0.0
 */
class ErrorHandlingEvent(
    /**
     * 相关的玩家
     * 如果错误与特定玩家相关则包含Player对象，系统级错误时为null
     */
    val player: Player?,

    /**
     * 错误类型
     * @see ErrorType
     */
    val errorType: ErrorType,

    /**
     * 原始异常对象
     * 包含完整的异常信息和堆栈跟踪
     */
    val originalError: Throwable,

    /**
     * 错误消息
     * 用户友好的错误描述，通常过滤掉敏感信息
     */
    val errorMessage: String,

    /**
     * 错误发生的上下文信息
     * 描述错误发生时的操作或功能模块，有助于问题定位
     */
    val context: String
) : BilibiliVideoProxyEvent()