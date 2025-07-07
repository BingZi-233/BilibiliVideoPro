package online.bingzi.bilibili.video.pro.internal.gui.menu

import online.bingzi.bilibili.video.pro.internal.database.service.PlayerBilibiliService
import online.bingzi.bilibili.video.pro.internal.gui.GuiManager
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.item.builder.ItemBuilder as InvUIItemBuilder

/**
 * 主菜单GUI
 * 提供插件的主要功能入口
 */
object MainMenuGui {

    /**
     * 显示主菜单
     */
    fun show(player: Player) {
        val gui = Gui.normal()
            .setStructure(
                "# # # # # # # # #",
                "# a # b # c # d #",
                "# # # # # # # # #",
                "# e # f # g # h #",
                "# # # # # # # # #"
            )
            .addIngredient('#', BorderItem())
            .addIngredient('a', LoginItem(player))
            .addIngredient('b', VideoListItem(player))
            .addIngredient('c', StatsItem(player))
            .addIngredient('d', if (player.hasPermission("bilibilipro.admin")) AdminItem(player) else EmptyItem())
            .addIngredient('e', StatusItem(player))
            .addIngredient('f', HelpItem(player))
            .addIngredient('g', ReloadItem(player))
            .addIngredient('h', CloseItem())
            .build()

        val window = Window.single()
            .setViewer(player)
            .setTitle("§6§lBilibiliVideoPro §f- 主菜单")
            .setGui(gui)
            .build()

        window.open()
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
     * 登录按钮
     */
    private class LoginItem(private val player: Player) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            val binding = PlayerBilibiliService.findByPlayerUuid(player.uniqueId.toString())
            return if (binding != null) {
                InvUIItemBuilder(Material.EMERALD)
                    .setDisplayName("§a§l已登录")
                    .addLoreLines(
                        "§7用户: §f${binding.bilibiliUsername}",
                        "§7UID: §f${binding.bilibiliUid}",
                        "§7登录时间: §f${binding.createdTime}",
                        "",
                        "§e点击查看详细信息"
                    )
            } else {
                InvUIItemBuilder(Material.REDSTONE)
                    .setDisplayName("§c§l未登录")
                    .addLoreLines(
                        "§7您还未绑定Bilibili账户",
                        "",
                        "§e点击开始登录"
                    )
            }
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            player.closeInventory()
            val binding = PlayerBilibiliService.findByPlayerUuid(player.uniqueId.toString())
            if (binding != null) {
                GuiManager.showPlayerStats(player)
            } else {
                player.performCommand("bvp login")
            }
        }
    }

    /**
     * 视频列表按钮
     */
    private class VideoListItem(private val player: Player) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.BOOK)
                .setDisplayName("§b§l视频列表")
                .addLoreLines(
                    "§7查看可获得奖励的视频",
                    "§7检查一键三联状态",
                    "",
                    "§e点击打开视频列表"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            GuiManager.showVideoList(player)
        }
    }

    /**
     * 统计信息按钮
     */
    private class StatsItem(private val player: Player) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.PAPER)
                .setDisplayName("§d§l个人统计")
                .addLoreLines(
                    "§7查看您的互动统计",
                    "§7一键三联完成次数",
                    "",
                    "§e点击查看统计"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            GuiManager.showPlayerStats(player)
        }
    }

    /**
     * 管理员按钮
     */
    private class AdminItem(private val player: Player) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.COMMAND_BLOCK)
                .setDisplayName("§c§l管理员面板")
                .addLoreLines(
                    "§7管理员专用功能",
                    "§7玩家解绑、系统管理",
                    "",
                    "§e点击打开管理面板"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            GuiManager.showAdminGui(player)
        }
    }

    /**
     * 状态按钮
     */
    private class StatusItem(private val player: Player) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.COMPASS)
                .setDisplayName("§6§l系统状态")
                .addLoreLines(
                    "§7查看插件运行状态",
                    "§7网络连接状态",
                    "",
                    "§e点击查看状态"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            player.closeInventory()
            player.performCommand("bvp info")
        }
    }

    /**
     * 帮助按钮
     */
    private class HelpItem(private val player: Player) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.KNOWLEDGE_BOOK)
                .setDisplayName("§a§l帮助")
                .addLoreLines(
                    "§7查看插件帮助信息",
                    "§7命令使用说明",
                    "",
                    "§e点击查看帮助"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            player.closeInventory()
            player.performCommand("bvp")
        }
    }

    /**
     * 重载按钮
     */
    private class ReloadItem(private val player: Player) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return if (player.hasPermission("bilibilipro.admin")) {
                InvUIItemBuilder(Material.REPEATER)
                    .setDisplayName("§e§l重载配置")
                    .addLoreLines(
                        "§7重载插件配置文件",
                        "§c需要管理员权限",
                        "",
                        "§e点击重载配置"
                    )
            } else {
                InvUIItemBuilder(Material.BARRIER)
                    .setDisplayName("§c§l权限不足")
                    .addLoreLines(
                        "§7您没有权限使用此功能",
                        "§c需要管理员权限"
                    )
            }
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            if (player.hasPermission("bilibilipro.admin")) {
                player.closeInventory()
                player.performCommand("bvp reload")
            }
        }
    }

    /**
     * 关闭按钮
     */
    private class CloseItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.BARRIER)
                .setDisplayName("§c§l关闭")
                .addLoreLines(
                    "§7关闭此界面",
                    "",
                    "§e点击关闭"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            player.closeInventory()
        }
    }

    /**
     * 空物品
     */
    private class EmptyItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.AIR)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // 不做任何处理
        }
    }
}