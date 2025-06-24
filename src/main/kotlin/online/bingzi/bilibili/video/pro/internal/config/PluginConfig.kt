package online.bingzi.bilibili.video.pro.internal.config

import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

/**
 * 插件配置管理器
 *
 * 集中管理所有配置项，提供类型安全的配置访问接口
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
object PluginConfig {

    /**
     * 主配置文件
     */
    @Config("config.yml")
    lateinit var config: Configuration

    /**
     * 数据库配置
     */
    object Database {
        /**
         * 获取数据库配置
         */
        fun getDatabaseConfig(): DatabaseConfig {
            val databaseSection = config.getConfigurationSection("database")

            return if (databaseSection != null) {
                val properties = mutableMapOf<String, Any>()

                // 读取数据库类型
                properties["type"] = databaseSection.getString("type") ?: "sqlite"

                // 读取SQLite配置
                properties["filePath"] = databaseSection.getString("sqlite.file") ?: "plugins/BilibiliVideoPro/data.db"

                // 读取MySQL配置
                properties["host"] = databaseSection.getString("mysql.host") ?: "localhost"
                properties["port"] = databaseSection.getInt("mysql.port", 3306)
                properties["database"] = databaseSection.getString("mysql.database") ?: "bilibili_video_pro"
                properties["username"] = databaseSection.getString("mysql.username") ?: ""
                properties["password"] = databaseSection.getString("mysql.password") ?: ""

                // 读取连接池配置
                properties["maxPoolSize"] = databaseSection.getInt("pool.maxPoolSize", 10)
                properties["minIdle"] = databaseSection.getInt("pool.minIdle", 2)
                properties["maxLifetime"] = databaseSection.getLong("pool.maxLifetime", 600000L)
                properties["connectionTimeout"] = databaseSection.getLong("pool.connectionTimeout", 30000L)
                properties["idleTimeout"] = databaseSection.getLong("pool.idleTimeout", 300000L)
                properties["validationTimeout"] = databaseSection.getLong("pool.validationTimeout", 5000L)

                DatabaseConfig.fromProperties(properties)
            } else {
                // 使用默认SQLite配置
                DatabaseConfig.sqlite()
            }
        }
    }

    /**
     * API配置
     */
    object Api {
        /**
         * 获取API配置
         */
        fun getApiConfig(): Map<String, Any> {
            val apiSection = config.getConfigurationSection("api")
            val apiConfig = mutableMapOf<String, Any>()

            if (apiSection != null) {
                apiConfig["cookies"] = apiSection.getStringList("cookies")
                apiConfig["timeout"] = apiSection.getInt("timeout", 30)
                apiConfig["maxRetries"] = apiSection.getInt("maxRetries", 3)
                apiConfig["enableLogging"] = apiSection.getBoolean("enableLogging", false)
                apiConfig["requestInterval"] = apiSection.getLong("requestInterval", 1000L)
            }

            return apiConfig
        }

        /**
         * 获取Cookie列表
         */
        fun getCookies(): List<String> {
            return config.getConfigurationSection("api")
                ?.getStringList("cookies") ?: emptyList()
        }

        /**
         * 获取请求超时时间
         */
        fun getTimeout(): Int {
            return config.getConfigurationSection("api")
                ?.getInt("timeout", 30) ?: 30
        }

        /**
         * 获取最大重试次数
         */
        fun getMaxRetries(): Int {
            return config.getConfigurationSection("api")
                ?.getInt("maxRetries", 3) ?: 3
        }

        /**
         * 是否启用日志
         */
        fun isLoggingEnabled(): Boolean {
            return config.getConfigurationSection("api")
                ?.getBoolean("enableLogging", false) ?: false
        }

        /**
         * 获取请求间隔
         */
        fun getRequestInterval(): Long {
            return config.getConfigurationSection("api")
                ?.getLong("requestInterval", 1000L) ?: 1000L
        }
    }

    /**
     * 奖励配置
     */
    object Rewards {
        /**
         * 奖励是否启用
         */
        fun isEnabled(): Boolean {
            return config.getConfigurationSection("rewards")
                ?.getBoolean("enabled", true) ?: true
        }

        /**
         * 获取绑定奖励配置
         */
        fun getBindReward(): RewardConfig {
            val section = config.getConfigurationSection("rewards.bind")
            return RewardConfig(
                enabled = section?.getBoolean("enabled", true) ?: true,
                commands = section?.getStringList("commands") ?: emptyList()
            )
        }

        /**
         * 获取点赞奖励配置
         */
        fun getLikeReward(): RewardConfig {
            val section = config.getConfigurationSection("rewards.like")
            return RewardConfig(
                enabled = section?.getBoolean("enabled", true) ?: true,
                commands = section?.getStringList("commands") ?: emptyList()
            )
        }

        /**
         * 获取投币奖励配置
         */
        fun getCoinReward(): RewardConfig {
            val section = config.getConfigurationSection("rewards.coin")
            return RewardConfig(
                enabled = section?.getBoolean("enabled", true) ?: true,
                commands = section?.getStringList("commands") ?: emptyList()
            )
        }

        /**
         * 获取收藏奖励配置
         */
        fun getFavoriteReward(): RewardConfig {
            val section = config.getConfigurationSection("rewards.favorite")
            return RewardConfig(
                enabled = section?.getBoolean("enabled", true) ?: true,
                commands = section?.getStringList("commands") ?: emptyList()
            )
        }

        /**
         * 获取完整三连奖励配置
         */
        fun getFullTripleReward(): RewardConfig {
            val section = config.getConfigurationSection("rewards.fullTriple")
            return RewardConfig(
                enabled = section?.getBoolean("enabled", true) ?: true,
                commands = section?.getStringList("commands") ?: emptyList()
            )
        }

        /**
         * 奖励配置数据类
         */
        data class RewardConfig(
            val enabled: Boolean,
            val commands: List<String>
        )
    }

    /**
     * 检测配置
     */
    object Detection {
        /**
         * 检测是否启用
         */
        fun isEnabled(): Boolean {
            return config.getConfigurationSection("detection")
                ?.getBoolean("enabled", true) ?: true
        }

        /**
         * 获取检测间隔（秒）
         */
        fun getInterval(): Int {
            return config.getConfigurationSection("detection")
                ?.getInt("interval", 300) ?: 300
        }

        /**
         * 获取目标UP主列表
         */
        fun getTargetUploaders(): List<String> {
            return config.getConfigurationSection("detection")
                ?.getStringList("targetUploaders") ?: emptyList()
        }

        /**
         * 是否只检测最新视频
         */
        fun isOnlyLatestVideos(): Boolean {
            return config.getConfigurationSection("detection")
                ?.getBoolean("onlyLatestVideos", true) ?: true
        }

        /**
         * 获取最新视频时间范围（小时）
         */
        fun getLatestVideoHours(): Int {
            return config.getConfigurationSection("detection")
                ?.getInt("latestVideoHours", 24) ?: 24
        }
    }

    /**
     * 消息配置
     */
    object Messages {
        /**
         * 获取语言设置
         */
        fun getLanguage(): String {
            return config.getConfigurationSection("messages")
                ?.getString("language", "zh_CN") ?: "zh_CN"
        }

        /**
         * 广播是否启用
         */
        fun isBroadcastEnabled(): Boolean {
            return config.getConfigurationSection("messages.broadcast")
                ?.getBoolean("enabled", true) ?: true
        }

        /**
         * 获取三连成功广播消息
         */
        fun getTripleSuccessBroadcast(): String {
            return config.getConfigurationSection("messages.broadcast")
                ?.getString("tripleSuccess", "§6{player} §a对视频 §e{title} §a完成了三连操作！")
                ?: "§6{player} §a对视频 §e{title} §a完成了三连操作！"
        }
    }

    /**
     * 高级配置
     */
    object Advanced {
        /**
         * 获取数据同步间隔（分钟）
         */
        fun getSyncInterval(): Int {
            return config.getConfigurationSection("advanced")
                ?.getInt("syncInterval", 30) ?: 30
        }

        /**
         * 自动备份是否启用
         */
        fun isAutoBackupEnabled(): Boolean {
            return config.getConfigurationSection("advanced.autoBackup")
                ?.getBoolean("enabled", true) ?: true
        }

        /**
         * 获取备份间隔（小时）
         */
        fun getBackupInterval(): Int {
            return config.getConfigurationSection("advanced.autoBackup")
                ?.getInt("interval", 24) ?: 24
        }

        /**
         * 获取备份保留数量
         */
        fun getBackupKeepCount(): Int {
            return config.getConfigurationSection("advanced.autoBackup")
                ?.getInt("keepCount", 7) ?: 7
        }

        /**
         * 缓存是否启用
         */
        fun isCacheEnabled(): Boolean {
            return config.getConfigurationSection("advanced.performance")
                ?.getBoolean("cacheEnabled", true) ?: true
        }

        /**
         * 获取缓存过期时间（分钟）
         */
        fun getCacheExpiration(): Int {
            return config.getConfigurationSection("advanced.performance")
                ?.getInt("cacheExpiration", 10) ?: 10
        }
    }
} 