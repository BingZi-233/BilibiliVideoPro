
package online.bingzi.bilibili.video.pro.internal.database.provider

import com.zaxxer.hikari.HikariDataSource
import online.bingzi.bilibili.video.pro.internal.config.DatabaseConfig

/**
 * Database provider
 * 数据库提供者接口
 *
 * @property type 数据库类型
 * @constructor Create empty Database provider
 */
interface IDatabaseProvider {
    val type: String

    /**
     * Create data source
     * 创建数据源
     *
     * @param config 数据库配置
     * @return 数据源
     */
    fun createDataSource(config: DatabaseConfig.DatabaseDetails): HikariDataSource
}
