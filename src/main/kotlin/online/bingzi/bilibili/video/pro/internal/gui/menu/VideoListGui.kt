package online.bingzi.bilibili.video.pro.internal.gui.menu

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.util.sendLang
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.builder.ItemBuilder as InvUIItemBuilder
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.window.Window
import online.bingzi.bilibili.video.pro.internal.database.service.PlayerBilibiliService
import online.bingzi.bilibili.video.pro.internal.gui.GuiManager

/**
 * 视频列表GUI
 * 显示所有配置的BV视频和奖励信息
 */
object VideoListGui {
    
    @Config("config.yml")
    lateinit var config: Configuration
    
    /**
     * 显示视频列表
     */
    fun show(player: Player) {
        val videos = getConfiguredVideos()
        
        val gui = PagedGui.items()
            .setStructure(
                "# # # # # # # # #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "# < # # # # # > #"
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
            .setContent(videos.map { VideoItem(player, it) })
            .build()
        
        val window = Window.single()
            .setViewer(player)
            .setTitle("§6§lBilibiliVideoPro §f- 视频列表")
            .setGui(gui)
            .build()
        
        window.open()
    }
    
    /**
     * 获取配置的视频列表
     */
    private fun getConfiguredVideos(): List<VideoInfo> {
        val videos = mutableListOf<VideoInfo>()
        
        // 添加默认配置
        if (config.getBoolean("triple_action_rewards.default.enabled", true)) {
            videos.add(VideoInfo(
                bvid = "默认奖励",
                title = "默认奖励配置",
                description = "适用于所有未特别配置的视频",
                enabled = true,
                rewardScript = config.getString("triple_action_rewards.default.reward_script", "") ?: ""
            ))
        }
        
        // 添加特定视频配置
        val specificVideos = config.getConfigurationSection("triple_action_rewards.specific_videos")
        specificVideos?.getKeys(false)?.forEach { bvid ->
            val enabled = config.getBoolean("triple_action_rewards.specific_videos.$bvid.enabled", true)
            val script = config.getString("triple_action_rewards.specific_videos.$bvid.reward_script", "")
            
            videos.add(VideoInfo(
                bvid = bvid,
                title = "特定视频: $bvid",
                description = "专门为此视频配置的奖励",
                enabled = enabled,
                rewardScript = script ?: ""
            ))
        }
        
        return videos
    }
    
    /**
     * 视频信息数据类
     */
    private data class VideoInfo(
        val bvid: String,
        val title: String,
        val description: String,
        val enabled: Boolean,
        val rewardScript: String
    )
    
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
     * 视频项目
     */
    private class VideoItem(private val player: Player, private val video: VideoInfo) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            val material = if (video.enabled) Material.EMERALD else Material.REDSTONE
            val statusColor = if (video.enabled) "§a" else "§c"
            val status = if (video.enabled) "启用" else "禁用"
            
            return InvUIItemBuilder(material)
                .setDisplayName("§6§l${video.title}")
                .addLoreLines(
                    "§7BV号: §f${video.bvid}",
                    "§7描述: §f${video.description}",
                    "§7状态: $statusColor$status",
                    "",
                    "§e奖励脚本预览:",
                    *video.rewardScript.split("\n").take(3).map { "§7  $it" }.toTypedArray(),
                    if (video.rewardScript.split("\n").size > 3) "§7  ..." else "",
                    "",
                    if (video.bvid != "默认奖励") "§e左键: 检查一键三联" else "",
                    if (video.bvid != "默认奖励") "§e右键: 快速检查" else "",
                    "§e Shift+左键: 查看完整脚本"
                )
        }
        
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            when (clickType) {
                ClickType.LEFT -> {
                    if (video.bvid != "默认奖励") {
                        player.closeInventory()
                        player.performCommand("bvp check ${video.bvid}")
                    }
                }
                ClickType.RIGHT -> {
                    if (video.bvid != "默认奖励") {
                        player.closeInventory()
                        // 快速检查逻辑
                        player.sendLang("quickCheckStarted", video.bvid)
                        player.performCommand("bvp check ${video.bvid}")
                    }
                }
                ClickType.SHIFT_LEFT -> {
                    showRewardScript(player, video)
                }
                else -> {}
            }
        }
        
        /**
         * 显示奖励脚本详情
         */
        private fun showRewardScript(player: Player, video: VideoInfo) {
            val lines = video.rewardScript.split("\n")
            val gui = Gui.normal()
                .setStructure(
                    "# # # # # # # # #",
                    "# # # # # # # # #",
                    "# # # # # # # # #",
                    "# # # # # # # # #",
                    "# # # # b # # # #"
                )
                .addIngredient('#', ScriptLineItem(lines))
                .addIngredient('b', BackButtonItem())
                .build()
            
            val window = Window.single()
                .setViewer(player)
                .setTitle("§6§l奖励脚本 §f- ${video.bvid}")
                .setGui(gui)
                .build()
            
            window.open()
        }
    }
    
    /**
     * 脚本行项目
     */
    private class ScriptLineItem(private val lines: List<String>) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.PAPER)
                .setDisplayName("§6§l脚本内容")
                .addLoreLines(
                    "§7完整的Kether脚本:",
                    "",
                    *lines.map { "§f$it" }.toTypedArray()
                )
        }
        
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // 不做任何处理
        }
    }
    
    /**
     * 返回按钮
     */
    private class BackButtonItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(Material.ARROW)
                .setDisplayName("§c§l返回")
                .addLoreLines(
                    "§7返回视频列表",
                    "",
                    "§e点击返回"
                )
        }
        
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            GuiManager.showVideoList(player)
        }
    }
}