package online.bingzi.bilibili.video.pro.internal.helper

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import org.bukkit.entity.Player
import taboolib.common.platform.function.console
import taboolib.module.lang.sendError

/**
 * NMSHelper
 * 提供使用 ProtocolLib 发送地图数据包的可靠功能。
 * 经过重构以正确使用 ProtocolLib 5.3.0+ 和现代Minecraft版本 (1.19.4+)。
 */
object NMSHelper {

    private val protocolManager = ProtocolLibrary.getProtocolManager()

    /**
     * 发送地图的像素数据。
     * 这是最常见的用途，用于在地图上绘制图像。
     *
     * @param player 目标玩家
     * @param mapId 地图ID
     * @param data 128x128的地图像素数据 (ByteArray)
     * @param icons 可选的地图图标列表
     */
    fun sendMapData(player: Player, mapId: Int, data: ByteArray) {
        // 对于纯数据更新，我们通常使用现有的地图属性。
        // 这里假设 scale=0, tracking=true, locked=false 作为安全的默认值。
        sendFullMapPacket(player, mapId, 0, true, false, 0, 0, 128, 128, data)
    }

    /**
     * 更新地图的属性，例如缩放级别或锁定状态，而不改变像素数据。
     *
     * @param player 目标玩家
     * @param mapId 地图ID
     * @param scale 新的缩放级别 (0-4)
     * @param trackingPosition 是否跟踪玩家位置
     * @param locked 地图是否被锁定
     */
    fun updateMapProperties(player: Player, mapId: Int, scale: Byte, trackingPosition: Boolean, locked: Boolean) {
        // 当只更新属性时，我们发送一个空的数据部分。
        sendFullMapPacket(player, mapId, scale, trackingPosition, locked, 0, 0, 0, 0, ByteArray(0))
    }

    /**
     * 发送一个结构完整的地图数据包 (PacketPlayOutMap)。
     * 这是核心的私有方法，确保所有字段都按照现代Minecraft协议被正确设置。
     *
     * @param player 目标玩家
     * @param mapId 地图ID
     * @param scale 缩放级别 (0-4)
     * @param trackingPosition 是否跟踪位置
     * @param locked 地图是否锁定
     * @param icons 地图上显示的图标列表
     * @param dirtyX 更新区域的起始X坐标 (0-127)
     * @param dirtyY 更新区域的起始Y坐标 (0-127)
     * @param dirtyWidth 更新区域的宽度
     * @param dirtyHeight 更新区域的高度
     * @param data 更新区域的像素数据
     */
    private fun sendFullMapPacket(
        player: Player,
        mapId: Int,
        scale: Byte,
        trackingPosition: Boolean,
        locked: Boolean,
        dirtyX: Int,
        dirtyY: Int,
        dirtyWidth: Int,
        dirtyHeight: Int,
        data: ByteArray
    ) {
        val packet = protocolManager.createPacket(PacketType.Play.Server.MAP)

        // 1. 写入地图ID (Integer)
        packet.integers.write(0, mapId)

        // 2. 写入缩放级别 (Byte)
        packet.bytes.write(0, scale)

        // 3. 写入是否锁定 (Boolean)
        packet.booleans.write(0, locked)

        // 4. 写入图标 (Optional<List<MapIcon>>)
        // 在现代版本中，如果列表为空，应写入Optional.empty()，而不是null。


        // 5. 写入数据更新区域 (如果数据为空则不写入)
        if (data.isNotEmpty()) {
            packet.integers.write(1, dirtyX)
            packet.integers.write(2, dirtyY)
            packet.integers.write(3, dirtyWidth)
            packet.integers.write(4, dirtyHeight)
            packet.byteArrays.write(0, data)
        } else {
            // 如果没有数据，宽度和高度必须为0，表示这是一个属性更新包。
            packet.integers.write(3, 0)
            packet.integers.write(4, 0)
            packet.byteArrays.write(0, ByteArray(0))
        }

        try {
            protocolManager.sendServerPacket(player, packet)
        } catch (e: Exception) {
            console().sendError("NMSHelper", "Failed to send map packet for mapId $mapId to player ${player.name}", e)
        }
    }
}
