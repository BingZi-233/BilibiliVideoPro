package online.bingzi.bilibili.video.pro.internal.manager

import online.bingzi.bilibili.video.pro.internal.database.DatabaseManager
import online.bingzi.bilibili.video.pro.internal.network.BilibiliNetworkManager
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.console
import taboolib.module.lang.sendInfo

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
        
        console().sendInfo("正在初始化 BilibiliVideoPro...")
        
        try {
            // 初始化数据库
            if (DatabaseManager.initialize()) {
                console().sendInfo("数据库初始化成功")
                
                // 初始化网络管理器
                BilibiliNetworkManager.getInstance().initialize()
                console().sendInfo("网络管理器初始化完成")
                
                isInitialized = true
                console().sendInfo("BilibiliVideoPro 初始化完成")
                
            } else {
                console().sendInfo("数据库初始化失败，插件功能可能无法正常使用")
            }
            
        } catch (e: Exception) {
            console().sendInfo("初始化过程中发生异常: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * 插件激活时执行健康检查
     */
    @Awake(value = LifeCycle.ACTIVE)
    fun healthCheck() {
        if (!isInitialized) {
            console().sendInfo("插件未正确初始化，跳过健康检查")
            return
        }
        
        try {
            // 执行数据库健康检查
            val healthInfo = DatabaseManager.healthCheck()
            if (healthInfo.isHealthy) {
                console().sendInfo("数据库连接正常")
            } else {
                console().sendInfo("数据库连接异常: ${healthInfo.message}")
            }
            
        } catch (e: Exception) {
            console().sendInfo("健康检查过程中发生异常: ${e.message}")
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
        
        console().sendInfo("正在关闭 BilibiliVideoPro...")
        
        try {
            // 关闭网络管理器
            BilibiliNetworkManager.destroyInstance()
            console().sendInfo("网络管理器已关闭")
            
            // 关闭数据库连接
            DatabaseManager.close()
            console().sendInfo("数据库连接已关闭")
            
            isInitialized = false
            console().sendInfo("BilibiliVideoPro 已完全关闭")
            
        } catch (e: Exception) {
            console().sendInfo("关闭过程中发生异常: ${e.message}")
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
        console().sendInfo("正在重新初始化插件...")
        cleanup()
        Thread.sleep(1000) // 等待资源完全释放
        initialize()
    }
} 