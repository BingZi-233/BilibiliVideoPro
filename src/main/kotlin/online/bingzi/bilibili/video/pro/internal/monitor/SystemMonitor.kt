package online.bingzi.bilibili.video.pro.internal.monitor

import online.bingzi.bilibili.video.pro.internal.database.DatabaseManager
import online.bingzi.bilibili.video.pro.internal.error.ErrorHandler
import online.bingzi.bilibili.video.pro.internal.security.SecureKeyManager
import taboolib.common.platform.function.console
import taboolib.module.lang.sendInfo
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 系统监控器
 * 提供性能统计、健康检查和系统监控功能
 */
object SystemMonitor {
    
    // 性能统计
    private val requestCounters = ConcurrentHashMap<String, AtomicLong>()
    private val executionTimes = ConcurrentHashMap<String, MutableList<Long>>()
    private val startTime = System.currentTimeMillis()
    
    // 健康状态缓存
    private var lastHealthCheck = 0L
    private var cachedHealthStatus: HealthStatus? = null
    private val healthCheckCacheTime = 30000L // 30秒缓存
    
    data class HealthStatus(
        val overall: HealthLevel,
        val database: HealthLevel,
        val security: HealthLevel,
        val network: HealthLevel,
        val memory: HealthLevel,
        val details: Map<String, Any>,
        val timestamp: LocalDateTime = LocalDateTime.now()
    )
    
    enum class HealthLevel {
        HEALTHY, WARNING, CRITICAL, UNKNOWN
    }
    
    data class PerformanceStats(
        val requestCounts: Map<String, Long>,
        val averageExecutionTimes: Map<String, Double>,
        val uptime: Long,
        val memoryUsage: MemoryUsage,
        val errorStatistics: Map<String, Any>
    )
    
    data class MemoryUsage(
        val used: Long,
        val free: Long,
        val total: Long,
        val max: Long,
        val usagePercentage: Double
    )
    
    /**
     * 记录操作执行时间
     */
    fun recordExecution(operation: String, executionTime: Long) {
        try {
            // 增加请求计数
            requestCounters.computeIfAbsent(operation) { AtomicLong(0) }.incrementAndGet()
            
            // 记录执行时间（只保留最近100次）
            executionTimes.computeIfAbsent(operation) { mutableListOf() }.let { times ->
                synchronized(times) {
                    times.add(executionTime)
                    if (times.size > 100) {
                        times.removeAt(0)
                    }
                }
            }
        } catch (e: Exception) {
            console().sendInfo("systemMonitorRecordError", e.message ?: "unknown")
        }
    }
    
    /**
     * 测量操作执行时间
     */
    inline fun <T> measureTime(operation: String, block: () -> T): T {
        val startTime = System.currentTimeMillis()
        return try {
            block()
        } finally {
            val executionTime = System.currentTimeMillis() - startTime
            recordExecution(operation, executionTime)
        }
    }
    
