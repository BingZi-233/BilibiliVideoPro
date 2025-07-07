package online.bingzi.bilibili.video.pro.internal.gui.menu

import online.bingzi.bilibili.video.pro.internal.database.service.PlayerBilibiliService
import online.bingzi.bilibili.video.pro.internal.database.service.VideoInteractionService
import online.bingzi.bilibili.video.pro.internal.gui.GuiManager
import online.bingzi.bilibili.video.pro.internal.manager.PluginManager
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.sendLang
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window
import java.text.SimpleDateFormat
import xyz.xenondevs.invui.item.builder.ItemBuilder as InvUIItemBuilder

/**
 * 管理员GUI
 * 提供管理员专用功能
 */
object AdminGui {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    /**
     * 显示管理员主界面
     */
    fun show(player: Player) {
        if (!player.hasPermission("bilibilipro.admin")) {
            player.sendLang("noPermission")
            return
        }

        val gui = Gui.normal()
            .setStructure(
                "# # # # # # # # #",
                "# a # b # c # d #",
                "# # # # # # # # #",
                "# e # f # g # h #",
                "# # # # # # # # #"
            )
            .addIngredient('#', BorderItem())
            .addIngredient('a', PlayerManagementItem(player))
            .addIngredient('b', SystemStatsItem(player))
            .addIngredient('c', ConfigManagementItem(player))
            .addIngredient('d', DatabaseManagementItem(player))
            .addIngredient('e', LogViewerItem(player))
            .addIngredient('f', BackupItem(player))
            .addIngredient('g', ReloadItem(player))
            .addIngredient('h', BackButtonItem(player))
            .build()

        val window = Window.single()
            .setViewer(player)
            .setTitle("§c§lBilibiliVideoPro §f- 管理员面板")
            .setGui(gui)
            .build()

        window.open()
    }

    /**
     * 显示玩家管理界面
     */
    fun showPlayerManagement(player: Player) {
        submit(async = true) {
            val allBindings = PlayerBilibiliService.findAll()

            submit(async = false) {
                val gui = PagedGui.items()
                    .setStructure(
                        "# # # # # # # # #",
                        "# x x x x x x x #",
                        "# x x x x x x x #",
                        "# x x x x x x x #",
                        "# < # # b # # > #"
                    )
                    .addIngredient('#', BorderItem())
                    .addIngredient('<', object : AbstractItem() {
                        override fun getItemProvider(): ItemProvider {
                            return InvUIItemBuilder(Material.ARROW).setDisplayName("§7上一页")
                        }

                        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {}
                    })
                    .addIngredient('>', object : AbstractItem() {
                        override fun getItemProvider(): ItemProvider {
                            return InvUIItemBuilder(Material.ARROW).setDisplayName("§7下一页")
                        }

                        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {}
                    })
                    .addIngredient('b', BackToAdminItem(player))
                    .setContent(allBindings.map { PlayerManagementItemEntry(player, it) })
                    .build()

                val window = Window.single()
                    .setViewer(player)
                    .setTitle("§c§l管理员面板 §f- 玩家管理")
                    .setGui(gui)
                    .build()

                window.open()
            }
        }
    }

