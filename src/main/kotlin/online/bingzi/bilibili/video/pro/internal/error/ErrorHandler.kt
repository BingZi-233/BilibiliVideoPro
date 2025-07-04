package online.bingzi.bilibili.video.pro.internal.error

import online.bingzi.bilibili.video.pro.internal.security.LogSecurityFilter
import taboolib.common.platform.function.console
import taboolib.module.lang.sendInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

/**
 * 全局错误处理器
 * 提供统一的错误处理、日志记录和恢复机制
 */
object ErrorHandler {
    
    private val errorStats = ConcurrentHashMap<String, ErrorStatistics>()
    private val errorCallbacks = ConcurrentHashMap<ErrorType, MutableList<ErrorCallback>>()
    
    data class ErrorStatistics(
        var count: Long = 0,
        var lastOccurrence: LocalDateTime = LocalDateTime.now(),
        var firstOccurrence: LocalDateTime = LocalDateTime.now()
    )
    
    enum class ErrorType {
        DATABASE,
        NETWORK,
        SECURITY,
        VALIDATION,
        RUNTIME,
        CONFIGURATION,
        EXTERNAL_API
    }
    
    data class ErrorContext(
        val type: ErrorType,
        val component: String,
        val operation: String,
        val exception: Throwable,
        val metadata: Map<String, Any> = emptyMap(),
        val timestamp: LocalDateTime = LocalDateTime.now()
    )
    
    fun interface ErrorCallback {
        fun onError(context: ErrorContext): Boolean // return true if error was handled
    }
    
    /**
     * 注册错误回调处理器
     */
    fun registerErrorCallback(type: ErrorType, callback: ErrorCallback) {
        errorCallbacks.computeIfAbsent(type) { mutableListOf() }.add(callback)
    }
    
    /**
     * 处理错误
     */
    fun handleError(
        type: ErrorType,
        component: String,
        operation: String,
        exception: Throwable,
        metadata: Map<String, Any> = emptyMap(),
        shouldRetry: Boolean = false
    ): Boolean {
        val context = ErrorContext(type, component, operation, exception, metadata)
        
        // 记录错误统计
        recordErrorStatistics(context)
        
        // 记录错误日志
        logError(context)
        
        // 执行注册的错误回调
        val handled = executeErrorCallbacks(context)
        
        // 如果需要重试且错误未被处理
        if (shouldRetry && !handled) {
            return attemptRecovery(context)
        }
        
        return handled
    }
    
    /**
     * 记录错误统计
     */
    private fun recordErrorStatistics(context: ErrorContext) {
        val key = "${context.type}-${context.component}-${context.operation}"
        
        errorStats.compute(key) { _, existing ->
            if (existing == null) {
                ErrorStatistics(
                    count = 1,
                    firstOccurrence = context.timestamp,
                    lastOccurrence = context.timestamp
                )
            } else {
                existing.copy(
                    count = existing.count + 1,
                    lastOccurrence = context.timestamp
                )
            }
        }
    }
    
    /**
     * 记录错误日志
     */
    private fun logError(context: ErrorContext) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val timestamp = context.timestamp.format(formatter)
        
        // 判断是否为生产环境
        val isProduction = !isDebugMode()
        
        console().sendInfo("=== 错误报告 ===")
        console().sendInfo("时间: $timestamp")
        console().sendInfo("类型: ${context.type}")
        console().sendInfo("组件: ${context.component}")
        console().sendInfo("操作: ${context.operation}")
        console().sendInfo("异常: ${context.exception.javaClass.simpleName}")
        
        // 过滤敏感信息后记录错误消息
        val safeMessage = LogSecurityFilter.filterForEnvironment(
            context.exception.message ?: "未知错误", 
            isProduction
        )
        console().sendInfo("消息: $safeMessage")
        
        if (context.metadata.isNotEmpty()) {
            console().sendInfo("元数据:")
            context.metadata.forEach { (key, value) ->
                val safeValue = LogSecurityFilter.filterForEnvironment(value.toString(), isProduction)
                console().sendInfo("  $key: $safeValue")
            }
        }
        
        // 打印堆栈跟踪（仅在调试模式下，且经过安全过滤）
        if (isDebugMode()) {
            console().sendInfo("堆栈跟踪:")
            val safeStackTrace = LogSecurityFilter.filterStackTrace(
                context.exception.stackTraceToString()
            )
            console().sendInfo(safeStackTrace)
        }
        
