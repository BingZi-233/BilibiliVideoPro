package online.bingzi.bilibili.video.pro.internal.helper

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import org.bukkit.entity.Player

/**
 * NMSHelper
 * 提供使用ProtocolLib发送地图数据包和地图展开数据包的功能
 */
object NMSHelper {

    /**
     * 发送地图数据包给玩家
     * @param player 目标玩家
     * @param mapId 地图ID
     * @param data 地图数据
     */
    fun sendMapDataPacket(player: Player, mapId: Int, data: ByteArray) {
        val protocolManager = ProtocolLibrary.getProtocolManager()
        val packet = protocolManager.createPacket(PacketType.Play.Server.MAP)

        // 设置地图数据包字段
        packet.integers.write(0, mapId) // 地图ID
        packet.byteArrays.write(0, data) // 地图数据

        // 发送数据包
        protocolManager.sendServerPacket(player, packet)
    }

    /**
     * 发送地图展开数据包给玩家
     * @param player 目标玩家
     * @param mapId 地图ID
     * @param scale 地图缩放级别
     * @param trackingPosition 是否跟踪位置
     * @param locked 是否锁定地图
     */
    fun sendMapUnfoldPacket(player: Player, mapId: Int, scale: Int, trackingPosition: Boolean, locked: Boolean) {
        val protocolManager = ProtocolLibrary.getProtocolManager()
        val packet = protocolManager.createPacket(PacketType.Play.Server.MAP)

        // 设置地图展开数据包字段
        packet.integers.write(0, mapId) // 地图ID
        packet.integers.write(1, scale) // 缩放级别
        packet.booleans.write(0, trackingPosition) // 是否跟踪位置
        packet.booleans.write(1, locked) // 是否锁定地图

        // 发送数据包
        protocolManager.sendServerPacket(player, packet)
    }
}