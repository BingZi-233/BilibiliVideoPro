
package online.bingzi.bilibili.video.pro.internal.database.provider

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import online.bingzi.bilibili.video.pro.internal.config.DatabaseConfig
import taboolib.common.platform.function.getDataFolder
import java.io.File

/**
 * Sqlite provider
 * SQLite 数据库提供者
 *
 * @constructor Create empty Sqlite provider
 */
class SQLiteProvider : IDatabaseProvider {
    override val type = "sqlite"

    override fun createDataSource(config: DatabaseConfig.DatabaseDetails): HikariDataSource {
        val sqliteConfig = config.sqlite
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:${File(getDataFolder(), sqliteConfig.file)}"
            driverClassName = "org.sqlite.JDBC"
            maximumPoolSize = sqliteConfig.pool.maximumPoolSize
            minimumIdle = sqliteConfig.pool.minimumIdle
            poolName = "BilibiliVideoPro-SQLite-Pool"
            addDataSourceProperty("characterEncoding", "utf8")
            addDataSourceProperty("useUnicode", "true")
        }
        return HikariDataSource(hikariConfig)
    }
}
