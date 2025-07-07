package online.bingzi.bilibili.video.pro.api.entity.event

/**
 * 奖励类型枚举
 * 
 * 定义了插件支持的各种奖励类型。
 * 用于奖励系统的分类和处理。
 * 
 * @author BilibiliVideoPro
 * @since 2.0.0
 */
enum class RewardType {
    /**
     * 脚本奖励
     * 执行Kether脚本或命令
     */
    SCRIPT,
    
    /**
     * 金钱奖励
     * 通过经济插件给予金钱
     */
    MONEY,
    
    /**
     * 物品奖励
     * 给予指定的游戏物品
     */
    ITEM,
    
    /**
     * 经验奖励
     * 给予经验值或经验等级
     */
    EXPERIENCE,
    
    /**
     * 权限奖励
     * 临时或永久权限授予
     */
    PERMISSION,
    
    /**
     * 命令奖励
     * 执行指定的控制台命令
     */
    COMMAND
}