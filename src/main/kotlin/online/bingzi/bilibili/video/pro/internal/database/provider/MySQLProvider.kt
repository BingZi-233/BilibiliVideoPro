package online.bingzi.bilibili.video.pro.internal.database.provider

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import online.bingzi.bilibili.video.pro.internal.config.DatabaseConfig

/**
 * My sql provider
 * MySQL 数据库提供者
 *
 * @constructor Create empty My sql provider
 */
class MySQLProvider : IDatabaseProvider {
    override val type = "mysql"

    override fun createDataSource(config: DatabaseConfig): HikariDataSource {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.getMysqlJdbcUrl()
            username = config.mysqlUsername
            password = config.mysqlPassword
            driverClassName = "com.mysql.cj.jdbc.Driver"
            maximumPoolSize = config.mysqlMaxPoolSize
            minimumIdle = config.mysqlMinIdle
            connectionTimeout = config.mysqlConnectionTimeout
            idleTimeout = config.mysqlIdleTimeout
            maxLifetime = config.mysqlMaxLifetime
            leakDetectionThreshold = config.mysqlLeakDetectionThreshold
            poolName = "BilibiliVideoPro-MySQL-Pool"
        }
        return HikariDataSource(hikariConfig)
    }

    override fun getJdbcUrl(config: DatabaseConfig): String {
        return config.getMysqlJdbcUrl()
    }
}