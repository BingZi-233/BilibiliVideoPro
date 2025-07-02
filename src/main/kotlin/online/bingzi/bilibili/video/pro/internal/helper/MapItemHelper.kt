package online.bingzi.bilibili.video.pro.internal.helper

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapView
import taboolib.module.chat.colored
import taboolib.platform.util.buildItem
import taboolib.platform.util.modifyMeta

/**
 * 地图物品构建Helper
 * 提供构建包含二维码的地图物品功能
 */
object MapItemHelper {

    /**
     * 创建包含二维码的地图物品
     * @param text 要生成二维码的文本
     * @param displayName 物品显示名称
     * @param lore 物品描述文本列表
     * @return ItemStack 包含二维码的地图物品
     */
    fun createQRCodeMapItem(
        text: String,
        displayName: String = "&a二维码地图",
        lore: List<String> = listOf(
            "&7扫描二维码获取信息",
            "&7内容: &f$text"
        )
    ): ItemStack {
        // 生成二维码图像
        val qrImage = QRCodeHelper.generateQRCode(text)

        // 创建地图视图
        val mapView = createMapView()

        // 添加二维码渲染器
        mapView.renderers.clear()
        mapView.addRenderer(QRCodeHelper.QRCodeMapRenderer(qrImage, text))

        // 构建地图物品
        return buildItem(Material.FILLED_MAP) {
            name = displayName.colored()
            this.colored()
        }.modifyMeta<MapMeta> { // 设置地图ID
            this.mapView = mapView
        }
    }

    /**
     * 创建带有自定义样式的二维码地图物品
     * @param text 要生成二维码的文本
     * @param title 标题
     * @param description 描述
     * @param category 分类
     * @return ItemStack 自定义样式的二维码地图物品
     */
    fun createStyledQRCodeMapItem(
        text: String,
        title: String,
        description: String = "",
        category: String = "信息"
    ): ItemStack {
        val qrImage = QRCodeHelper.generateQRCode(text)
        val mapView = createMapView()

        mapView.renderers.clear()
        mapView.addRenderer(QRCodeHelper.QRCodeMapRenderer(qrImage, text))

        return buildItem(Material.FILLED_MAP) {
            name = "&6&l[$category] &r&a$title"
            lore.addAll(
                listOf(
                    "&7━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                    "&7描述: &f$description",
                    "&7━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                    "&7二维码内容:",
                    "&f$text",
                    "&7━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                    "&e点击右键查看详细信息",
                    "&c注意: 请使用二维码扫描器扫描"
                )
            )
            colored()
        }.modifyMeta<MapMeta> {// 设置地图ID
            this.mapView = mapView
        }
    }

    /**
     * 创建Bilibili登录二维码地图物品
     * @param qrCodeUrl 二维码URL
     * @param qrKey 二维码密钥
     * @return ItemStack Bilibili登录二维码地图物品
     */
    fun createBilibiliLoginQRCodeMap(qrCodeUrl: String, qrKey: String): ItemStack {
        return createStyledQRCodeMapItem(
            text = qrCodeUrl,
            title = "哔哩哔哩登录",
            description = "扫描此二维码进行哔哩哔哩账号登录",
            category = "登录"
        ).also { item ->
            // 为登录二维码添加特殊的NBT标签
            item.modifyMeta<MapMeta> {
                // 可以在这里添加自定义NBT数据
                // 例如存储qrKey用于后续验证
            }
        }
    }

    /**
     * 创建视频分享二维码地图物品
     * @param videoUrl 视频URL
     * @param videoTitle 视频标题
     * @param videoAuthor 视频作者
     * @return ItemStack 视频分享二维码地图物品
     */
    fun createVideoShareQRCodeMap(
        videoUrl: String,
        videoTitle: String,
        videoAuthor: String = "未知"
    ): ItemStack {
        return createStyledQRCodeMapItem(
            text = videoUrl,
            title = videoTitle,
            description = "作者: $videoAuthor",
            category = "视频"
        )
    }

    /**
     * 创建通用信息二维码地图物品
     * @param content 信息内容
     * @param infoType 信息类型
     * @return ItemStack 通用信息二维码地图物品
     */
    fun createInfoQRCodeMap(content: String, infoType: String = "信息"): ItemStack {
        return createStyledQRCodeMapItem(
            text = content,
            title = infoType,
            description = "点击查看详细信息",
            category = "通用"
        )
    }

    /**
     * 批量创建二维码地图物品
     * @param contents 内容列表，每个元素包含文本和标题
     * @return List<ItemStack> 二维码地图物品列表
     */
    fun createBatchQRCodeMaps(contents: List<Pair<String, String>>): List<ItemStack> {
        return contents.map { (text, title) ->
            createQRCodeMapItem(
                text = text,
                displayName = "&a$title",
                lore = listOf(
                    "&7二维码内容: &f$text",
                    "&7右键点击查看详情"
                )
            )
        }
    }

    /**
     * 创建地图视图
     * @return MapView 新的地图视图
     */
    private fun createMapView(): MapView {
        @Suppress("DEPRECATION")
        return Bukkit.createMap(Bukkit.getWorlds().first())
    }

    /**
     * 检查物品是否为二维码地图
     * @param item 要检查的物品
     * @return Boolean 是否为二维码地图
     */
    fun isQRCodeMap(item: ItemStack?): Boolean {
        if (item?.type != Material.FILLED_MAP) return false

        val meta = item.itemMeta as? MapMeta ?: return false
        val mapView = meta.mapView ?: return false

        // 检查是否包含二维码渲染器
        return mapView.renderers.any { it is QRCodeHelper.QRCodeMapRenderer }
    }

    /**
     * 从二维码地图物品中提取文本内容
     * @param item 二维码地图物品
     * @return String? 提取的文本内容，如果无法提取则返回null
     */
    fun extractQRCodeContent(item: ItemStack): String? {
        if (!isQRCodeMap(item)) return null

        // 从渲染器中直接提取内容
        val meta = item.itemMeta as? MapMeta ?: return null
        val mapView = meta.mapView ?: return null

        return mapView.renderers
            .filterIsInstance<QRCodeHelper.QRCodeMapRenderer>()
            .firstOrNull()
            ?.let { renderer ->
                // 从渲染器获取原始二维码文本
                QRCodeHelper.extractContentFromRenderer(renderer)
            }
    }
} 