package online.bingzi.bilibili.video.pro.internal.gui.builder

import online.bingzi.bilibili.video.pro.internal.gui.config.GuiConfigManager
import online.bingzi.bilibili.video.pro.internal.gui.config.GuiConfigManager.GuiItem
import online.bingzi.bilibili.video.pro.internal.gui.handler.GuiActionHandler
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.item.builder.ItemBuilder as InvUIItemBuilder

/**
 * 配置化GUI构建器
 * 根据配置文件动态构建GUI界面
 */
object ConfigurableGuiBuilder {

    /**
     * 根据配置构建GUI
     */
    fun buildGui(guiName: String, player: Player): Window? {
        val layout = GuiConfigManager.getGuiLayout(guiName) ?: return null
        val theme = GuiConfigManager.getCurrentTheme()

        // 验证布局
        if (layout.layout.isEmpty() || layout.size <= 0) {
            return null
        }

        // 构建GUI结构
        val gui = Gui.normal()
            .setStructure(*layout.layout.toTypedArray())

        // 添加物品配置
        layout.items.forEach { (key, itemConfig) ->
            // 检查权限
            if (itemConfig.permission != null && !player.hasPermission(itemConfig.permission)) {
                // 替换为无权限物品
                gui.addIngredient(key.first(), NoPermissionItem(itemConfig.name))
                return@forEach
            }

            // 根据类型创建物品
            val item = when (itemConfig.type) {
                "border" -> BorderItem(itemConfig, theme)
                "login" -> LoginItem(player, itemConfig, theme)
                "video_list" -> ActionItem(player, itemConfig, theme)
                "player_stats" -> ActionItem(player, itemConfig, theme)
                "admin" -> ActionItem(player, itemConfig, theme)
                "system_status" -> SystemStatusItem(player, itemConfig, theme)
                "help" -> ActionItem(player, itemConfig, theme)
                "reload" -> ActionItem(player, itemConfig, theme)
                "close" -> CloseItem(itemConfig, theme)
                "back" -> ActionItem(player, itemConfig, theme)
                "previous_page" -> NavigationItem(itemConfig, theme, false)
                "next_page" -> NavigationItem(itemConfig, theme, true)
                "refresh" -> ActionItem(player, itemConfig, theme)
                else -> ActionItem(player, itemConfig, theme)
            }

            gui.addIngredient(key.first(), item)
        }

        // 创建窗口
        return Window.single()
            .setViewer(player)
            .setTitle(GuiConfigManager.applyThemeColors(layout.title, theme))
            .setGui(gui.build())
            .build()
    }

