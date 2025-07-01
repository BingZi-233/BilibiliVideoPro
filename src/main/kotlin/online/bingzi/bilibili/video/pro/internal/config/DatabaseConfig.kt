package online.bingzi.bilibili.video.pro.internal.config

import online.bingzi.bilibili.video.pro.internal.entity.database.DatabaseType
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
    val databaseType: String = config.getString("database.type", "sqlite")!!
    
    // SQLite 配置
    val sqliteFile: String = config.getString("database.sqlite.file", "bilibili_data.db")!!
    
    val sqliteMaxPoolSize: Int = config.getInt("database.sqlite.pool.maximum_pool_size", 10)
    
    val sqliteMinIdle: Int = config.getInt("database.sqlite.pool.minimum_idle", 2)
    
    val sqliteConnectionTimeout: Long = config.getLong("database.sqlite.pool.connection_timeout", 30000)
    
    val sqliteIdleTimeout: Long = config.getLong("database.sqlite.pool.idle_timeout", 600000)
    
    val sqliteMaxLifetime: Long = config.getLong("database.sqlite.pool.max_lifetime", 1800000)
    
    // MySQL 配置
    val mysqlHost: String = config.getString("database.mysql.host", "localhost")!!
    
    val mysqlPort: Int = config.getInt("database.mysql.port", 3306)
    
    val mysqlDatabase: String = config.getString("database.mysql.database", "bilibili_video_pro")!!
    
    val mysqlUsername: String = config.getString("database.mysql.username", "bilibili_user")!!
    
    val mysqlPassword: String = config.getString("database.mysql.password", "your_password_here")!!
    
    val mysqlMaxPoolSize: Int = config.getInt("database.mysql.pool.maximum_pool_size", 20)
    
    val mysqlMinIdle: Int = config.getInt("database.mysql.pool.minimum_idle", 5)
    
    val mysqlConnectionTimeout: Long = config.getLong("database.mysql.pool.connection_timeout", 30000)
    
    val mysqlIdleTimeout: Long = config.getLong("database.mysql.pool.idle_timeout", 600000)
    
    val mysqlMaxLifetime: Long = config.getLong("database.mysql.pool.max_lifetime", 1800000)
    
    val mysqlLeakDetectionThreshold: Long = config.getLong("database.mysql.pool.leak_detection_threshold", 60000)
    
    // 通用配置
    val tablePrefix: String = config.getString("table_prefix", "bvp_")!!
    
    val autoCreateTables: Boolean = config.getBoolean("auto_create_tables", true)
    
    val enableSqlLogging: Boolean = config.getBoolean("enable_sql_logging", false)
    
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

