package online.bingzi.bilibili.video.pro

import online.bingzi.bilibili.video.pro.internal.config.PluginConfig
import online.bingzi.bilibili.video.pro.internal.database.DatabaseManager
import online.bingzi.bilibili.video.pro.internal.database.DatabaseService
import online.bingzi.bilibili.video.pro.internal.network.BilibiliApiManager
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.console
import taboolib.module.lang.sendInfo
import taboolib.module.lang.sendWarn
import java.sql.SQLException

/**
 * BilibiliVideoPro 主类
 *
 * Bilibili视频三连奖励插件的主入口
 * 提供Bilibili视频一键三连检测和奖励发放功能
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
object BilibiliVideoPro : Plugin() {

    /**
     * 插件启动时调用
     */
    override fun onEnable() {
        console().sendInfo("pluginStarting")
        console().sendInfo("pluginVersion", "1.0.0")
        console().sendInfo("pluginAuthor", "BilibiliVideoPro")

        try {
            // 初始化数据库
            initializeDatabase()

            // 初始化Bilibili API管理器
            initializeBilibiliApi()

            console().sendInfo("pluginStarted")

        } catch (e: Exception) {
            console().sendWarn("pluginStartFailed", e.message ?: "unknownError")
            console().sendInfo("pluginCheckConfig")
        }
    }

    /**
     * 插件停止时调用
     */
    override fun onDisable() {
        console().sendInfo("pluginStopping")

        try {
            // 清理Bilibili API管理器
            BilibiliApiManager.cleanup()

            // 清理数据库服务
            DatabaseService.cleanup()

            console().sendInfo("pluginStopped")

        } catch (e: Exception) {
            console().sendWarn("pluginStopError", e.message ?: "unknownError")
        }
    }

    /**
     * 初始化数据库
     */
    private fun initializeDatabase() {
        console().sendInfo("databaseInitializing")

        try {
            // 从配置文件读取数据库配置
            val databaseConfig = PluginConfig.Database.getDatabaseConfig()

            // 初始化数据库服务
            DatabaseService.initialize(databaseConfig)

            // 检查数据库连接
            if (DatabaseService.checkConnection()) {
                console().sendInfo("databaseConnectionSuccess")

                // 显示数据库统计信息
                val stats = DatabaseService.getDatabaseStats()
                if (stats != null) {
                    console().sendInfo("databaseType", stats.databaseType)
                    console().sendInfo("databaseStatsUsers", stats.userCount)
                    console().sendInfo("databaseStatsVideos", stats.videoCount)
                    console().sendInfo("databaseStatsRecords", stats.actionRecordCount)
                }
            } else {
                throw SQLException("databaseConnectionTestFailed")
            }

        } catch (e: SQLException) {
            throw RuntimeException("databaseInitFailed", e)
        }
    }

    /**
     * 初始化Bilibili API
     */
    private fun initializeBilibiliApi() {
        console().sendInfo("apiInitializing")

        try {
            // 添加Cookie配置到API管理器
            val cookies = PluginConfig.Api.getCookies()
            cookies.forEachIndexed { index, cookie ->
                BilibiliApiManager.addUserCookie("user_$index", cookie)
            }

            console().sendInfo("apiInitSuccess")

        } catch (e: Exception) {
            throw RuntimeException("apiInitFailed", e)
        }
    }

    /**
     * 获取数据库统计信息
     */
    fun getDatabaseStats(): DatabaseManager.DatabaseStats? {
        return if (DatabaseService.isInitialized()) {
            DatabaseService.getDatabaseStats()
        } else {
            null
        }
    }

    /**
     * 检查数据库连接状态
     */
    fun isDatabaseConnected(): Boolean {
        return DatabaseService.isInitialized() && DatabaseService.checkConnection()
    }
}