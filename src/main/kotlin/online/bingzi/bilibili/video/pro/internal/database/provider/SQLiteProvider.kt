package online.bingzi.bilibili.video.pro.internal.database.provider

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import online.bingzi.bilibili.video.pro.internal.config.DatabaseConfig

/**
 * Sqlite provider
 * SQLite 数据库提供者
 *
 * @constructor Create empty Sqlite provider
 */
class SQLiteProvider : IDatabaseProvider {
    override val type = "sqlite"

    override fun createDataSource(config: DatabaseConfig): HikariDataSource {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:${config.getSqliteFilePath()}"
            driverClassName = "org.sqlite.JDBC"
            maximumPoolSize = config.sqliteMaxPoolSize
            minimumIdle = config.sqliteMinIdle
            connectionTimeout = config.sqliteConnectionTimeout
            idleTimeout = config.sqliteIdleTimeout
            maxLifetime = config.sqliteMaxLifetime
            poolName = "BilibiliVideoPro-SQLite-Pool"
        }
        return HikariDataSource(hikariConfig)
    }

    override fun getJdbcUrl(config: DatabaseConfig): String {
        return "jdbc:sqlite:${config.getSqliteFilePath()}"
    }
}