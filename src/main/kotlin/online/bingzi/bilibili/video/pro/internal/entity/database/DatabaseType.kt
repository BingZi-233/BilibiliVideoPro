package online.bingzi.bilibili.video.pro.internal.entity.database

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