    /**
     * 获取系统健康状态
     */
    fun getHealthStatus(forceRefresh: Boolean = false): HealthStatus {
        val now = System.currentTimeMillis()
        
        // 检查缓存
        if (!forceRefresh && cachedHealthStatus != null && (now - lastHealthCheck) < healthCheckCacheTime) {
            return cachedHealthStatus!!
        }
        
        return try {
            console().sendInfo("systemMonitorHealthCheck")
            
            val databaseHealth = checkDatabaseHealth()
            val securityHealth = checkSecurityHealth()
            val networkHealth = checkNetworkHealth()
            val memoryHealth = checkMemoryHealth()
            
            val overall = determineOverallHealth(databaseHealth, securityHealth, networkHealth, memoryHealth)
            
            val details = mutableMapOf<String, Any>()
            details["database_status"] = databaseHealth.name
            details["security_status"] = securityHealth.name
            details["network_status"] = networkHealth.name
            details["memory_status"] = memoryHealth.name
            details["uptime_seconds"] = (now - startTime) / 1000
            details["last_check_time"] = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            
            // 添加错误统计
            details["error_statistics"] = ErrorHandler.getErrorStatistics().map { (key, stats) ->
                key to mapOf(
                    "count" to stats.count,
                    "last_occurrence" to stats.lastOccurrence.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
            }
            
            val healthStatus = HealthStatus(
                overall = overall,
                database = databaseHealth,
                security = securityHealth,
                network = networkHealth,
                memory = memoryHealth,
                details = details
            )
            
            // 更新缓存
            cachedHealthStatus = healthStatus
            lastHealthCheck = now
            
            console().sendInfo("systemMonitorHealthComplete", overall.name)
            healthStatus
            
        } catch (e: Exception) {
            console().sendInfo("systemMonitorHealthFailed", e.message ?: "unknown")
            HealthStatus(
                overall = HealthLevel.UNKNOWN,
                database = HealthLevel.UNKNOWN,
                security = HealthLevel.UNKNOWN,
                network = HealthLevel.UNKNOWN,
                memory = HealthLevel.UNKNOWN,
                details = mapOf("error" to (e.message ?: "unknown error"))
            )
        }
    }
    
    /**
     * 检查数据库健康状态
     */
    private fun checkDatabaseHealth(): HealthLevel {
        return try {
            val healthInfo = DatabaseManager.healthCheck()
            if (healthInfo.isHealthy) {
                HealthLevel.HEALTHY
            } else {
                HealthLevel.CRITICAL
            }
        } catch (e: Exception) {
            HealthLevel.CRITICAL
        }
    }
    
    /**
     * 检查安全系统健康状态
     */
    private fun checkSecurityHealth(): HealthLevel {
        return try {
            if (SecureKeyManager.verifyKeyIntegrity()) {
                HealthLevel.HEALTHY
            } else {
                HealthLevel.CRITICAL
            }
        } catch (e: Exception) {
            HealthLevel.CRITICAL
        }
    }
    
    /**
     * 检查网络健康状态
     */
    private fun checkNetworkHealth(): HealthLevel {
        // 简单的网络健康检查，可以扩展
        return try {
            // 检查是否有网络相关的错误
            val networkErrors = ErrorHandler.getErrorStatistics(ErrorHandler.ErrorType.NETWORK)
            when {
                networkErrors.isEmpty() -> HealthLevel.HEALTHY
                networkErrors.values.any { it.count > 10 } -> HealthLevel.WARNING
                networkErrors.values.any { it.count > 50 } -> HealthLevel.CRITICAL
                else -> HealthLevel.HEALTHY
            }
        } catch (e: Exception) {
            HealthLevel.UNKNOWN
        }
    }
    
    /**
     * 检查内存健康状态
     */
    private fun checkMemoryHealth(): HealthLevel {
        return try {
            val runtime = Runtime.getRuntime()
            val maxMemory = runtime.maxMemory()
            val totalMemory = runtime.totalMemory()
            val freeMemory = runtime.freeMemory()
            val usedMemory = totalMemory - freeMemory
            val usagePercentage = (usedMemory.toDouble() / maxMemory.toDouble()) * 100
            
            when {
                usagePercentage < 70 -> HealthLevel.HEALTHY
                usagePercentage < 85 -> HealthLevel.WARNING
                else -> HealthLevel.CRITICAL
            }
        } catch (e: Exception) {
            HealthLevel.UNKNOWN
        }
    }
    
    /**
     * 确定总体健康状态
     */
    private fun determineOverallHealth(vararg levels: HealthLevel): HealthLevel {
        return when {
            levels.any { it == HealthLevel.CRITICAL } -> HealthLevel.CRITICAL
            levels.any { it == HealthLevel.WARNING } -> HealthLevel.WARNING
            levels.any { it == HealthLevel.UNKNOWN } -> HealthLevel.WARNING
            levels.all { it == HealthLevel.HEALTHY } -> HealthLevel.HEALTHY
            else -> HealthLevel.UNKNOWN
        }
    }
    
    /**
     * 获取性能统计
     */
    fun getPerformanceStats(): PerformanceStats {
        return try {
            val requestCounts = requestCounters.mapValues { it.value.get() }
            
            val averageExecutionTimes = executionTimes.mapValues { (_, times) ->
                synchronized(times) {
                    if (times.isEmpty()) 0.0 else times.average()
                }
            }
            
            val runtime = Runtime.getRuntime()
            val memoryUsage = MemoryUsage(
                used = runtime.totalMemory() - runtime.freeMemory(),
                free = runtime.freeMemory(),
                total = runtime.totalMemory(),
                max = runtime.maxMemory(),
                usagePercentage = ((runtime.totalMemory() - runtime.freeMemory()).toDouble() / runtime.maxMemory().toDouble()) * 100
            )
            
            PerformanceStats(
                requestCounts = requestCounts,
                averageExecutionTimes = averageExecutionTimes,
                uptime = System.currentTimeMillis() - startTime,
                memoryUsage = memoryUsage,
                errorStatistics = ErrorHandler.getErrorStatistics().mapValues { (_, stats) ->
                    mapOf(
                        "count" to stats.count,
                        "last_occurrence" to stats.lastOccurrence.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    )
                }
            )
        } catch (e: Exception) {
            console().sendInfo("systemMonitorStatsError", e.message ?: "unknown")
            PerformanceStats(
                requestCounts = emptyMap(),
                averageExecutionTimes = emptyMap(),
                uptime = 0L,
                memoryUsage = MemoryUsage(0, 0, 0, 0, 0.0),
                errorStatistics = emptyMap()
            )
        }
    }
    
    /**
     * 生成系统报告
     */
    fun generateSystemReport(): String {
        return try {
            val healthStatus = getHealthStatus()
            val performanceStats = getPerformanceStats()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            
            buildString {
                appendLine("=== BilibiliVideoPro 系统报告 ===")
                appendLine("生成时间: ${LocalDateTime.now().format(formatter)}")
                appendLine()
                
                appendLine("## 系统健康状态")
                appendLine("总体状态: ${healthStatus.overall.name}")
                appendLine("数据库: ${healthStatus.database.name}")
                appendLine("安全系统: ${healthStatus.security.name}")
                appendLine("网络: ${healthStatus.network.name}")
                appendLine("内存: ${healthStatus.memory.name}")
                appendLine()
                
                appendLine("## 性能统计")
                appendLine("运行时间: ${performanceStats.uptime / 1000}秒")
                appendLine("内存使用: ${String.format("%.2f", performanceStats.memoryUsage.usagePercentage)}%")
                appendLine()
                
                if (performanceStats.requestCounts.isNotEmpty()) {
                    appendLine("请求统计:")
                    performanceStats.requestCounts.forEach { (operation, count) ->
                        val avgTime = performanceStats.averageExecutionTimes[operation] ?: 0.0
                        appendLine("  $operation: $count 次, 平均耗时: ${String.format("%.2f", avgTime)}ms")
                    }
                    appendLine()
                }
                
                if (performanceStats.errorStatistics.isNotEmpty()) {
                    appendLine("错误统计:")
                    performanceStats.errorStatistics.forEach { (key, stats) ->
                        @Suppress("UNCHECKED_CAST")
                        val statsMap = stats as Map<String, Any>
                        appendLine("  $key: ${statsMap["count"]} 次, 最近: ${statsMap["last_occurrence"]}")
                    }
                    appendLine()
                }
                
                appendLine("========================")
            }
        } catch (e: Exception) {
            "生成系统报告时出错: ${e.message}"
        }
    }
    
    /**
     * 清理统计数据
     */
    fun clearStatistics() {
        try {
            requestCounters.clear()
            executionTimes.clear()
            cachedHealthStatus = null
            lastHealthCheck = 0L
            console().sendInfo("systemStatisticsCleared")
        } catch (e: Exception) {
            console().sendInfo("systemStatisticsClearError", e.message ?: "unknown")
        }
    }
}