package online.bingzi.bilibili.video.pro.api.entity.event

/**
 * 错误类型枚举
 * 
 * 定义了插件中可能出现的各种错误类型。
 * 用于错误分类、处理和统计分析。
 * 
 * @author BilibiliVideoPro
 * @since 2.0.0
 */
enum class ErrorType {
    /**
     * 网络错误
     * 包括HTTP请求失败、连接超时、API响应错误等
     */
    NETWORK_ERROR,
    
    /**
     * 数据库错误
     * 包括连接失败、查询错误、事务回滚等
     */
    DATABASE_ERROR,
    
    /**
     * 验证错误
     * 包括输入参数验证失败、格式错误等
     */
    VALIDATION_ERROR,
    
    /**
     * 认证错误
     * 包括登录失败、token无效、权限不足等
     */
    AUTHENTICATION_ERROR,
    
    /**
     * 权限错误
     * 包括命令权限不足、操作权限缺失等
     */
    PERMISSION_ERROR,
    
    /**
     * 脚本错误
     * 包括Kether脚本执行失败、语法错误等
     */
    SCRIPT_ERROR,
    
    /**
     * 系统错误
     * 包括未知异常、系统级别错误等
     */
    SYSTEM_ERROR
}