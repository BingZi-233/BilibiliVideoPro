package online.bingzi.bilibili.video.pro.internal.helper

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import taboolib.module.lang.sendError

/**
 * NMSHelper
 * 提供使用 ProtocolLib 发送地图数据包和虚拟物品的可靠功能。
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
        try {
            val packet = protocolManager.createPacket(PacketType.Play.Server.MAP)

            // 针对Minecraft 1.21.1版本，简化字段设置，只设置必要的字段
            try {
                // 1. 设置地图ID - 对于MAP包，这通常是第一个字段
                if (packet.integers.size() > 0) {
                    packet.integers.write(0, mapId)
                } else {
                    // 如果没有integers字段，尝试用modifier
                    packet.modifier.write(0, mapId)
                }

                // 2. 设置缩放级别
                if (packet.bytes.size() > 0) {
                    packet.bytes.write(0, scale)
                }

                // 3. 设置布尔值字段（locked和trackingPosition）
                if (packet.booleans.size() >= 1) {
                    packet.booleans.write(0, locked)
                }
                if (packet.booleans.size() >= 2) {
                    packet.booleans.write(1, trackingPosition)
                }

                // 4. 设置图标列表（可选）
                try {
                    packet.modifier.write(1, emptyList<Any>())
                } catch (e: Exception) {
                    // 忽略图标设置错误
                }

                // 5. 设置地图数据
                if (data.isNotEmpty()) {
                    // 设置数据区域坐标和尺寸
                    val intSize = packet.integers.size()
                    if (intSize >= 5) {
                        packet.integers.write(1, dirtyX)
                        packet.integers.write(2, dirtyY) 
                        packet.integers.write(3, dirtyWidth)
                        packet.integers.write(4, dirtyHeight)
                    }
                    
                    // 设置像素数据
                    packet.byteArrays.write(0, data)
                } else {
                    // 空数据包
                    packet.byteArrays.write(0, ByteArray(0))
                }

            } catch (fieldException: Exception) {
                console().sendError("nmsPacketFieldsFailed", mapId, fieldException.message ?: "Unknown error")
                return // 如果字段设置失败，直接返回，不发送数据包
            }

            protocolManager.sendServerPacket(player, packet)
            
        } catch (e: Exception) {
            console().sendError("nmsMapPacketFailed", player.name, mapId, e.message ?: "Unknown error")
        }
    }

    /**
     * 向玩家发送虚拟物品，该物品仅在客户端显示，不会添加到玩家背包。
     * 使用SET_SLOT数据包将物品发送到玩家的热键栏指定位置。
     *
     * @param player 目标玩家
     * @param slot 热键栏位置 (0-8)，-1 表示主手
     * @param itemStack 要发送的物品
     */
    fun sendVirtualItem(player: Player, slot: Int, itemStack: ItemStack) {
        try {
            val packet = protocolManager.createPacket(PacketType.Play.Server.SET_SLOT)
            
            // 设置窗口ID，0表示玩家背包
            packet.integers.write(0, 0)
            
            // 设置状态ID，可以使用0
            packet.integers.write(1, 0)
            
            // 设置槽位，36-44是热键栏位置0-8，-1是主手
            val actualSlot = if (slot == -1) -1 else 36 + slot
            packet.integers.write(2, actualSlot)
            
            // 设置物品 - 使用modifier方式
            packet.modifier.write(0, itemStack)
            
            protocolManager.sendServerPacket(player, packet)
        } catch (e: Exception) {
            console().sendError("nmsVirtualItemFailed", player.name, e.message ?: "Unknown error")
        }
    }

    /**
     * 向玩家发送虚拟地图物品，该物品仅在客户端显示，不会添加到玩家背包。
     * 同时发送地图数据包以确保地图内容正确显示。
     *
     * @param player 目标玩家
     * @param slot 热键栏位置 (0-8)，-1 表示主手
     * @param mapItem 要发送的地图物品
     */
    fun sendVirtualMapItem(player: Player, slot: Int, mapItem: ItemStack) {
        try {
            // 首先发送虚拟物品
            sendVirtualItem(player, slot, mapItem)
            
            // 如果是地图物品，还需要发送地图数据包
            if (mapItem.itemMeta is MapMeta) {
                val mapMeta = mapItem.itemMeta as MapMeta
                val mapView = mapMeta.mapView
                
                if (mapView != null) {
                    // 获取地图ID
                    val mapId = mapView.id
                    
                    // 延迟发送地图数据包，确保客户端已经接收到物品
                    submit(delay = 2L) {
                        if (player.isOnline) {
                            // 发送空的地图数据包来触发地图展开
                            val mapData = ByteArray(128 * 128) { 0 }
                            sendMapData(player, mapId, mapData)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            console().sendError("nmsVirtualMapItemFailed", player.name, e.message ?: "Unknown error")
        }
    }

    /**
     * 清除玩家指定位置的虚拟物品。
     *
     * @param player 目标玩家
     * @param slot 热键栏位置 (0-8)，-1 表示主手
     */
    fun clearVirtualItem(player: Player, slot: Int) {
        try {
            val packet = protocolManager.createPacket(PacketType.Play.Server.SET_SLOT)
            
            // 设置窗口ID，0表示玩家背包
            packet.integers.write(0, 0)
            
            // 设置状态ID
            packet.integers.write(1, 0)
            
            // 设置槽位
            val actualSlot = if (slot == -1) -1 else 36 + slot
            packet.integers.write(2, actualSlot)
            
            // 设置为空物品 - 使用modifier方式
            packet.modifier.write(0, null)
            
            protocolManager.sendServerPacket(player, packet)
        } catch (e: Exception) {
            console().sendError("nmsClearVirtualItemFailed", player.name, e.message ?: "Unknown error")
        }
    }

    /**
     * 向玩家临时显示虚拟地图物品，指定时间后自动清除。
     * 专门为地图物品优化，确保地图内容正确显示。
     *
     * @param player 目标玩家
     * @param slot 热键栏位置 (0-8)，-1 表示主手
     * @param mapItem 要发送的地图物品
     * @param durationSeconds 显示持续时间（秒）
     */
    fun sendTemporaryVirtualMapItem(player: Player, slot: Int, mapItem: ItemStack, durationSeconds: Int) {
        sendVirtualMapItem(player, slot, mapItem)
        
        // 使用TabooLib的延时任务在指定时间后清除物品
        submit(delay = durationSeconds * 20L) {
            if (player.isOnline) {
                clearVirtualItem(player, slot)
            }
        }
    }

    /**
     * 向玩家临时显示虚拟物品，指定时间后自动清除。
     *
     * @param player 目标玩家
     * @param slot 热键栏位置 (0-8)，-1 表示主手
     * @param itemStack 要发送的物品
     * @param durationSeconds 显示持续时间（秒）
     */
    fun sendTemporaryVirtualItem(player: Player, slot: Int, itemStack: ItemStack, durationSeconds: Int) {
        sendVirtualItem(player, slot, itemStack)
        
        // 使用TabooLib的延时任务在指定时间后清除物品
        submit(delay = durationSeconds * 20L) {
            if (player.isOnline) {
                clearVirtualItem(player, slot)
            }
        }
    }
}
