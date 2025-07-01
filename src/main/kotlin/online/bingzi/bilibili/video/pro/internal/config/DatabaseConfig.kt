package online.bingzi.bilibili.video.pro.internal.config

import taboolib.common.platform.function.getDataFolder
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import java.io.File

/**
 * 数据库配置管理器
 * 负责读取和管理database.yml配置文件
 */
object DatabaseConfig {
    
    @Config("database.yml")
    lateinit var config: Configuration
        private set
    
    // 数据库类型
    val databaseType: String
        get() = config.getString("database.type", "sqlite")!!
    
    // SQLite 配置
    val sqliteFile: String
        get() = config.getString("database.sqlite.file", "bilibili_data.db")!!
    
    val sqliteMaxPoolSize: Int
        get() = config.getInt("database.sqlite.pool.maximum_pool_size", 10)
    
    val sqliteMinIdle: Int
        get() = config.getInt("database.sqlite.pool.minimum_idle", 2)
    
    val sqliteConnectionTimeout: Long
        get() = config.getLong("database.sqlite.pool.connection_timeout", 30000)
    
    val sqliteIdleTimeout: Long
        get() = config.getLong("database.sqlite.pool.idle_timeout", 600000)
    
    val sqliteMaxLifetime: Long
        get() = config.getLong("database.sqlite.pool.max_lifetime", 1800000)
    
    // MySQL 配置
    val mysqlHost: String
        get() = config.getString("database.mysql.host", "localhost")!!
    
    val mysqlPort: Int
        get() = config.getInt("database.mysql.port", 3306)
    
    val mysqlDatabase: String
        get() = config.getString("database.mysql.database", "bilibili_video_pro")!!
    
    val mysqlUsername: String
        get() = config.getString("database.mysql.username", "bilibili_user")!!
    
    val mysqlPassword: String
        get() = config.getString("database.mysql.password", "your_password_here")!!
    
    val mysqlMaxPoolSize: Int
        get() = config.getInt("database.mysql.pool.maximum_pool_size", 20)
    
    val mysqlMinIdle: Int
        get() = config.getInt("database.mysql.pool.minimum_idle", 5)
    
    val mysqlConnectionTimeout: Long
        get() = config.getLong("database.mysql.pool.connection_timeout", 30000)
    
    val mysqlIdleTimeout: Long
        get() = config.getLong("database.mysql.pool.idle_timeout", 600000)
    
    val mysqlMaxLifetime: Long
        get() = config.getLong("database.mysql.pool.max_lifetime", 1800000)
    
    val mysqlLeakDetectionThreshold: Long
        get() = config.getLong("database.mysql.pool.leak_detection_threshold", 60000)
    
    // 通用配置
    val tablePrefix: String
        get() = config.getString("table_prefix", "bvp_")!!
    
    val autoCreateTables: Boolean
        get() = config.getBoolean("auto_create_tables", true)
    
    val enableSqlLogging: Boolean
        get() = config.getBoolean("enable_sql_logging", false)
    
    /**
     * 获取数据库类型枚举
     */
    fun getDatabaseType(): DatabaseType {
        return when (databaseType.lowercase()) {
            "mysql" -> DatabaseType.MYSQL
            "sqlite" -> DatabaseType.SQLITE
            else -> DatabaseType.SQLITE
        }
    }
    
    /**
     * 获取SQLite数据库文件的完整路径
     */
    fun getSqliteFilePath(): String {
        val dataFolder = getDataFolder()
        return File(dataFolder, sqliteFile).absolutePath
    }
    
    /**
     * 获取MySQL JDBC URL
     */
    fun getMysqlJdbcUrl(): String {
        val properties = mapOf(
            "useSSL" to "false",
            "useUnicode" to "true",
            "characterEncoding" to "utf8mb4",
            "serverTimezone" to "Asia/Shanghai",
            "autoReconnect" to "true",
            "failOverReadOnly" to "false",
            "maxReconnects" to "3"
        )
        
        val propertiesString = properties.entries.joinToString("&") { "${it.key}=${it.value}" }
        return "jdbc:mysql://$mysqlHost:$mysqlPort/$mysqlDatabase?$propertiesString"
    }
    
    /**
     * 获取带前缀的表名
     */
    fun getTableName(baseName: String): String {
        return "$tablePrefix$baseName"
    }
}

/**
 * 数据库类型枚举
 */
enum class DatabaseType {
    SQLITE,
    MYSQL;
    
    /**
     * 获取JDBC驱动类名
     */
    fun getDriverClassName(): String {
        return when (this) {
            SQLITE -> "org.sqlite.JDBC"
            MYSQL -> "com.mysql.cj.jdbc.Driver"
        }
    }
    
    /**
     * 获取数据库方言
     */
    fun getDialect(): String {
        return when (this) {
            SQLITE -> "sqlite"
            MYSQL -> "mysql"
        }
    }
} 