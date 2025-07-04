package online.bingzi.bilibili.video.pro.internal.manager

import online.bingzi.bilibili.video.pro.internal.database.DatabaseManager
import online.bingzi.bilibili.video.pro.internal.network.BilibiliNetworkManager
import online.bingzi.bilibili.video.pro.internal.gui.GuiManager
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.console
import taboolib.module.lang.sendLang

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
        
        console().sendLang("pluginInitializing")
        
        try {
            // 初始化数据库
            if (DatabaseManager.initialize()) {
                console().sendLang("pluginDatabaseSuccess")
                
                // 初始化网络管理器
                BilibiliNetworkManager.getInstance().initialize()
                console().sendLang("pluginNetworkInitialized")
                
                // 初始化GUI管理器
                GuiManager.initialize()
                
                isInitialized = true
                console().sendLang("pluginInitializationSuccess")
                
            } else {
                console().sendLang("pluginDatabaseFailed")
            }
            
        } catch (e: Exception) {
            console().sendLang("pluginInitializationFailed", e.message ?: "Unknown error")
            e.printStackTrace()
        }
    }
    
    /**
     * 插件激活时执行健康检查
     */
    @Awake(value = LifeCycle.ACTIVE)
    fun healthCheck() {
        if (!isInitialized) {
            console().sendLang("pluginInitializationNotInitialized")
            return
        }
        
        try {
            // 执行数据库健康检查
            val healthInfo = DatabaseManager.healthCheck()
            if (healthInfo.isHealthy) {
                console().sendLang("pluginDatabaseHealthy")
            } else {
                console().sendLang("pluginDatabaseUnhealthy", healthInfo.message)
            }
            
        } catch (e: Exception) {
            console().sendLang("pluginHealthCheckFailed", e.message ?: "Unknown error")
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
        
        console().sendLang("pluginShutdownStarting")
        
        try {
            // 关闭网络管理器
            BilibiliNetworkManager.destroyInstance()
            console().sendLang("pluginNetworkClosed")
            
            // 关闭数据库连接
            DatabaseManager.close()
            console().sendLang("pluginShutdownDatabaseClosed")
            
            isInitialized = false
            console().sendLang("pluginShutdownSuccess")
            
        } catch (e: Exception) {
            console().sendLang("pluginShutdownFailed", e.message ?: "Unknown error")
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
        console().sendLang("pluginReinitialize")
        cleanup()
        Thread.sleep(1000) // 等待资源完全释放
        initialize()
    }
} 