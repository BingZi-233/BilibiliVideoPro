package online.bingzi.bilibili.video.pro.internal.security

import java.util.regex.Pattern

/**
 * 日志安全过滤器
 * 过滤日志中的敏感信息，防止信息泄露
 */
object LogSecurityFilter {
    
    // 敏感信息的正则表达式模式
    private val sensitivePatterns = listOf(
        // Cookie相关
        Pattern.compile("(SESSDATA|bili_jct|DedeUserID)[=:]\\s*([^\\s,;]+)", Pattern.CASE_INSENSITIVE),
        // 密码相关
        Pattern.compile("(password|pwd|pass)[=:]\\s*([^\\s,;]+)", Pattern.CASE_INSENSITIVE),
        // Token相关
        Pattern.compile("(token|auth|key)[=:]\\s*([^\\s,;]+)", Pattern.CASE_INSENSITIVE),
        // 数据库连接字符串
        Pattern.compile("(jdbc:[^\\s]+://[^\\s@]+):([^@\\s]+)@", Pattern.CASE_INSENSITIVE),
        // IP地址（部分遮蔽）
        Pattern.compile("\\b(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\b"),
        // UUID（部分遮蔽）
        Pattern.compile("\\b([0-9a-fA-F]{8})-([0-9a-fA-F]{4})-([0-9a-fA-F]{4})-([0-9a-fA-F]{4})-([0-9a-fA-F]{12})\\b"),
        // 邮箱地址（部分遮蔽）
        Pattern.compile("\\b([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})\\b")
    )
    
    // 替换模式
    private val replacementMap = mapOf(
        "cookie" to "***COOKIE***",
        "password" to "***PASSWORD***", 
        "token" to "***TOKEN***",
        "database" to "***DATABASE***",
        "ip" to "***IP***",
        "uuid" to "***UUID***",
        "email" to "***EMAIL***"
    )
    
    /**
     * 过滤日志消息中的敏感信息
     */
    fun filterSensitiveInfo(message: String): String {
        var filteredMessage = message
        
        try {
            // 过滤Cookie信息
            filteredMessage = sensitivePatterns[0].matcher(filteredMessage).replaceAll("$1=${replacementMap["cookie"]}")
            
            // 过滤密码信息
            filteredMessage = sensitivePatterns[1].matcher(filteredMessage).replaceAll("$1=${replacementMap["password"]}")
            
            // 过滤Token信息
            filteredMessage = sensitivePatterns[2].matcher(filteredMessage).replaceAll("$1=${replacementMap["token"]}")
            
            // 过滤数据库连接字符串
            filteredMessage = sensitivePatterns[3].matcher(filteredMessage).replaceAll("$1:${replacementMap["database"]}")
            
            // 过滤IP地址（保留前两段）
            filteredMessage = sensitivePatterns[4].matcher(filteredMessage).replaceAll("$1.$2.***.**")
            
            // 过滤UUID（只保留前8位）
            filteredMessage = sensitivePatterns[5].matcher(filteredMessage).replaceAll("$1-****-****-****-************")
            
            // 过滤邮箱地址（部分遮蔽）
            filteredMessage = sensitivePatterns[6].matcher(filteredMessage).replaceAll("***@$2")
            
        } catch (e: Exception) {
            // 如果过滤过程出错，返回通用的安全消息
            return "日志消息包含敏感信息，已被安全过滤"
        }
        
        return filteredMessage
    }
    
    /**
     * 过滤异常堆栈跟踪中的敏感信息
     */
    fun filterStackTrace(stackTrace: String): String {
        var filteredTrace = stackTrace
        
        try {
            // 移除文件路径中的用户相关信息
            filteredTrace = filteredTrace.replace(
                Regex("(/Users/[^/]+|C:\\\\Users\\\\[^\\\\]+)"),
                "/***USER***/") 
            
            // 移除可能的数据库连接信息
            filteredTrace = filteredTrace.replace(
                Regex("jdbc:[^\\s)]+"),
                "jdbc:***DATABASE***")
            
            // 移除其他敏感路径
            filteredTrace = filteredTrace.replace(
                Regex("(password|token|key)=[^\\s,)]+"),
                "$1=***FILTERED***")
                
        } catch (e: Exception) {
            return "堆栈跟踪包含敏感信息，已被安全过滤"
        }
        
        return filteredTrace
    }
    
    /**
     * 检查消息是否包含敏感信息
     */
    fun containsSensitiveInfo(message: String): Boolean {
        return try {
            sensitivePatterns.any { pattern ->
                pattern.matcher(message).find()
            }
        } catch (e: Exception) {
            // 如果检查过程出错，假设包含敏感信息
            true
        }
    }
    
    /**
     * 为不同环境过滤日志
     */
    fun filterForEnvironment(message: String, isProduction: Boolean): String {
        return if (isProduction) {
            // 生产环境：严格过滤
            filterSensitiveInfo(message)
        } else {
            // 开发环境：温和过滤（保留更多信息用于调试）
            if (containsSensitiveInfo(message)) {
                "[DEV] " + filterSensitiveInfo(message)
            } else {
                message
            }
        }
    }
    
    /**
     * 获取安全的错误消息（用于返回给用户）
     */
    fun getSafeErrorMessage(originalError: String): String {
        return when {
            originalError.contains("SQLException", ignoreCase = true) -> "数据库操作失败"
            originalError.contains("IOException", ignoreCase = true) -> "文件操作失败" 
            originalError.contains("SecurityException", ignoreCase = true) -> "安全验证失败"
            originalError.contains("NetworkException", ignoreCase = true) -> "网络连接失败"
            originalError.contains("timeout", ignoreCase = true) -> "操作超时"
            else -> "系统内部错误"
        }
    }
}