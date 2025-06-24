package online.bingzi.bilibili.video.pro.internal.database

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import online.bingzi.bilibili.video.pro.internal.config.DatabaseConfig
import online.bingzi.bilibili.video.pro.internal.database.entity.BilibiliUserEntity
import online.bingzi.bilibili.video.pro.internal.database.entity.BilibiliVideoEntity
import online.bingzi.bilibili.video.pro.internal.database.entity.TripleActionRecordEntity
import taboolib.common.platform.function.console
import taboolib.module.lang.sendInfo
import taboolib.module.lang.sendWarn
import java.io.File
import java.sql.SQLException
import javax.sql.DataSource

/**
 * 数据库管理器
 *
 * 负责数据库连接管理、表初始化、DAO创建等核心功能
 * 支持SQLite和MySQL两种数据源，使用HikariCP连接池
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
class DatabaseManager(private val config: DatabaseConfig) {

    /**
     * 数据源实例
     */
    private var dataSource: DataSource? = null

    /**
     * ORMLite连接源
     */
    private var connectionSource: ConnectionSource? = null

    /**
     * DAO缓存
     */
    private val daoCache = mutableMapOf<Class<*>, Dao<*, *>>()

    /**
     * 是否已初始化
     */
    private var initialized = false

    /**
     * 数据库实体类列表
     */
    private val entityClasses = listOf(
        BilibiliUserEntity::class.java,
        BilibiliVideoEntity::class.java,
        TripleActionRecordEntity::class.java
    )

    /**
     * 初始化数据库
     *
     * @throws SQLException 数据库初始化失败
     */
    @Throws(SQLException::class)
    fun initialize() {
        if (initialized) {
            console().sendInfo("databaseAlreadyInitialized")
            return
        }

        try {
            // 验证配置
            val (isValid, errorMessage) = config.validate()
            if (!isValid) {
                console().sendWarn("databaseConfigInvalid", errorMessage ?: "unknownError")
                throw SQLException("数据库配置无效: $errorMessage")
            }

            console().sendInfo("databaseInitializing")
            console().sendInfo("databaseType", config.type.toString())

            // 创建数据源
            dataSource = createDataSource()

            // 创建ORMLite连接源
            connectionSource = when (config.type) {
                DatabaseConfig.DatabaseType.SQLITE -> {
                    // 确保SQLite文件目录存在
                    ensureSqliteDirectory()
                    JdbcConnectionSource(config.getJdbcUrl())
                }

                DatabaseConfig.DatabaseType.MYSQL -> {
                    JdbcConnectionSource(config.getJdbcUrl(), config.username, config.password)
                }
            }

            // 初始化数据库表
            initializeTables()

            console().sendInfo("databaseInitialized")
            initialized = true

        } catch (e: Exception) {
            console().sendWarn("databaseInitFailed", e.message ?: "unknownError")
            cleanup()
            throw SQLException("数据库初始化失败", e)
        }
    }

    /**
     * 创建数据源
     *
     * @return 数据源实例
     */
    private fun createDataSource(): DataSource {
        return when (config.type) {
            DatabaseConfig.DatabaseType.SQLITE -> {
                // SQLite不需要连接池，直接返回一个简单的数据源
                createSqliteDataSource()
            }

            DatabaseConfig.DatabaseType.MYSQL -> {
                // MySQL使用HikariCP连接池
                createMysqlDataSource()
            }
        }
    }

    /**
     * 创建SQLite数据源
     *
     * @return SQLite数据源
     */
    private fun createSqliteDataSource(): DataSource {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.getJdbcUrl()
            driverClassName = config.getDriverClassName()
            maximumPoolSize = 1 // SQLite只支持单连接
            minimumIdle = 1
            connectionTimeout = config.poolConfig.connectionTimeout
            validationTimeout = config.poolConfig.validationTimeout
            poolName = "BilibiliVideoPro-SQLite-Pool"
        }

        return HikariDataSource(hikariConfig)
    }

    /**
     * 创建MySQL数据源
     *
     * @return MySQL数据源
     */
    private fun createMysqlDataSource(): DataSource {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.getJdbcUrl()
            driverClassName = config.getDriverClassName()
            username = config.username
            password = config.password
            maximumPoolSize = config.poolConfig.maxPoolSize
            minimumIdle = config.poolConfig.minIdle
            maxLifetime = config.poolConfig.maxLifetime
            connectionTimeout = config.poolConfig.connectionTimeout
            idleTimeout = config.poolConfig.idleTimeout
            validationTimeout = config.poolConfig.validationTimeout
            poolName = "BilibiliVideoPro-MySQL-Pool"

            // MySQL优化配置
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
        }

        return HikariDataSource(hikariConfig)
    }

    /**
     * 确保SQLite文件目录存在
     */
    private fun ensureSqliteDirectory() {
        val file = File(config.filePath)
        val parentDir = file.parentFile
        if (parentDir != null && !parentDir.exists()) {
            if (parentDir.mkdirs()) {
                console().sendInfo("sqliteDirectoryCreated", parentDir.absolutePath)
            } else {
                console().sendWarn("sqliteDirectoryCreateFailed", parentDir.absolutePath)
                throw SQLException("无法创建SQLite数据库目录: ${parentDir.absolutePath}")
            }
        }
    }

    /**
     * 初始化数据库表
     *
     * @throws SQLException 表初始化失败
     */
    @Throws(SQLException::class)
    private fun initializeTables() {
        val connection = connectionSource ?: throw SQLException("连接源未初始化")

        console().sendInfo("databaseTablesCreating")

        try {
            entityClasses.forEach { entityClass ->
                try {
                    // 创建表（如果不存在）
                    TableUtils.createTableIfNotExists(connection, entityClass)
                    console().sendInfo("databaseTableCreated", getTableName(entityClass))
                } catch (e: SQLException) {
                    console().sendWarn(
                        "databaseTableCreateFailed",
                        getTableName(entityClass),
                        e.message ?: "unknownError"
                    )
                    throw e
                }
            }

            console().sendInfo("databaseTablesCreated")

        } catch (e: SQLException) {
            console().sendWarn("databaseTablesCreateFailed", e.message ?: "unknownError")
            throw e
        }
    }

    /**
     * 获取表名
     *
     * @param entityClass 实体类
     * @return 表名
     */
    private fun getTableName(entityClass: Class<*>): String {
        return when (entityClass) {
            BilibiliUserEntity::class.java -> "bilibili_users"
            BilibiliVideoEntity::class.java -> "bilibili_videos"
            TripleActionRecordEntity::class.java -> "triple_action_records"
            else -> entityClass.simpleName
        }
    }

    /**
     * 获取DAO实例
     *
     * @param T 实体类型
     * @param ID 主键类型
     * @param entityClass 实体类
     * @return DAO实例
     * @throws SQLException DAO创建失败
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(SQLException::class)
    fun <T, ID> getDao(entityClass: Class<T>): Dao<T, ID> {
        if (!initialized) {
            throw SQLException("数据库未初始化")
        }

        // 从缓存中获取DAO
        return daoCache.getOrPut(entityClass) {
            val connection = connectionSource ?: throw SQLException("连接源未初始化")
            DaoManager.createDao(connection, entityClass)
        } as Dao<T, ID>
    }

    /**
     * 获取用户DAO
     *
     * @return 用户DAO实例
     */
    fun getUserDao(): Dao<BilibiliUserEntity, Long> {
        return getDao(BilibiliUserEntity::class.java)
    }

    /**
     * 获取视频DAO
     *
     * @return 视频DAO实例
     */
    fun getVideoDao(): Dao<BilibiliVideoEntity, Long> {
        return getDao(BilibiliVideoEntity::class.java)
    }

    /**
     * 获取三连操作记录DAO
     *
     * @return 三连操作记录DAO实例
     */
    fun getTripleActionDao(): Dao<TripleActionRecordEntity, Long> {
        return getDao(TripleActionRecordEntity::class.java)
    }

    /**
     * 检查数据库连接状态
     *
     * @return true表示连接正常
     */
    fun checkConnection(): Boolean {
        return try {
            val cs = connectionSource ?: return false
            val connection = cs.getReadOnlyConnection(null)

            try {
                connection.queryForLong("SELECT 1")
                true
            } finally {
                cs.releaseConnection(connection)
            }
        } catch (e: SQLException) {
            console().sendMessage("数据库连接检查失败: ${e.message}")
            false
        }
    }

    /**
     * 获取数据库统计信息
     *
     * @return 统计信息
     */
    fun getDatabaseStats(): DatabaseStats {
        return try {
            val userCount = getUserDao().countOf()
            val videoCount = getVideoDao().countOf()
            val actionCount = getTripleActionDao().countOf()

            val poolStats = when (dataSource) {
                is HikariDataSource -> {
                    val hikari = dataSource as HikariDataSource
                    PoolStats(
                        activeConnections = hikari.hikariPoolMXBean?.activeConnections ?: 0,
                        idleConnections = hikari.hikariPoolMXBean?.idleConnections ?: 0,
                        totalConnections = hikari.hikariPoolMXBean?.totalConnections ?: 0,
                        threadsAwaitingConnection = hikari.hikariPoolMXBean?.threadsAwaitingConnection ?: 0
                    )
                }

                else -> PoolStats()
            }

            DatabaseStats(
                databaseType = config.type,
                isConnected = checkConnection(),
                userCount = userCount,
                videoCount = videoCount,
                actionRecordCount = actionCount,
                poolStats = poolStats
            )
        } catch (e: SQLException) {
            console().sendMessage("获取数据库统计信息失败: ${e.message}")
            DatabaseStats(
                databaseType = config.type,
                isConnected = false,
                userCount = 0,
                videoCount = 0,
                actionRecordCount = 0
            )
        }
    }

    /**
     * 执行数据库备份（仅SQLite）
     *
     * @param backupPath 备份文件路径
     * @return 备份是否成功
     */
    fun backup(backupPath: String): Boolean {
        if (config.type != DatabaseConfig.DatabaseType.SQLITE) {
            console().sendMessage("只有SQLite数据库支持备份功能")
            return false
        }

        return try {
            val sourceFile = File(config.filePath)
            val backupFile = File(backupPath)

            // 确保备份目录存在
            backupFile.parentFile?.mkdirs()

            // 复制文件
            sourceFile.copyTo(backupFile, overwrite = true)

            console().sendMessage("数据库备份成功: $backupPath")
            true
        } catch (e: Exception) {
            console().sendMessage("数据库备份失败: ${e.message}")
            false
        }
    }

    /**
     * 清理资源
     */
    fun cleanup() {
        try {
            // 关闭连接源
            connectionSource?.close()

            // 关闭数据源
            when (dataSource) {
                is HikariDataSource -> {
                    (dataSource as HikariDataSource).close()
                }
            }

            // 清理缓存
            daoCache.clear()

            initialized = false
            console().sendMessage("数据库资源清理完成")

        } catch (e: Exception) {
            console().sendMessage("数据库资源清理失败: ${e.message}")
        }
    }

    /**
     * 获取数据库配置
     *
     * @return 数据库配置
     */
    fun getConfig(): DatabaseConfig = config

    /**
     * 是否已初始化
     *
     * @return 初始化状态
     */
    fun isInitialized(): Boolean = initialized

    /**
     * 数据库统计信息
     *
     * @property databaseType 数据库类型
     * @property isConnected 是否连接
     * @property userCount 用户数量
     * @property videoCount 视频数量
     * @property actionRecordCount 操作记录数量
     * @property poolStats 连接池统计
     */
    data class DatabaseStats(
        val databaseType: DatabaseConfig.DatabaseType,
        val isConnected: Boolean,
        val userCount: Long,
        val videoCount: Long,
        val actionRecordCount: Long,
        val poolStats: PoolStats = PoolStats()
    )

    /**
     * 连接池统计信息
     *
     * @property activeConnections 活跃连接数
     * @property idleConnections 空闲连接数
     * @property totalConnections 总连接数
     * @property threadsAwaitingConnection 等待连接的线程数
     */
    data class PoolStats(
        val activeConnections: Int = 0,
        val idleConnections: Int = 0,
        val totalConnections: Int = 0,
        val threadsAwaitingConnection: Int = 0
    )
} 