package online.bingzi.bilibili.video.pro.api.event

/**
 * 系统状态查询事件
 *
 * 当管理员或系统内部查询插件运行状态时触发此事件。
 * 包含健康状态和性能统计信息，可以用于监控、日志记录或报警。
 *
 * @param requester 查询请求者，可能是CommandSender或其他系统组件
 * @param healthStatus 健康状态信息映射
 * @param performanceStats 性能统计信息映射
 *
 * @author BilibiliVideoPro
 * @since 2.0.0
 */
class SystemStatusQueryEvent(
    /**
     * 查询请求者
     * 可能是CommandSender（控制台或玩家）或其他系统组件
     */
    val requester: Any,

    /**
     * 健康状态信息
     * 键为状态项名称，值为状态描述
     * 例如：{"database" -> "HEALTHY", "network" -> "WARNING"}
     */
    val healthStatus: Map<String, String>,

    /**
     * 性能统计信息
     * 键为统计项名称，值为统计数据（可能是数字、字符串等）
     * 例如：{"uptime" -> 3600000L, "memory_usage" -> 75.5}
     */
    val performanceStats: Map<String, Any>
) : BilibiliVideoProxyEvent()