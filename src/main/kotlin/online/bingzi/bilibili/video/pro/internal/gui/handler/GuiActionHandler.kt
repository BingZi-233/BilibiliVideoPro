package online.bingzi.bilibili.video.pro.internal.gui.handler

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import taboolib.common.platform.function.submit
import online.bingzi.bilibili.video.pro.internal.gui.GuiManager
import online.bingzi.bilibili.video.pro.internal.gui.config.GuiConfigManager
import online.bingzi.bilibili.video.pro.internal.helper.ketherEval

/**
 * GUI操作处理器
 * 处理GUI中的各种操作
 */
object GuiActionHandler {
    
    /**
     * 处理GUI操作
     */
    fun handleAction(player: Player, action: String?, clickType: ClickType) {
        if (action == null) return
        
        when (action) {
            // 基础操作
            "close" -> player.closeInventory()
            "refresh" -> refreshCurrentGui(player)
            
            // 导航操作
            "back_to_main" -> GuiManager.showMainMenu(player)
            "open_video_list" -> GuiManager.showVideoList(player)
            "open_player_stats" -> GuiManager.showPlayerStats(player)
            "open_admin" -> GuiManager.showAdminGui(player)
            
            // 登录相关
            "login" -> handleLogin(player)
            
            // 系统操作
            "show_status" -> handleShowStatus(player)
            "show_help" -> handleShowHelp(player)
            "reload_config" -> handleReloadConfig(player)
            
            // 管理员操作
            "open_player_management" -> handlePlayerManagement(player)
            "open_system_stats" -> handleSystemStats(player)
            "open_database_management" -> handleDatabaseManagement(player)
            "open_log_viewer" -> handleLogViewer(player)
            "open_backup_management" -> handleBackupManagement(player)
            
            // 自定义操作
            else -> handleCustomAction(player, action, clickType)
        }
        
        playActionSound(player, action)
    }
    
    /**
     * 处理登录操作
     */
    private fun handleLogin(player: Player) {
        val binding = online.bingzi.bilibili.video.pro.internal.database.service.PlayerBilibiliService.findByPlayerUuid(player.uniqueId.toString())
        if (binding != null) {
            // 已登录，显示统计信息
            GuiManager.showPlayerStats(player)
        } else {
            // 未登录，执行登录命令
            player.closeInventory()
            player.performCommand("bvp login")
        }
    }
    
    /**
     * 处理显示状态
     */
    private fun handleShowStatus(player: Player) {
        player.closeInventory()
        player.performCommand("bvp info")
    }
    
    /**
     * 处理显示帮助
     */
    private fun handleShowHelp(player: Player) {
        player.closeInventory()
        player.performCommand("bvp")
    }
    
    /**
     * 处理重载配置
     */
    private fun handleReloadConfig(player: Player) {
        if (!player.hasPermission("bilibilipro.admin")) {
            player.sendMessage("§c您没有权限执行此操作！")
            return
        }
        
        player.closeInventory()
        player.performCommand("bvp reload")
    }
    
    /**
     * 处理玩家管理
     */
    private fun handlePlayerManagement(player: Player) {
        if (!player.hasPermission("bilibilipro.admin")) {
            player.sendMessage("§c您没有权限使用管理员功能！")
            return
        }
        
        online.bingzi.bilibili.video.pro.internal.gui.menu.AdminGui.showPlayerManagement(player)
    }
    
    /**
     * 处理系统统计
     */
    private fun handleSystemStats(player: Player) {
        if (!player.hasPermission("bilibilipro.admin")) {
            player.sendMessage("§c您没有权限使用管理员功能！")
            return
        }
        
        player.sendMessage("§7系统统计功能开发中...")
    }
    
    /**
     * 处理数据库管理
     */
    private fun handleDatabaseManagement(player: Player) {
        if (!player.hasPermission("bilibilipro.admin")) {
            player.sendMessage("§c您没有权限使用管理员功能！")
            return
        }
        
        player.sendMessage("§7数据库管理功能开发中...")
    }
    
    /**
     * 处理日志查看
     */
    private fun handleLogViewer(player: Player) {
        if (!player.hasPermission("bilibilipro.admin")) {
            player.sendMessage("§c您没有权限使用管理员功能！")
            return
        }
        
        player.sendMessage("§7日志查看功能开发中...")
    }
    
    /**
     * 处理备份管理
     */
    private fun handleBackupManagement(player: Player) {
        if (!player.hasPermission("bilibilipro.admin")) {
            player.sendMessage("§c您没有权限使用管理员功能！")
            return
        }
        
        player.sendMessage("§7备份管理功能开发中...")
    }
    
    /**
     * 刷新当前GUI
     */
    private fun refreshCurrentGui(player: Player) {
        // 重新打开当前GUI
        submit(async = false, delay = 1) {
            GuiManager.showMainMenu(player)
        }
    }
    
    /**
     * 处理自定义操作
     */
    private fun handleCustomAction(player: Player, action: String, clickType: ClickType) {
        // 支持格式: command:命令 或 script:脚本
        when {
            action.startsWith("command:") -> {
                val command = action.removePrefix("command:")
                player.closeInventory()
                player.performCommand(command)
            }
            
            action.startsWith("script:") -> {
                val script = action.removePrefix("script:")
                player.closeInventory()
                executeKetherScript(player, script)
            }
            
            action.startsWith("message:") -> {
                val message = action.removePrefix("message:")
                player.sendMessage(message.replace("&", "§"))
            }
            
            action.startsWith("gui:") -> {
                val guiName = action.removePrefix("gui:")
                openCustomGui(player, guiName)
            }
            
            else -> {
                player.sendMessage("§c未知操作: $action")
            }
        }
    }
    
    /**
     * 执行Kether脚本
     */
    private fun executeKetherScript(player: Player, script: String) {
        try {
            script.ketherEval(player as taboolib.common.platform.ProxyCommandSender)
        } catch (e: Exception) {
            player.sendMessage("§c脚本执行失败: ${e.message}")
        }
    }
    
    /**
     * 打开自定义GUI
     */
    private fun openCustomGui(player: Player, guiName: String) {
        val window = online.bingzi.bilibili.video.pro.internal.gui.builder.ConfigurableGuiBuilder.buildGui(guiName, player)
        if (window != null) {
            window.open()
        } else {
            player.sendMessage("§c无法找到GUI配置: $guiName")
        }
    }
    
    /**
     * 播放操作声音
     */
    private fun playActionSound(player: Player, action: String) {
        val soundConfig = GuiConfigManager.getSoundConfig()
        if (!soundConfig.enabled) return
        
        val soundName = when {
            action.contains("error") || action == "close" -> soundConfig.effects["error"] ?: "ENTITY_VILLAGER_NO"
            action.contains("success") || action == "reload_config" -> soundConfig.effects["success"] ?: "ENTITY_EXPERIENCE_ORB_PICKUP"
            else -> soundConfig.effects["click"] ?: "UI_BUTTON_CLICK"
        }
        
        try {
            val sound = org.bukkit.Sound.valueOf(soundName)
            player.playSound(player.location, sound, soundConfig.volume, soundConfig.pitch)
        } catch (e: Exception) {
            // 忽略声音播放错误
        }
    }
}