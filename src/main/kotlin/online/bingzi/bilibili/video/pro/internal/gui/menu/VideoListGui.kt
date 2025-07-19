package online.bingzi.bilibili.video.pro.internal.gui.menu

import online.bingzi.bilibili.video.pro.internal.gui.GuiManager
import online.bingzi.bilibili.video.pro.internal.gui.config.GuiConfigManager
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.util.sendLang
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.item.builder.ItemBuilder as InvUIItemBuilder

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
        val guiLayout = GuiConfigManager.getVideoListLayout()
        val theme = GuiConfigManager.getCurrentTheme()

        // 使用配置的布局或回退到默认布局
        val layout = guiLayout?.layout ?: listOf(
            "# # # # # # # # #",
            "# x x x x x x x #",
            "# x x x x x x x #",
            "# x x x x x x x #",
            "# < # # b # # > #"
        )

        val gui = PagedGui.items()
            .setStructure(*layout.toTypedArray())
            .addIngredient('#', BorderItem(theme))
            .addIngredient('<', PreviousPageItem(guiLayout))
            .addIngredient('>', NextPageItem(guiLayout))
            .addIngredient('b', BackToMainItem(guiLayout))
            .setContent(videos.map { VideoItem(player, it, guiLayout) })
            .build()

        val title = GuiConfigManager.applyThemeColors(
            guiLayout?.title ?: "§6§lBilibiliVideoPro §f- 视频列表",
            theme
        )

        val window = Window.single()
            .setViewer(player)
            .setTitle(title)
            .setGui(gui)
            .build()

        window.open()
    }

    /**
     * 获取配置的视频列表
     */
    private fun getConfiguredVideos(): List<VideoInfo> {
        val videos = mutableListOf<VideoInfo>()

        // 检查三联奖励是否启用
        if (!config.getBoolean("triple_action_rewards.enabled", true)) {
            videos.add(
                VideoInfo(
                    bvid = "功能已禁用",
                    title = "三联奖励功能已禁用",
                    description = "请在配置文件中启用 triple_action_rewards.enabled",
                    enabled = false,
                    rewardScript = ""
                )
            )
            return videos
        }

        // 添加默认配置
        if (config.getBoolean("triple_action_rewards.default.enabled", true)) {
            videos.add(
                VideoInfo(
                    bvid = "默认奖励",
                    title = "默认奖励配置",
                    description = "适用于所有未特别配置的视频",
                    enabled = true,
                    rewardScript = config.getString("triple_action_rewards.default.reward_script", "") ?: ""
                )
            )
        }

        // 添加特定视频配置
        val specificVideos = config.getConfigurationSection("triple_action_rewards.specific_videos")
        if (specificVideos != null && specificVideos.getKeys(false).isNotEmpty()) {
            specificVideos.getKeys(false).forEach { bvid ->
                val enabled = config.getBoolean("triple_action_rewards.specific_videos.$bvid.enabled", true)
                val script = config.getString("triple_action_rewards.specific_videos.$bvid.reward_script", "")

                videos.add(
                    VideoInfo(
                        bvid = bvid,
                        title = "特定视频: $bvid",
                        description = "专门为此视频配置的奖励",
                        enabled = enabled,
                        rewardScript = script ?: ""
                    )
                )
            }
        } else {
            // 如果没有特定视频配置，显示提示信息
            videos.add(
                VideoInfo(
                    bvid = "无特定配置",
                    title = "暂无特定视频配置",
                    description = "您可以在 config.yml 中添加特定BV号的奖励配置",
                    enabled = false,
                    rewardScript = ""
                )
            )
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
    private class BorderItem(private val theme: GuiConfigManager.GuiTheme) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return InvUIItemBuilder(theme.borderMaterial)
                .setDisplayName("§f ")
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // 不做任何处理
        }
    }

    /**
     * 上一页按钮
     */
    private class PreviousPageItem(private val guiLayout: GuiConfigManager.GuiLayout?) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            val item = guiLayout?.items?.get("<")
            return InvUIItemBuilder(item?.material ?: Material.ARROW)
                .setDisplayName(item?.name?.replace("&", "§") ?: "§7上一页")
                .addLoreLines(*(item?.lore?.map { it.replace("&", "§") }?.toTypedArray() ?: arrayOf("§7点击查看上一页")))
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {}
    }

    /**
     * 下一页按钮
     */
    private class NextPageItem(private val guiLayout: GuiConfigManager.GuiLayout?) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            val item = guiLayout?.items?.get(">")
            return InvUIItemBuilder(item?.material ?: Material.ARROW)
                .setDisplayName(item?.name?.replace("&", "§") ?: "§7下一页")
                .addLoreLines(*(item?.lore?.map { it.replace("&", "§") }?.toTypedArray() ?: arrayOf("§7点击查看下一页")))
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {}
    }

    /**
     * 返回主菜单按钮
     */
    private class BackToMainItem(private val guiLayout: GuiConfigManager.GuiLayout?) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            val item = guiLayout?.items?.get("b")
            return InvUIItemBuilder(item?.material ?: Material.ARROW)
                .setDisplayName(item?.name?.replace("&", "§") ?: "§c§l返回")
                .addLoreLines(*(item?.lore?.map { it.replace("&", "§") }?.toTypedArray() ?: arrayOf("§7返回主菜单", "", "§e左键点击")))
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            GuiManager.showMainMenu(player)
        }
    }

    /**
     * 视频项目
     */
    private class VideoItem(
        private val player: Player, 
        private val video: VideoInfo, 
        private val guiLayout: GuiConfigManager.GuiLayout?
    ) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            val videoConfig = GuiConfigManager.videoListConfig.getConfigurationSection("video_item")
            
            val material = when {
                video.bvid == "默认奖励" -> {
                    val materialName = videoConfig?.getString("default_material", "BOOK") ?: "BOOK"
                    try { Material.valueOf(materialName) } catch (e: Exception) { Material.BOOK }
                }
                video.bvid == "功能已禁用" -> Material.BARRIER
                video.bvid == "无特定配置" -> Material.GRAY_DYE
                video.enabled -> {
                    val materialName = videoConfig?.getString("enabled_material", "EMERALD") ?: "EMERALD"
                    try { Material.valueOf(materialName) } catch (e: Exception) { Material.EMERALD }
                }
                else -> {
                    val materialName = videoConfig?.getString("disabled_material", "REDSTONE") ?: "REDSTONE"
                    try { Material.valueOf(materialName) } catch (e: Exception) { Material.REDSTONE }
                }
            }

            val theme = GuiConfigManager.getCurrentTheme()
            val statusColor = if (video.enabled) theme.successColor else theme.errorColor
            val status = if (video.enabled) "启用" else "禁用"

            val displayName = when (video.bvid) {
                "默认奖励" -> {
                    videoConfig?.getString("default_name", "&6&l默认奖励配置")?.replace("&", "§") 
                        ?: "§6§l默认奖励配置"
                }
                "功能已禁用" -> "§c§l功能已禁用"
                "无特定配置" -> "§7§l暂无特定配置"
                else -> {
                    videoConfig?.getString("specific_name", "&6&l{bvid}")
                        ?.replace("{bvid}", video.bvid)?.replace("&", "§")
                        ?: "§6§l${video.bvid}"
                }
            }

            val baseLore = when (video.bvid) {
                "默认奖励" -> {
                    videoConfig?.getStringList("default_lore")?.map { line ->
                        line.replace("&", "§")
                            .replace("{status}", "$statusColor$status")
                    } ?: listOf(
                        "§7适用于所有未特别配置的视频",
                        "§7状态: $statusColor$status",
                        "",
                        "§e左键: 查看脚本详情"
                    )
                }
                "功能已禁用" -> listOf(
                    "§7三联奖励功能已禁用",
                    "§7请在配置文件中启用",
                    "§7triple_action_rewards.enabled: true",
                    "",
                    "§e点击查看详情"
                )
                "无特定配置" -> listOf(
                    "§7暂无特定视频配置",
                    "§7您可以在 config.yml 中添加特定BV号配置",
                    "§7例如: triple_action_rewards.specific_videos.BV1234567890",
                    "",
                    "§e点击查看说明"
                )
                else -> {
                    val rewardPreview = video.rewardScript.split("\n").take(3).joinToString("\n") { "§7  $it" }
                    val hasMore = if (video.rewardScript.split("\n").size > 3) "\n§7  ..." else ""
                    
                    videoConfig?.getStringList("specific_lore")?.map { line ->
                        line.replace("&", "§")
                            .replace("{bvid}", video.bvid)
                            .replace("{status}", "$statusColor$status")
                            .replace("{reward_preview}", rewardPreview + hasMore)
                    } ?: listOf(
                        "§7BV号: §f${video.bvid}",
                        "§7状态: $statusColor$status",
                        "§7奖励预览:",
                        rewardPreview,
                        if (video.rewardScript.split("\n").size > 3) "§7  ..." else "",
                        "",
                        "§e左键: 检查一键三联",
                        "§e右键: 快速检查",
                        "§e Shift+左键: 查看完整脚本"
                    )
                }
            }

            return InvUIItemBuilder(material)
                .setDisplayName(displayName)
                .addLoreLines(*baseLore.toTypedArray())
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // 检查是否为特殊项目
            if (video.bvid in listOf("默认奖励", "功能已禁用", "无特定配置")) {
                when (video.bvid) {
                    "默认奖励" -> {
                        if (clickType == ClickType.SHIFT_LEFT) {
                            showRewardScript(player, video)
                        } else {
                            player.sendMessage("§e这是默认奖励配置，适用于所有未特别配置的视频")
                        }
                    }
                    "功能已禁用" -> {
                        player.sendMessage("§c三联奖励功能已禁用，请在配置文件中启用")
                    }
                    "无特定配置" -> {
                        player.sendMessage("§7暂无特定视频配置，您可以在 config.yml 中添加")
                    }
                }
                return
            }

            when (clickType) {
                ClickType.LEFT -> {
                    player.closeInventory()
                    player.performCommand("bvp check ${video.bvid}")
                }

                ClickType.RIGHT -> {
                    player.closeInventory()
                    // 快速检查逻辑
                    player.sendLang("quickCheckStarted", video.bvid)
                    player.performCommand("bvp check ${video.bvid}")
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