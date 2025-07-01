package online.bingzi.bilibili.video.pro.internal.database

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import online.bingzi.bilibili.video.pro.internal.config.DatabaseConfig
import online.bingzi.bilibili.video.pro.internal.config.DatabaseType
import online.bingzi.bilibili.video.pro.internal.database.entity.PlayerBilibili
import online.bingzi.bilibili.video.pro.internal.database.entity.VideoInteractionRecord
import taboolib.common.platform.function.console
import taboolib.module.lang.sendInfo
import java.io.File
import java.sql.SQLException

/**
 * 数据库管理器
 * 负责数据库连接、表创建和DAO管理
 */
object DatabaseManager {
    
    private var connectionSource: ConnectionSource? = null
    private var dataSource: HikariDataSource? = null
    
    // DAO实例
    private var playerBilibiliDao: Dao<PlayerBilibili, Long>? = null
    private var videoInteractionRecordDao: Dao<VideoInteractionRecord, Long>? = null
    
    /**
     * 初始化数据库连接
     */
    fun initialize(): Boolean {
        return try {
            console().sendInfo("正在初始化数据库连接...")
            
            val dbType = DatabaseConfig.getDatabaseType()
            console().sendInfo("数据库类型: ${dbType.name}")
            
            when (dbType) {
                DatabaseType.SQLITE -> initializeSQLite()
                DatabaseType.MYSQL -> initializeMySQL()
            }
            
            createTables()
            initializeDAOs()
            
            console().sendInfo("数据库初始化完成")
            true
            
        } catch (e: Exception) {
            console().sendInfo("数据库初始化失败: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 初始化SQLite连接
     */
    private fun initializeSQLite() {
        val dbPath = DatabaseConfig.getSqliteFilePath()
        console().sendInfo("SQLite数据库路径: $dbPath")
        
        // 确保数据库文件目录存在
        val dbFile = File(dbPath)
        dbFile.parentFile?.mkdirs()
        
        // 创建HikariCP数据源
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:$dbPath"
            driverClassName = DatabaseType.SQLITE.getDriverClassName()
            maximumPoolSize = DatabaseConfig.sqliteMaxPoolSize
            minimumIdle = DatabaseConfig.sqliteMinIdle
            connectionTimeout = DatabaseConfig.sqliteConnectionTimeout
            idleTimeout = DatabaseConfig.sqliteIdleTimeout
            maxLifetime = DatabaseConfig.sqliteMaxLifetime
            
            // SQLite特定配置
            addDataSourceProperty("foreign_keys", "true")
            addDataSourceProperty("journal_mode", "WAL")
            addDataSourceProperty("synchronous", "NORMAL")
            addDataSourceProperty("cache_size", "10000")
            
            if (DatabaseConfig.enableSqlLogging) {
                addDataSourceProperty("trace_callback", "true")
            }
        }
        
        dataSource = HikariDataSource(hikariConfig)
        connectionSource = JdbcConnectionSource("jdbc:sqlite:$dbPath")
    }
    
    /**
     * 初始化MySQL连接
     */
    private fun initializeMySQL() {
        val jdbcUrl = DatabaseConfig.getMysqlJdbcUrl()
        console().sendInfo("MySQL JDBC URL: $jdbcUrl")
        
        // 创建HikariCP数据源
        val hikariConfig = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            driverClassName = DatabaseType.MYSQL.getDriverClassName()
            username = DatabaseConfig.mysqlUsername
            password = DatabaseConfig.mysqlPassword
            maximumPoolSize = DatabaseConfig.mysqlMaxPoolSize
            minimumIdle = DatabaseConfig.mysqlMinIdle
            connectionTimeout = DatabaseConfig.mysqlConnectionTimeout
            idleTimeout = DatabaseConfig.mysqlIdleTimeout
            maxLifetime = DatabaseConfig.mysqlMaxLifetime
            leakDetectionThreshold = DatabaseConfig.mysqlLeakDetectionThreshold
            
            // MySQL特定配置
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            addDataSourceProperty("useServerPrepStmts", "true")
            addDataSourceProperty("useLocalSessionState", "true")
            addDataSourceProperty("rewriteBatchedStatements", "true")
            addDataSourceProperty("cacheResultSetMetadata", "true")
            addDataSourceProperty("cacheServerConfiguration", "true")
            addDataSourceProperty("elideSetAutoCommits", "true")
            addDataSourceProperty("maintainTimeStats", "false")
            
            if (DatabaseConfig.enableSqlLogging) {
                addDataSourceProperty("profileSQL", "true")
                addDataSourceProperty("logSlowQueries", "true")
                addDataSourceProperty("slowQueryThresholdMillis", "1000")
            }
        }
        
        dataSource = HikariDataSource(hikariConfig)
        connectionSource = JdbcConnectionSource(jdbcUrl, DatabaseConfig.mysqlUsername, DatabaseConfig.mysqlPassword)
    }
    
    /**
     * 创建数据库表
     */
    private fun createTables() {
        if (!DatabaseConfig.autoCreateTables) {
            console().sendInfo("跳过自动创建表（配置已禁用）")
            return
        }
        
        console().sendInfo("正在创建数据库表...")
        
        val connection = connectionSource ?: throw SQLException("数据库连接未初始化")
        
        try {
            // 创建玩家Bilibili绑定表
            TableUtils.createTableIfNotExists(connection, PlayerBilibili::class.java)
            console().sendInfo("确保表存在: ${PlayerBilibili.getFullTableName()}")
            
            // 创建视频互动记录表
            TableUtils.createTableIfNotExists(connection, VideoInteractionRecord::class.java)
            console().sendInfo("确保表存在: ${VideoInteractionRecord.getFullTableName()}")
            
            console().sendInfo("数据库表创建完成")
            
        } catch (e: SQLException) {
            console().sendInfo("创建数据库表失败: ${e.message}")
            throw e
        }
    }
    
    /**
     * 初始化DAO
     */
    private fun initializeDAOs() {
        console().sendInfo("正在初始化DAO...")
        
        val connection = connectionSource ?: throw SQLException("数据库连接未初始化")
        
        try {
            playerBilibiliDao = DaoManager.createDao(connection, PlayerBilibili::class.java)
            videoInteractionRecordDao = DaoManager.createDao(connection, VideoInteractionRecord::class.java)
            
            console().sendInfo("DAO初始化完成")
            
        } catch (e: SQLException) {
            console().sendInfo("DAO初始化失败: ${e.message}")
            throw e
        }
    }
    
    /**
     * 获取玩家Bilibili绑定DAO
     */
    fun getPlayerBilibiliDao(): Dao<PlayerBilibili, Long> {
        return playerBilibiliDao ?: throw IllegalStateException("PlayerBilibili DAO未初始化")
    }
    
    /**
     * 获取视频互动记录DAO
     */
    fun getVideoInteractionRecordDao(): Dao<VideoInteractionRecord, Long> {
        return videoInteractionRecordDao ?: throw IllegalStateException("VideoInteractionRecord DAO未初始化")
    }
    
    /**
     * 获取数据库连接源
     */
    fun getConnectionSource(): ConnectionSource {
        return connectionSource ?: throw IllegalStateException("数据库连接未初始化")
    }
    
    /**
     * 检查数据库连接是否有效
     */
    fun isConnectionValid(): Boolean {
        return try {
            connectionSource != null && dataSource != null && !dataSource!!.isClosed
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取数据库连接池状态信息
     */
    fun getPoolStatus(): String {
        return if (dataSource != null) {
            val ds = dataSource!!
            "连接池状态 - 活跃连接: ${ds.hikariPoolMXBean?.activeConnections ?: 0}, " +
                    "空闲连接: ${ds.hikariPoolMXBean?.idleConnections ?: 0}, " +
                    "总连接: ${ds.hikariPoolMXBean?.totalConnections ?: 0}, " +
                    "等待线程: ${ds.hikariPoolMXBean?.threadsAwaitingConnection ?: 0}"
        } else {
            "连接池未初始化"
        }
    }
    
    /**
     * 执行数据库健康检查
     */
    fun healthCheck(): DatabaseHealthInfo {
        return try {
            val isConnected = isConnectionValid()
            val poolStatus = getPoolStatus()
            val dbType = DatabaseConfig.getDatabaseType()
            
            if (isConnected) {
                // 尝试执行一个简单的查询来验证连接
                val playerDao = getPlayerBilibiliDao()
                playerDao.queryBuilder().limit(1).query()
                
                DatabaseHealthInfo(
                    isHealthy = true,
                    message = "数据库连接正常",
                    databaseType = dbType.name,
                    poolStatus = poolStatus
                )
            } else {
                DatabaseHealthInfo(
                    isHealthy = false,
                    message = "数据库连接无效",
                    databaseType = dbType.name,
                    poolStatus = poolStatus
                )
            }
        } catch (e: Exception) {
            DatabaseHealthInfo(
                isHealthy = false,
                message = "数据库健康检查失败: ${e.message}",
                databaseType = DatabaseConfig.getDatabaseType().name,
                poolStatus = getPoolStatus()
            )
        }
    }
    
    /**
     * 关闭数据库连接
     */
    fun close() {
        try {
            console().sendInfo("正在关闭数据库连接...")
            
            connectionSource?.close()
            dataSource?.close()
            
            connectionSource = null
            dataSource = null
            playerBilibiliDao = null
            videoInteractionRecordDao = null
            
            console().sendInfo("数据库连接已关闭")
            
        } catch (e: Exception) {
            console().sendInfo("关闭数据库连接时出错: ${e.message}")
        }
    }
}

/**
 * 数据库健康状态信息
 */
data class DatabaseHealthInfo(
    val isHealthy: Boolean,
    val message: String,
    val databaseType: String,
    val poolStatus: String
) 