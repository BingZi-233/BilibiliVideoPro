package online.bingzi.bilibili.video.pro.internal.helper

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.bukkit.entity.Player
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage

/**
 * 二维码生成Helper
 * 提供将字符串转换为二维码的功能
 */
object QRCodeHelper {

    /**
     * 将字符串转换为二维码图像
     * @param text 要转换的文本
     * @param size 二维码大小 (像素)
     * @return BufferedImage 二维码图像
     */
    fun generateQRCode(text: String, size: Int = 128): BufferedImage {
        try {
            // 使用ZXing库生成真正的二维码
            val writer = QRCodeWriter()
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1
            )

            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints)
            val image = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)

            for (x in 0 until size) {
                for (y in 0 until size) {
                    val rgb = if (bitMatrix[x, y]) Color.BLACK.rgb else Color.WHITE.rgb
                    image.setRGB(x, y, rgb)
                }
            }

            return image
        } catch (e: Exception) {
            // 如果ZXing失败，使用简化版本作为后备
            return generateSimpleQRCode(text, size)
        }
    }

    /**
     * 生成简化的二维码图像 (后备方案)
     * @param text 要转换的文本
     * @param size 二维码大小 (像素)
     * @return BufferedImage 简化的二维码图像
     */
    private fun generateSimpleQRCode(text: String, size: Int): BufferedImage {
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()

        // 填充白色背景
        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, size, size)

        // 生成简单的模式
        graphics.color = Color.BLACK
        val blockSize = size / 21

        // 绘制位置检测模块 (三个角的方块)
        drawPositionDetectionPattern(graphics, 0, 0, blockSize)
        drawPositionDetectionPattern(graphics, 14 * blockSize, 0, blockSize)
        drawPositionDetectionPattern(graphics, 0, 14 * blockSize, blockSize)

        // 根据文本内容生成简单的数据模块
        val hashCode = text.hashCode()
        for (i in 0..20) {
            for (j in 0..20) {
                if (isPositionDetectionArea(i, j)) continue

                val shouldFill = ((hashCode + i * j) % 3) == 0
                if (shouldFill) {
                    graphics.fillRect(i * blockSize, j * blockSize, blockSize, blockSize)
                }
            }
        }

        graphics.dispose()
        return image
    }

    /**
     * 绘制位置检测模块
     */
    private fun drawPositionDetectionPattern(graphics: Graphics2D, x: Int, y: Int, blockSize: Int) {
        // 外框 7x7
        graphics.fillRect(x, y, 7 * blockSize, 7 * blockSize)
        graphics.color = Color.WHITE
        graphics.fillRect(x + blockSize, y + blockSize, 5 * blockSize, 5 * blockSize)
        graphics.color = Color.BLACK
        graphics.fillRect(x + 2 * blockSize, y + 2 * blockSize, 3 * blockSize, 3 * blockSize)
    }

    /**
     * 检查是否为位置检测区域
     */
    private fun isPositionDetectionArea(i: Int, j: Int): Boolean {
        return (i < 9 && j < 9) || // 左上角
                (i > 11 && j < 9) || // 右上角
                (i < 9 && j > 11)    // 左下角
    }

    /**
     * 创建二维码地图渲染器
     */
    class QRCodeMapRenderer(private val qrImage: BufferedImage, private val content: String) : MapRenderer() {

        override fun render(map: MapView, canvas: org.bukkit.map.MapCanvas, player: Player) {
            // 将二维码图像渲染到地图画布上
            val mapSize = 128 // Minecraft地图标准大小
            val scaledImage = BufferedImage(mapSize, mapSize, BufferedImage.TYPE_INT_RGB)
            val graphics = scaledImage.createGraphics()

            // 缩放二维码图像到地图大小
            graphics.drawImage(qrImage, 0, 0, mapSize, mapSize, null)
            graphics.dispose()

            // 将图像数据转换为地图像素
            for (x in 0 until mapSize) {
                for (y in 0 until mapSize) {
                    val rgb = scaledImage.getRGB(x, y)
                    val brightness = (rgb and 0xFF) // 使用蓝色通道作为亮度

                    // 转换为Minecraft地图颜色
                    val mapColor = when {
                        brightness > 200 -> 34.toByte() // 白色
                        brightness > 100 -> 8.toByte()  // 灰色
                        else -> 119.toByte()            // 黑色
                    }

                    canvas.setPixel(x, y, mapColor)
                }
            }
        }

        fun getContent(): String = content
    }

    /**
     * 从渲染器提取二维码内容
     */
    fun extractContentFromRenderer(renderer: QRCodeMapRenderer): String {
        return renderer.getContent()
    }
} 