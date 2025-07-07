package online.bingzi.bilibili.video.pro.internal.gui.menu

import online.bingzi.bilibili.video.pro.internal.database.service.PlayerBilibiliService
import online.bingzi.bilibili.video.pro.internal.database.service.VideoInteractionService
import online.bingzi.bilibili.video.pro.internal.entity.database.service.PlayerStatistics
import online.bingzi.bilibili.video.pro.internal.gui.GuiManager
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import taboolib.common.platform.function.submit
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window
import java.text.SimpleDateFormat
import xyz.xenondevs.invui.item.builder.ItemBuilder as InvUIItemBuilder

/**
 * 个人统计GUI
 * 显示玩家的详细统计信息
 */
object PlayerStatsGui {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    /**
     * 显示个人统计界面
     */
    fun show(player: Player) {
        submit(async = true) {
            val binding = PlayerBilibiliService.findByPlayerUuid(player.uniqueId.toString())
            val stats = VideoInteractionService.getPlayerStatistics(player.uniqueId.toString())

            submit(async = false) {
                if (binding == null) {
                    // 未绑定账户的界面
                    showNotBoundGui(player)
                } else {
                    // 已绑定账户的统计界面
                    showStatsGui(player, binding, stats)
                }
            }
        }
    }

    /**
     * 显示未绑定账户的界面
     */
    private fun showNotBoundGui(player: Player) {
        val gui = Gui.normal()
            .setStructure(
                "# # # # # # # # #",
                "# # # # a # # # #",
                "# # # # # # # # #",
                "# # b # # # c # #",
                "# # # # # # # # #"
            )
            .addIngredient('#', BorderItem())
            .addIngredient('a', NotBoundItem())
            .addIngredient('b', LoginButtonItem(player))
            .addIngredient('c', BackButtonItem(player))
            .build()

        val window = Window.single()
            .setViewer(player)
            .setTitle("§6§lBilibiliVideoPro §f- 个人统计")
            .setGui(gui)
            .build()

        window.open()
    }

