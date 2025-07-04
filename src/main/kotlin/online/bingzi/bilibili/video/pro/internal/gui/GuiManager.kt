package online.bingzi.bilibili.video.pro.internal.gui

import org.bukkit.entity.Player
import online.bingzi.bilibili.video.pro.internal.gui.menu.MainMenuGui
import online.bingzi.bilibili.video.pro.internal.gui.menu.VideoListGui
import online.bingzi.bilibili.video.pro.internal.gui.menu.PlayerStatsGui
import online.bingzi.bilibili.video.pro.internal.gui.menu.AdminGui
import online.bingzi.bilibili.video.pro.internal.gui.config.GuiConfigManager
import online.bingzi.bilibili.video.pro.internal.gui.builder.ConfigurableGuiBuilder
import taboolib.common.platform.function.submit

/**
 * GUI管理器
 * 负责管理所有GUI界面的创建和显示
 */
object GuiManager {
    
    /**
     * 初始化GUI管理器
     */
    fun initialize() {
        submit(async = false) {
            // 验证GUI配置文件
            val errors = GuiConfigManager.validateConfig()
            if (errors.isNotEmpty()) {
                println("GUI配置验证失败:")
                errors.forEach { println("  - $it") }
            }
        }
    }
    
    /**
     * 显示主菜单GUI (配置化)
     */
    fun showMainMenu(player: Player) {
        submit(async = false) {
            val window = ConfigurableGuiBuilder.buildGui("main_menu", player)
            if (window != null) {
                window.open()
            } else {
                // fallback到原始GUI
                MainMenuGui.show(player)
            }
        }
    }
    
    /**
     * 显示视频列表GUI (配置化)
     */
    fun showVideoList(player: Player) {
        submit(async = false) {
            val window = ConfigurableGuiBuilder.buildGui("video_list", player)
            if (window != null) {
                window.open()
            } else {
                // fallback到原始GUI
                VideoListGui.show(player)
            }
        }
    }
    
    /**
     * 显示个人统计GUI (配置化)
     */
    fun showPlayerStats(player: Player) {
        submit(async = false) {
            val window = ConfigurableGuiBuilder.buildGui("player_stats", player)
            if (window != null) {
                window.open()
            } else {
                // fallback到原始GUI
                PlayerStatsGui.show(player)
            }
        }
    }
    
    /**
     * 显示管理员GUI (配置化)
     */
    fun showAdminGui(player: Player) {
        if (!player.hasPermission("bilibilipro.admin")) {
            player.sendMessage("§c您没有权限使用管理员功能！")
            return
        }
        
        submit(async = false) {
            val window = ConfigurableGuiBuilder.buildGui("admin_panel", player)
            if (window != null) {
                window.open()
            } else {
                // fallback到原始GUI
                AdminGui.show(player)
            }
        }
    }
    
    /**
     * 显示自定义GUI
     */
    fun showCustomGui(player: Player, guiName: String) {
        submit(async = false) {
            val window = ConfigurableGuiBuilder.buildGui(guiName, player)
            if (window != null) {
                window.open()
            } else {
                player.sendMessage("§c无法找到GUI配置: $guiName")
            }
        }
    }
    
    /**
     * 重载GUI配置
     */
    fun reloadGuiConfig() {
        GuiConfigManager.reload()
        
        // 验证重载后的配置
        val errors = GuiConfigManager.validateConfig()
        if (errors.isEmpty()) {
            println("GUI配置重载成功")
        } else {
            println("GUI配置重载完成，但发现问题:")
            errors.forEach { println("  - $it") }
        }
    }
    
    /**
     * 关闭所有GUI
     */
    fun closeAllGuis() {
        // InvUI 会自动处理GUI的关闭
    }
    
    /**
     * 获取当前主题信息
     */
    fun getCurrentThemeInfo(): String {
        val theme = GuiConfigManager.getCurrentTheme()
        return "当前主题: ${theme.accentColor}边框材质: ${theme.borderMaterial.name}"
    }
}