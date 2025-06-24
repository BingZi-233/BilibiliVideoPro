package online.bingzi.bilibili.video.pro.internal.config

/**
 * 数据库配置类
 *
 * 用于管理数据库连接配置，支持SQLite和MySQL两种数据源
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
data class DatabaseConfig(
    /**
     * 数据库类型
     */
    val type: DatabaseType,

    /**
     * 数据库主机地址（MySQL使用）
     */
    val host: String = "localhost",

    /**
     * 数据库端口（MySQL使用）
     */
    val port: Int = 3306,

    /**
     * 数据库名称
     */
    val database: String,

    /**
     * 用户名（MySQL使用）
     */
    val username: String = "",

    /**
     * 密码（MySQL使用）
     */
    val password: String = "",

    /**
     * SQLite文件路径（SQLite使用）
     */
    val filePath: String = "plugins/BilibiliVideoPro/data.db",

    /**
     * 连接池配置
     */
    val poolConfig: PoolConfig = PoolConfig()
) {

    /**
     * 数据库类型枚举
     */
    enum class DatabaseType {
        /**
         * SQLite数据库
         * - 轻量级、文件型数据库
         * - 适合小型应用和开发环境
         * - 无需额外服务器
         */
        SQLITE,

        /**
         * MySQL数据库
         * - 功能强大的关系型数据库
         * - 适合生产环境和大型应用
         * - 支持多用户并发访问
         */
        MYSQL
    }

    /**
     * 连接池配置
     *
     * @property maxPoolSize 最大连接池大小
     * @property minIdle 最小空闲连接数
     * @property maxLifetime 连接最大生命周期（毫秒）
     * @property connectionTimeout 连接超时时间（毫秒）
     * @property idleTimeout 空闲超时时间（毫秒）
     * @property validationTimeout 验证超时时间（毫秒）
     */
    data class PoolConfig(
        val maxPoolSize: Int = 10,
        val minIdle: Int = 2,
        val maxLifetime: Long = 600000L,    // 10分钟
        val connectionTimeout: Long = 30000L, // 30秒
        val idleTimeout: Long = 300000L,     // 5分钟
        val validationTimeout: Long = 5000L   // 5秒
    )

    /**
     * 获取JDBC连接URL
     *
     * @return JDBC连接字符串
     */
    fun getJdbcUrl(): String {
        return when (type) {
            DatabaseType.SQLITE -> {
                "jdbc:sqlite:$filePath"
            }

            DatabaseType.MYSQL -> {
                "jdbc:mysql://$host:$port/$database" +
                        "?useUnicode=true" +
                        "&characterEncoding=utf8" +
                        "&useSSL=false" +
                        "&allowPublicKeyRetrieval=true" +
                        "&serverTimezone=Asia/Shanghai" +
                        "&rewriteBatchedStatements=true"
            }
        }
    }

    /**
     * 获取数据库驱动类名
     *
     * @return 驱动类名
     */
    fun getDriverClassName(): String {
        return when (type) {
            DatabaseType.SQLITE -> "org.sqlite.JDBC"
            DatabaseType.MYSQL -> "com.mysql.cj.jdbc.Driver"
        }
    }

    /**
     * 验证配置是否完整
     *
     * @return 验证结果和错误信息
     */
    fun validate(): Pair<Boolean, String?> {
        when (type) {
            DatabaseType.SQLITE -> {
                if (filePath.isBlank()) {
                    return false to "dbConfigSqliteFilePathEmpty"
                }
            }

            DatabaseType.MYSQL -> {
                if (host.isBlank()) {
                    return false to "dbConfigMysqlHostEmpty"
                }
                if (database.isBlank()) {
                    return false to "dbConfigMysqlDatabaseEmpty"
                }
                if (username.isBlank()) {
                    return false to "dbConfigMysqlUsernameEmpty"
                }
                if (port <= 0 || port > 65535) {
                    return false to "dbConfigMysqlPortInvalid"
                }
            }
        }

        // 验证连接池配置
        if (poolConfig.maxPoolSize <= 0) {
            return false to "dbConfigPoolMaxSizeInvalid"
        }
        if (poolConfig.minIdle < 0) {
            return false to "dbConfigPoolMinIdleInvalid"
        }
        if (poolConfig.minIdle > poolConfig.maxPoolSize) {
            return false to "dbConfigPoolMinIdleTooLarge"
        }

        return true to null
    }

    /**
     * 创建默认的SQLite配置
     */
    companion object {
        /**
         * 创建默认的SQLite配置
         *
         * @param filePath SQLite文件路径
         * @return SQLite数据库配置
         */
        fun sqlite(filePath: String = "plugins/BilibiliVideoPro/data.db"): DatabaseConfig {
            return DatabaseConfig(
                type = DatabaseType.SQLITE,
                database = "bilibili_video_pro",
                filePath = filePath
            )
        }

        /**
         * 创建MySQL配置
         *
         * @param host 主机地址
         * @param port 端口
         * @param database 数据库名
         * @param username 用户名
         * @param password 密码
         * @return MySQL数据库配置
         */
        fun mysql(
            host: String = "localhost",
            port: Int = 3306,
            database: String = "bilibili_video_pro",
            username: String,
            password: String
        ): DatabaseConfig {
            return DatabaseConfig(
                type = DatabaseType.MYSQL,
                host = host,
                port = port,
                database = database,
                username = username,
                password = password
            )
        }

        /**
         * 从配置文件加载数据库配置
         *
         * @param properties 配置属性
         * @return 数据库配置
         */
        fun fromProperties(properties: Map<String, Any>): DatabaseConfig {
            val type = when (properties["type"]?.toString()?.uppercase()) {
                "MYSQL" -> DatabaseType.MYSQL
                "SQLITE" -> DatabaseType.SQLITE
                else -> DatabaseType.SQLITE
            }

            return DatabaseConfig(
                type = type,
                host = properties["host"]?.toString() ?: "localhost",
                port = properties["port"]?.toString()?.toIntOrNull() ?: 3306,
                database = properties["database"]?.toString() ?: "bilibili_video_pro",
                username = properties["username"]?.toString() ?: "",
                password = properties["password"]?.toString() ?: "",
                filePath = properties["filePath"]?.toString() ?: "plugins/BilibiliVideoPro/data.db",
                poolConfig = PoolConfig(
                    maxPoolSize = properties["maxPoolSize"]?.toString()?.toIntOrNull() ?: 10,
                    minIdle = properties["minIdle"]?.toString()?.toIntOrNull() ?: 2,
                    maxLifetime = properties["maxLifetime"]?.toString()?.toLongOrNull() ?: 600000L,
                    connectionTimeout = properties["connectionTimeout"]?.toString()?.toLongOrNull() ?: 30000L,
                    idleTimeout = properties["idleTimeout"]?.toString()?.toLongOrNull() ?: 300000L,
                    validationTimeout = properties["validationTimeout"]?.toString()?.toLongOrNull() ?: 5000L
                )
            )
        }
    }
} 