    /**
     * 显示统计界面
     */
    private fun showStatsGui(player: Player, binding: online.bingzi.bilibili.video.pro.internal.database.entity.PlayerBilibili, stats: PlayerStatistics) {
        val gui = Gui.normal()
            .setStructure(
                "# # # # # # # # #",
                "# a # b # c # d #",
                "# # # # # # # # #",
                "# e # f # g # h #",
                "# # # # # # # # #"
            )
            .addIngredient('#', BorderItem())
            .addIngredient('a', AccountInfoItem(binding))
            .addIngredient('b', TotalStatsItem(stats))
            .addIngredient('c', TripleActionStatsItem(stats))
            .addIngredient('d', RecentActivityItem(player))
            .addIngredient('e', CookieStatusItem(binding))
            .addIngredient('f', RankingItem(player, stats))
            .addIngredient('g', RefreshItem(player))
            .addIngredient('h', BackButtonItem(player))
            .build()

        val window = Window.single()
            .setViewer(player)
            .setTitle("§6§lBilibiliVideoPro §f- 个人统计")
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
     * 未绑定提示项目
     */
    private class NotBoundItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.BARRIER)
                .setDisplayName("§c§l未绑定Bilibili账户")
                .addLoreLines(
                    "§7您还没有绑定Bilibili账户",
                    "§7无法查看统计信息",
                    "",
                    "§e请先登录您的Bilibili账户"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // 不做任何处理
        }
    }

    /**
     * 账户信息项目
     */
    private class AccountInfoItem(private val binding: online.bingzi.bilibili.video.pro.internal.database.entity.PlayerBilibili) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.PLAYER_HEAD)
                .setDisplayName("§a§l账户信息")
                .addLoreLines(
                    "§7用户名: §f${binding.bilibiliUsername}",
                    "§7UID: §f${binding.bilibiliUid}",
                    "§7等级: §f${binding.bilibiliLevel}",
                    "§7大会员: ${if (binding.isVip) "§a是" else "§c否"}",
                    "§7绑定时间: §f${dateFormat.format(binding.createdTime)}",
                    "§7最后登录: §f${binding.lastLoginTime?.let { dateFormat.format(it) } ?: "从未"}"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // 不做任何处理
        }
    }

    /**
     * 总体统计项目
     */
    private class TotalStatsItem(private val stats: PlayerStatistics) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.BOOK)
                .setDisplayName("§b§l总体统计")
                .addLoreLines(
                    "§7互动视频总数: §f${stats.totalVideos}",
                    "§7总点赞数: §f${stats.totalLikes}",
                    "§7总投币数: §f${stats.totalCoins}",
                    "§7总收藏数: §f${stats.totalFavorites}",
                    "§7总奖励次数: §f${stats.totalRewards}"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // 不做任何处理
        }
    }

    /**
     * 一键三联统计项目
     */
    private class TripleActionStatsItem(private val stats: PlayerStatistics) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.DIAMOND)
                .setDisplayName("§d§l一键三联统计")
                .addLoreLines(
                    "§7完成三联视频数: §f${stats.tripleCompletedVideos}",
                    "§7三联完成率: §f${"%.1f".format(if (stats.totalVideos > 0) stats.tripleCompletedVideos.toDouble() / stats.totalVideos * 100 else 0.0)}%",
                    "§7平均每日三联: §f${"%.1f".format(stats.averageTriplePerDay)}",
                    "",
                    "§a§l成就进度:",
                    getAchievementProgress(stats.tripleCompletedVideos.toInt())
                )
        }

        private fun getAchievementProgress(count: Int): String {
            return when {
                count >= 100 -> "§6★★★ 三联大师 (${count}/100+)"
                count >= 50 -> "§e★★☆ 三联专家 (${count}/50)"
                count >= 20 -> "§a★☆☆ 三联达人 (${count}/20)"
                count >= 5 -> "§7☆☆☆ 三联新手 (${count}/5)"
                else -> "§7☆☆☆ 开始三联之旅 (${count}/5)"
            }
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // 不做任何处理
        }
    }

    /**
     * 最近活动项目
     */
    private class RecentActivityItem(private val player: Player) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.CLOCK)
                .setDisplayName("§e§l最近活动")
                .addLoreLines(
                    "§7查看最近的互动记录",
                    "§7最近7天的活动统计",
                    "",
                    "§e点击查看详细记录"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // TODO: 实现最近活动详情界面
            player.sendMessage("§7功能开发中...")
        }
    }

    /**
     * Cookie状态项目
     */
    private class CookieStatusItem(private val binding: online.bingzi.bilibili.video.pro.internal.database.entity.PlayerBilibili) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            val isValid = binding.hasValidCookies()
            return InvUIItemBuilder(if (isValid) Material.EMERALD else Material.REDSTONE)
                .setDisplayName("§6§lCookie状态")
                .addLoreLines(
                    "§7状态: ${if (isValid) "§a有效" else "§c无效"}",
                    "§7更新时间: §f${dateFormat.format(binding.updatedTime)}",
                    "",
                    if (!isValid) "§c需要重新登录以刷新Cookie" else "§a登录状态正常"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // 不做任何处理
        }
    }

    /**
     * 排行榜项目
     */
    private class RankingItem(private val player: Player, private val stats: PlayerStatistics) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.GOLD_INGOT)
                .setDisplayName("§6§l服务器排行")
                .addLoreLines(
                    "§7您的三联排名: §f#?",
                    "§7服务器总玩家: §f?",
                    "",
                    "§e点击查看完整排行榜"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // TODO: 实现排行榜界面
            player.sendMessage("§7排行榜功能开发中...")
        }
    }

    /**
     * 刷新按钮
     */
    private class RefreshItem(private val player: Player) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.REPEATER)
                .setDisplayName("§a§l刷新数据")
                .addLoreLines(
                    "§7刷新统计信息",
                    "",
                    "§e点击刷新"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            show(player)
        }
    }

    /**
     * 登录按钮
     */
    private class LoginButtonItem(private val player: Player) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.EMERALD)
                .setDisplayName("§a§l开始登录")
                .addLoreLines(
                    "§7绑定您的Bilibili账户",
                    "",
                    "§e点击开始登录"
                )
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            player.closeInventory()
            player.performCommand("bvp login")
        }
    }

    /**
     * 返回按钮
     */
    private class BackButtonItem(private val player: Player) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.ARROW)
                .setDisplayName("§c§l返回")
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