
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

    override fun createDataSource(config: DatabaseConfig.DatabaseDetails): HikariDataSource {
        val mysqlConfig = config.mysql
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:mysql://${mysqlConfig.host}:${mysqlConfig.port}/${mysqlConfig.database}?useSSL=false&autoReconnect=true"
            username = mysqlConfig.username
            password = mysqlConfig.password
            driverClassName = "com.mysql.cj.jdbc.Driver"
            maximumPoolSize = mysqlConfig.pool.maximumPoolSize
            minimumIdle = mysqlConfig.pool.minimumIdle
            poolName = "BilibiliVideoPro-MySQL-Pool"
            addDataSourceProperty("characterEncoding", "utf8")
            addDataSourceProperty("useUnicode", "true")
        }
        return HikariDataSource(hikariConfig)
    }
}
