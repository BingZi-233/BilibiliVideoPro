package online.bingzi.bilibili.video.pro.internal.manager

import online.bingzi.bilibili.video.pro.internal.database.DatabaseManager
import online.bingzi.bilibili.video.pro.internal.gui.GuiManager
import online.bingzi.bilibili.video.pro.internal.network.BilibiliNetworkManager
import online.bingzi.bilibili.video.pro.internal.security.SecureKeyManager
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.console
import taboolib.module.lang.sendInfo
import taboolib.module.lang.sendWarn
import taboolib.module.lang.sendError

/**
 * 插件管理器
 * 负责插件的初始化和关闭流程
 */
object PluginManager {

    private var isInitialized = false

    /**
     * 插件启用时初始化
     */
    @Awake(value = LifeCycle.ENABLE)
    fun initialize() {
        if (isInitialized) {
            return
        }

        console().sendInfo("pluginInitializing")

        try {
            // 初始化安全密钥管理器
            if (SecureKeyManager.initialize()) {
                console().sendInfo("pluginSecurityInitialized")

                // 初始化数据库
                if (DatabaseManager.initialize()) {
                    console().sendInfo("pluginDatabaseSuccess")

                    // 初始化网络管理器
                    BilibiliNetworkManager.getInstance().initialize()
                    console().sendInfo("pluginNetworkInitialized")

                    // 初始化GUI管理器
                    GuiManager.initialize()

                    isInitialized = true
                    console().sendInfo("pluginInitializationSuccess")

                } else {
                    console().sendError("pluginDatabaseFailed")
                }
            } else {
                console().sendError("pluginSecurityInitializationFailed")
            }

        } catch (e: Exception) {
            console().sendError("pluginInitializationFailed", e.message ?: "Unknown error")
            e.printStackTrace()
        }
    }

    /**
     * 插件激活时执行健康检查
     */
    @Awake(value = LifeCycle.ACTIVE)
    fun healthCheck() {
        if (!isInitialized) {
            console().sendWarn("pluginInitializationNotInitialized")
            return
        }

        try {
            // 执行密钥管理器完整性检查
            if (SecureKeyManager.verifyKeyIntegrity()) {
                console().sendInfo("pluginSecurityHealthy")
            } else {
                console().sendWarn("pluginSecurityUnhealthy")
            }

            // 执行数据库健康检查
            val healthInfo = DatabaseManager.healthCheck()
            if (healthInfo.isHealthy) {
                console().sendInfo("pluginDatabaseHealthy")
            } else {
                console().sendWarn("pluginDatabaseUnhealthy", healthInfo.message)
            }

        } catch (e: Exception) {
            console().sendError("pluginHealthCheckFailed", e.message ?: "Unknown error")
        }
    }

    /**
     * 插件禁用时清理资源
     */
    @Awake(value = LifeCycle.DISABLE)
    fun cleanup() {
        if (!isInitialized) {
            return
        }

        console().sendInfo("pluginShutdownStarting")

        try {
            // 关闭网络管理器
            BilibiliNetworkManager.destroyInstance()
            console().sendInfo("pluginNetworkClosed")

            // 关闭数据库连接
            DatabaseManager.close()
            console().sendInfo("pluginShutdownDatabaseClosed")

            // 清理安全密钥管理器
            SecureKeyManager.cleanup()
            console().sendInfo("pluginSecurityCleaned")

            isInitialized = false
            console().sendInfo("pluginShutdownSuccess")

        } catch (e: Exception) {
            console().sendError("pluginShutdownFailed", e.message ?: "Unknown error")
        }
    }

    /**
     * 获取初始化状态
     */
    fun isInitialized(): Boolean {
        return isInitialized
    }

    /**
     * 手动重新初始化（用于调试或命令）
     */
    fun reinitialize() {
        console().sendInfo("pluginReinitialize")
        cleanup()
        Thread.sleep(1000) // 等待资源完全释放
        initialize()
    }
} 