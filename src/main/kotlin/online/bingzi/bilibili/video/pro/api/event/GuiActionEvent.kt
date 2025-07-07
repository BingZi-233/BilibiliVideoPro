package online.bingzi.bilibili.video.pro.api.event

import online.bingzi.bilibili.video.pro.api.entity.event.GuiType
import org.bukkit.entity.Player

/**
 * GUI操作事件
 * 
 * 当玩家在插件的图形界面中执行操作时触发此事件。
 * 可以用于用户行为分析、操作日志记录或触发相关的业务逻辑。
 * 
 * @param player 执行操作的玩家
 * @param guiType GUI类型
 * @param actionType 操作类型
 * @param actionData 操作相关的额外数据
 * 
 * @author BilibiliVideoPro
 * @since 2.0.0
 */
class GuiActionEvent(
    /**
     * 执行操作的玩家
     */
    val player: Player,

    /**
     * GUI类型
     * @see GuiType
     */
    val guiType: GuiType,

    /**
     * 操作类型
     * @see GuiActionType
     */
    val actionType: GuiActionType,

    /**
     * 操作相关的额外数据
     * 键值对形式存储，可能包含：
     * - "slot": 点击的槽位号
     * - "item": 点击的物品类型
     * - "input": 输入的文本内容
     * - "page": 当前页面号
     * 等等
     */
    val actionData: Map<String, Any> = emptyMap()
) : BilibiliVideoProxyEvent()