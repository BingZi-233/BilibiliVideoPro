package online.bingzi.bilibili.video.pro.api.event

/**
 * GUI操作类型枚举
 *
 * 定义了用户在图形界面中可以执行的操作类型。
 * 用于GUI事件的细分和不同操作的处理。
 *
 * @author BilibiliVideoPro
 * @since 2.0.0
 */
enum class GuiActionType {
    /**
     * 打开操作
     * 用户打开或显示GUI界面
     */
    OPEN,

    /**
     * 关闭操作
     * 用户关闭或退出GUI界面
     */
    CLOSE,

    /**
     * 点击操作
     * 用户点击GUI中的按钮或物品
     */
    CLICK,

    /**
     * 拖拽操作
     * 用户拖拽GUI中的物品
     */
    DRAG,

    /**
     * 输入操作
     * 用户在GUI中进行文本输入或数据输入
     */
    INPUT
}