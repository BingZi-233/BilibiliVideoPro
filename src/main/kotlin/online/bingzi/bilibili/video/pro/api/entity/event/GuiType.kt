package online.bingzi.bilibili.video.pro.api.entity.event

/**
 * GUI类型枚举
 * 
 * 定义了插件中的各种用户界面类型。
 * 用于GUI事件的分类和处理。
 * 
 * @author BilibiliVideoPro
 * @since 2.0.0
 */
enum class GuiType {
    /**
     * 主菜单界面
     * 插件的主要导航界面
     */
    MAIN_MENU,
    
    /**
     * 玩家统计界面
     * 显示玩家的B站绑定信息和互动统计
     */
    PLAYER_STATS,
    
    /**
     * 视频列表界面
     * 显示视频列表和相关操作
     */
    VIDEO_LIST,
    
    /**
     * 管理员面板
     * 管理员专用的系统管理界面
     */
    ADMIN_PANEL
}