package online.bingzi.bilibili.video.pro.internal.database

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import com.zaxxer.hikari.HikariDataSource
import online.bingzi.bilibili.video.pro.internal.config.DatabaseConfig
import online.bingzi.bilibili.video.pro.internal.database.entity.PlayerBilibili
import online.bingzi.bilibili.video.pro.internal.database.entity.VideoInteractionRecord
import online.bingzi.bilibili.video.pro.internal.database.provider.IDatabaseProvider
import online.bingzi.bilibili.video.pro.internal.database.provider.MySQLProvider
import online.bingzi.bilibili.video.pro.internal.database.provider.SQLiteProvider
import taboolib.common.platform.function.console
import taboolib.module.lang.sendInfo
import java.sql.SQLException

/**
 * 数据库管理器
 * 负责数据库连接、表创建和DAO管理
 */
object DatabaseManager {

    private var connectionSource: ConnectionSource? = null
    private var dataSource: HikariDataSource? = null

    // DAO实例
    lateinit var playerBilibiliDao: Dao<PlayerBilibili, Long>
    lateinit var videoInteractionRecordDao: Dao<VideoInteractionRecord, Long>

    private val providers = mutableMapOf<String, IDatabaseProvider>()

    init {
        // 自动注册内置的提供者
        registerProvider(SQLiteProvider())
        registerProvider(MySQLProvider())
    }

    /**
     * 注册数据库提供者
     *
     * @param provider 数据库提供者
     */
    fun registerProvider(provider: IDatabaseProvider) {
        providers[provider.type.lowercase()] = provider
        console().sendInfo("已注册数据库提供者: ${provider.type}")
    }

    /**
     * 初始化数据库连接
     */
    fun initialize(): Boolean {
        return try {
            console().sendInfo("正在初始化数据库连接...")

            val dbType = DatabaseConfig.databaseType.lowercase()
            console().sendInfo("数据库类型: $dbType")

            val provider = providers[dbType]
                ?: throw IllegalStateException("不支持的数据库类型: $dbType, 请确保已注册对应的IDatabaseProvider")

            dataSource = provider.createDataSource(DatabaseConfig)
            connectionSource = JdbcConnectionSource(provider.getJdbcUrl(DatabaseConfig))

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
            console().sendInfo("确保表存在: ${PlayerBilibili.TABLE_NAME}")

            // 创建视频互动记录表
            TableUtils.createTableIfNotExists(connection, VideoInteractionRecord::class.java)
            console().sendInfo("确保表存在: ${VideoInteractionRecord.TABLE_NAME}")

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
        var poolStatus = "连接池未初始化" // Default value
        return try {
            val isConnected = isConnectionValid()
            poolStatus = getPoolStatus() // Assign here
            val dbType = DatabaseConfig.databaseType

            if (isConnected) {
                // 尝试执行一个简单的查询来验证连接
                val playerDao = playerBilibiliDao
                playerDao.queryBuilder().limit(1).query()

                DatabaseHealthInfo(
                    isHealthy = true,
                    message = "数据库连接正常",
                    databaseType = dbType,
                    poolStatus = poolStatus
                )
            } else {
                DatabaseHealthInfo(
                    isHealthy = false,
                    message = "数据库连接无效",
                    databaseType = dbType,
                    poolStatus = poolStatus
                )
            }
        } catch (e: Exception) {
            DatabaseHealthInfo(
                isHealthy = false,
                message = "数据库健康检查失败: ${e.message}",
                databaseType = DatabaseConfig.databaseType,
                poolStatus = poolStatus
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