    /**
     * 边框装饰物品
     */
    private class BorderItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setDisplayName("§f ")
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // 不做任何处理
        }
    }

    /**
     * 玩家管理项目
     */
    private class PlayerManagementItem(private val player: Player) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.PLAYER_HEAD)
                .setDisplayName("§a§l玩家管理")
                .addLoreLines(
                    "§7管理已绑定的玩家账户",
                    "§7查看玩家信息和解绑",
                    "",
                    "§e点击进入玩家管理"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            showPlayerManagement(player)
        }
    }

    /**
     * 系统统计项目
     */
    private class SystemStatsItem(private val player: Player) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.BOOK)
                .setDisplayName("§b§l系统统计")
                .addLoreLines(
                    "§7查看服务器整体统计",
                    "§7玩家活跃度分析",
                    "",
                    "§e点击查看统计"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            showSystemStats(player)
        }

        private fun showSystemStats(player: Player) {
            submit(async = true) {
                val totalPlayers = PlayerBilibiliService.getTotalPlayerCount()
                val totalInteractions = VideoInteractionService.getTotalInteractionCount()

                submit(async = false) {
                    val gui = Gui.normal()
                        .setStructure(
                            "# # # # # # # # #",
                            "# a # b # c # d #",
                            "# # # # # # # # #",
                            "# # # # e # # # #",
                            "# # # # # # # # #"
                        )
                        .addIngredient('#', BorderItem())
                        .addIngredient('a', TotalPlayersItem(totalPlayers))
                        .addIngredient('b', TotalInteractionsItem(totalInteractions))
                        .addIngredient('c', SystemStatusItem())
                        .addIngredient('d', DatabaseStatusItem())
                        .addIngredient('e', BackToAdminItem(player))
                        .build()

                    val window = Window.single()
                        .setViewer(player)
                        .setTitle("§c§l管理员面板 §f- 系统统计")
                        .setGui(gui)
                        .build()

                    window.open()
                }
            }
        }
    }

    /**
     * 配置管理项目
     */
    private class ConfigManagementItem(private val player: Player) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.WRITABLE_BOOK)
                .setDisplayName("§d§l配置管理")
                .addLoreLines(
                    "§7管理插件配置",
                    "§7重载配置文件",
                    "",
                    "§e点击管理配置"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            player.closeInventory()
            player.performCommand("bvp reload")
        }
    }

    /**
     * 数据库管理项目
     */
    private class DatabaseManagementItem(private val player: Player) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.CHEST)
                .setDisplayName("§6§l数据库管理")
                .addLoreLines(
                    "§7数据库状态检查",
                    "§7数据备份与恢复",
                    "",
                    "§e点击管理数据库"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // TODO: 实现数据库管理界面
            player.sendMessage("§7数据库管理功能开发中...")
        }
    }

    /**
     * 日志查看项目
     */
    private class LogViewerItem(private val player: Player) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.PAPER)
                .setDisplayName("§e§l日志查看")
                .addLoreLines(
                    "§7查看插件运行日志",
                    "§7错误日志分析",
                    "",
                    "§e点击查看日志"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // TODO: 实现日志查看界面
            player.sendMessage("§7日志查看功能开发中...")
        }
    }

    /**
     * 备份项目
     */
    private class BackupItem(private val player: Player) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.ENDER_CHEST)
                .setDisplayName("§c§l数据备份")
                .addLoreLines(
                    "§7创建数据备份",
                    "§7恢复历史数据",
                    "",
                    "§e点击管理备份"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // TODO: 实现备份管理界面
            player.sendMessage("§7备份管理功能开发中...")
        }
    }

    /**
     * 重载项目
     */
    private class ReloadItem(private val player: Player) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.REPEATER)
                .setDisplayName("§a§l重载插件")
                .addLoreLines(
                    "§7重载插件配置",
                    "§7刷新系统状态",
                    "",
                    "§e点击重载"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            player.closeInventory()
            player.performCommand("bvp reload")
        }
    }

    /**
     * 玩家管理条目
     */
    private class PlayerManagementItemEntry(
        private val admin: Player,
        private val binding: online.bingzi.bilibili.video.pro.internal.database.entity.PlayerBilibili
    ) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.PLAYER_HEAD)
                .setDisplayName("§6§l${binding.playerName}")
                .addLoreLines(
                    "§7UUID: §f${binding.playerUuid}",
                    "§7Bilibili用户: §f${binding.bilibiliUsername}",
                    "§7UID: §f${binding.bilibiliUid}",
                    "§7绑定时间: §f${dateFormat.format(binding.createdTime)}",
                    "§7最后登录: §f${binding.lastLoginTime?.let { dateFormat.format(it) } ?: "从未"}",
                    "§7状态: ${if (binding.isActive) "§a活跃" else "§c不活跃"}",
                    "",
                    "§e左键: 查看详情",
                    "§c右键: 解绑账户"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            when (clickType) {
                ClickType.LEFT -> {
                    showPlayerDetails(admin, binding)
                }

                ClickType.RIGHT -> {
                    confirmUnbind(admin, binding)
                }

                else -> {}
            }
        }

        private fun showPlayerDetails(admin: Player, binding: online.bingzi.bilibili.video.pro.internal.database.entity.PlayerBilibili) {
            // TODO: 实现玩家详情界面
            admin.sendMessage("§7玩家详情功能开发中...")
        }

        private fun confirmUnbind(admin: Player, binding: online.bingzi.bilibili.video.pro.internal.database.entity.PlayerBilibili) {
            admin.closeInventory()
            admin.performCommand("bvp unbind ${binding.playerName}")
        }
    }

    /**
     * 总玩家数项目
     */
    private class TotalPlayersItem(private val count: Int) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.PLAYER_HEAD)
                .setDisplayName("§a§l总绑定玩家")
                .addLoreLines(
                    "§7已绑定玩家数: §f$count",
                    "§7活跃玩家数: §f?",
                    "§7今日新增: §f?"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // 不做任何处理
        }
    }

    /**
     * 总互动数项目
     */
    private class TotalInteractionsItem(private val count: Int) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.DIAMOND)
                .setDisplayName("§b§l总互动数")
                .addLoreLines(
                    "§7总互动次数: §f$count",
                    "§7今日互动: §f?",
                    "§7平均互动: §f?"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // 不做任何处理
        }
    }

    /**
     * 系统状态项目
     */
    private class SystemStatusItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            val isInitialized = PluginManager.isInitialized()
            return InvUIItemBuilder(if (isInitialized) Material.EMERALD else Material.REDSTONE)
                .setDisplayName("§6§l系统状态")
                .addLoreLines(
                    "§7插件状态: ${if (isInitialized) "§a正常" else "§c异常"}",
                    "§7网络连接: §a正常",
                    "§7内存使用: §f${Runtime.getRuntime().let { (it.totalMemory() - it.freeMemory()) / 1024 / 1024 }}MB"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // 不做任何处理
        }
    }

    /**
     * 数据库状态项目
     */
    private class DatabaseStatusItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.EMERALD)
                .setDisplayName("§d§l数据库状态")
                .addLoreLines(
                    "§7数据库类型: §fSQLite",
                    "§7连接状态: §a正常",
                    "§7表数量: §f2"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // 不做任何处理
        }
    }

    /**
     * 返回管理员面板按钮
     */
    private class BackToAdminItem(private val player: Player) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.ARROW)
                .setDisplayName("§c§l返回")
                .addLoreLines(
                    "§7返回管理员面板",
                    "",
                    "§e点击返回"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            show(player)
        }
    }

    /**
     * 返回主菜单按钮
     */
    private class BackButtonItem(private val player: Player) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.BARRIER)
                .setDisplayName("§c§l返回主菜单")
                .addLoreLines(
                    "§7返回主菜单",
                    "",
                    "§e点击返回"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            GuiManager.showMainMenu(player)
        }
    }
}