        console().sendInfo("================")
    }
    
    /**
     * 执行错误回调
     */
    private fun executeErrorCallbacks(context: ErrorContext): Boolean {
        val callbacks = errorCallbacks[context.type] ?: return false
        
        for (callback in callbacks) {
            try {
                if (callback.onError(context)) {
                    console().sendInfo("错误已被回调处理器处理: ${context.type}-${context.component}")
                    return true
                }
            } catch (e: Exception) {
                console().sendInfo("错误回调处理器执行失败: ${e.message}")
            }
        }
        
        return false
    }
    
    /**
     * 尝试错误恢复
     */
    private fun attemptRecovery(context: ErrorContext): Boolean {
        return when (context.type) {
            ErrorType.DATABASE -> attemptDatabaseRecovery(context)
            ErrorType.NETWORK -> attemptNetworkRecovery(context)
            ErrorType.SECURITY -> attemptSecurityRecovery(context)
            else -> false
        }
    }
    
    /**
     * 数据库错误恢复
     */
    private fun attemptDatabaseRecovery(context: ErrorContext): Boolean {
        console().sendInfo("尝试数据库错误恢复: ${context.operation}")
        
        return try {
            // 检查数据库连接是否有效
            val databaseManager = Class.forName("online.bingzi.bilibili.video.pro.internal.database.DatabaseManager")
            val isValidMethod = databaseManager.getDeclaredMethod("isConnectionValid")
            val isValid = isValidMethod.invoke(databaseManager.getDeclaredField("INSTANCE").get(null)) as Boolean
            
            if (!isValid) {
                // 尝试重新连接数据库
                val initializeMethod = databaseManager.getDeclaredMethod("initialize")
                val success = initializeMethod.invoke(databaseManager.getDeclaredField("INSTANCE").get(null)) as Boolean
                
                if (success) {
                    console().sendInfo("数据库连接恢复成功")
                    return true
                }
            }
            
            false
        } catch (e: Exception) {
            console().sendInfo("数据库恢复失败: ${e.message}")
            false
        }
    }
    
    /**
     * 网络错误恢复
     */
    private fun attemptNetworkRecovery(context: ErrorContext): Boolean {
        console().sendInfo("尝试网络错误恢复: ${context.operation}")
        
        // 对于网络错误，通常只需要简单重试
        return when {
            context.exception.message?.contains("timeout", ignoreCase = true) == true -> {
                console().sendInfo("检测到超时错误，建议稍后重试")
                false
            }
            context.exception.message?.contains("connection", ignoreCase = true) == true -> {
                console().sendInfo("检测到连接错误，建议检查网络")
                false
            }
            else -> false
        }
    }
    
    /**
     * 安全错误恢复
     */
    private fun attemptSecurityRecovery(context: ErrorContext): Boolean {
        console().sendInfo("尝试安全错误恢复: ${context.operation}")
        
        // 安全错误通常不应该自动恢复，需要管理员干预
        console().sendInfo("安全错误需要管理员干预，停止自动恢复")
        return false
    }
    
    /**
     * 获取错误统计
     */
    fun getErrorStatistics(): Map<String, ErrorStatistics> {
        return errorStats.toMap()
    }
    
    /**
     * 获取特定类型的错误统计
     */
    fun getErrorStatistics(type: ErrorType): Map<String, ErrorStatistics> {
        val prefix = type.name
        return errorStats.filterKeys { it.startsWith(prefix) }
    }
    
    /**
     * 清除错误统计
     */
    fun clearErrorStatistics() {
        errorStats.clear()
        console().sendInfo("错误统计已清除")
    }
    
    /**
     * 检查是否为调试模式
     */
    private fun isDebugMode(): Boolean {
        return try {
            val configClass = Class.forName("online.bingzi.bilibili.video.pro.internal.config.DebugConfig")
            val isEnabledField = configClass.getDeclaredField("enabled")
            isEnabledField.getBoolean(null)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取错误报告
     */
    fun generateErrorReport(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val now = LocalDateTime.now().format(formatter)
        
        val report = StringBuilder()
        report.appendLine("=== BilibiliVideoPro 错误报告 ===")
        report.appendLine("生成时间: $now")
        report.appendLine()
        
        if (errorStats.isEmpty()) {
            report.appendLine("没有记录到错误")
        } else {
            report.appendLine("错误统计:")
            errorStats.forEach { (key, stats) ->
                report.appendLine("  $key:")
                report.appendLine("    错误次数: ${stats.count}")
                report.appendLine("    首次发生: ${stats.firstOccurrence.format(formatter)}")
                report.appendLine("    最近发生: ${stats.lastOccurrence.format(formatter)}")
                report.appendLine()
            }
        }
        
        report.appendLine("============================")
        
        return report.toString()
    }
}