    /**
     * 边框装饰物品
     */
    private class BorderItem(
        private val config: GuiItem,
        private val theme: GuiConfigManager.GuiTheme
    ) : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            return createItemProvider(config, theme)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // 边框不响应点击
        }
    }

    /**
     * 登录状态物品
     */
    private class LoginItem(
        private val player: Player,
        private val config: GuiItem,
        private val theme: GuiConfigManager.GuiTheme
    ) : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            // 检查登录状态并动态更新显示
            val binding = online.bingzi.bilibili.video.pro.internal.database.service.PlayerBilibiliService.findByPlayerUuid(player.uniqueId.toString())

            return if (binding != null) {
                // 已登录状态
                InvUIItemBuilder(Material.EMERALD)
                    .setDisplayName(GuiConfigManager.applyThemeColors("&a&l已登录", theme))
                    .addLoreLines(
                        GuiConfigManager.applyThemeColors("&7用户: &f${binding.bilibiliUsername}", theme),
                        GuiConfigManager.applyThemeColors("&7UID: &f${binding.bilibiliUid}", theme),
                        "",
                        GuiConfigManager.applyThemeColors("&e点击查看详细信息", theme)
                    )
                    .apply { if (config.enchanted) addEnchantment(Enchantment.DURABILITY, 1, true).addItemFlags(ItemFlag.HIDE_ENCHANTS) }
            } else {
                // 未登录状态
                createItemProvider(config, theme)
            }
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            GuiActionHandler.handleAction(player, config.action, clickType)
            playClickSound(player)
        }
    }

    /**
     * 系统状态物品
     */
    private class SystemStatusItem(
        private val player: Player,
        private val config: GuiItem,
        private val theme: GuiConfigManager.GuiTheme
    ) : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            val isInitialized = online.bingzi.bilibili.video.pro.internal.manager.PluginManager.isInitialized()
            val statusColor = if (isInitialized) theme.successColor else theme.errorColor
            val status = if (isInitialized) "正常" else "异常"

            return InvUIItemBuilder(if (isInitialized) Material.EMERALD else Material.REDSTONE)
                .setDisplayName(GuiConfigManager.applyThemeColors(config.name, theme))
                .addLoreLines(
                    GuiConfigManager.applyThemeColors("&7插件状态: $statusColor$status", theme),
                    GuiConfigManager.applyThemeColors("&7网络连接: ${theme.successColor}正常", theme),
                    "",
                    *config.lore.map { GuiConfigManager.applyThemeColors(it, theme) }.toTypedArray()
                )
                .apply { if (config.enchanted) addEnchantment(Enchantment.DURABILITY, 1, true).addItemFlags(ItemFlag.HIDE_ENCHANTS) }
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            GuiActionHandler.handleAction(player, config.action, clickType)
            playClickSound(player)
        }
    }

    /**
     * 通用操作物品
     */
    private class ActionItem(
        private val player: Player,
        private val config: GuiItem,
        private val theme: GuiConfigManager.GuiTheme
    ) : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            return createItemProvider(config, theme)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            GuiActionHandler.handleAction(player, config.action, clickType)
            playClickSound(player)
        }
    }

    /**
     * 关闭按钮
     */
    private class CloseItem(
        private val config: GuiItem,
        private val theme: GuiConfigManager.GuiTheme
    ) : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            return createItemProvider(config, theme)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            player.closeInventory()
            playClickSound(player)
        }
    }

    /**
     * 导航按钮
     */
    private class NavigationItem(
        private val config: GuiItem,
        private val theme: GuiConfigManager.GuiTheme,
        private val isNext: Boolean
    ) : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            return createItemProvider(config, theme)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // 分页逻辑将在分页GUI中实现
            playClickSound(player)
        }
    }

    /**
     * 无权限物品
     */
    private class NoPermissionItem(
        private val originalName: String
    ) : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.BARRIER)
                .setDisplayName("§c§l权限不足")
                .addLoreLines(
                    "§7您没有权限使用此功能",
                    "§7原功能: §f$originalName"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            player.sendMessage("§c您没有权限使用此功能！")
            playErrorSound(player)
        }
    }

    /**
     * 创建物品提供器
     */
    private fun createItemProvider(config: GuiItem, theme: GuiConfigManager.GuiTheme): ItemProvider {
        val builder = InvUIItemBuilder(config.material)
            .setDisplayName(GuiConfigManager.applyThemeColors(config.name, theme))
            .setAmount(config.amount)

        // 添加描述
        if (config.lore.isNotEmpty()) {
            builder.addLoreLines(*config.lore.map { GuiConfigManager.applyThemeColors(it, theme) }.toTypedArray())
        }

        // 添加自定义模型数据
        config.customModelData?.let { builder.setCustomModelData(it) }

        // 添加附魔效果
        if (config.enchanted) {
            builder.addEnchantment(Enchantment.DURABILITY, 1, true)
            builder.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }

        return builder
    }

    /**
     * 播放点击声音
     */
    private fun playClickSound(player: Player) {
        val soundConfig = GuiConfigManager.getSoundConfig()
        if (soundConfig.enabled) {
            try {
                val sound = org.bukkit.Sound.valueOf(soundConfig.effects["click"] ?: "UI_BUTTON_CLICK")
                player.playSound(player.location, sound, soundConfig.volume, soundConfig.pitch)
            } catch (e: Exception) {
                // 忽略声音播放错误
            }
        }
    }

    /**
     * 播放错误声音
     */
    private fun playErrorSound(player: Player) {
        val soundConfig = GuiConfigManager.getSoundConfig()
        if (soundConfig.enabled) {
            try {
                val sound = org.bukkit.Sound.valueOf(soundConfig.effects["error"] ?: "ENTITY_VILLAGER_NO")
                player.playSound(player.location, sound, soundConfig.volume, soundConfig.pitch)
            } catch (e: Exception) {
                // 忽略声音播放错误
            }
        }
    }
}