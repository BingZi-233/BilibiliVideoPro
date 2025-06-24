/**
 * Bilibili视频一键三连奖励系统事件包
 *
 * 本包包含了Bilibili插件的完整事件系统，用于处理用户的三连操作、奖励发放、
 * 连续操作追踪等功能。所有事件都遵循Bukkit事件系统规范，可以被其他插件监听和处理。
 *
 * ## 主要组件
 *
 * ### 实体类 (Entity)
 * - [BilibiliUser][online.bingzi.bilibili.video.pro.api.entity.BilibiliUser] - Bilibili用户信息
 * - [BilibiliVideo][online.bingzi.bilibili.video.pro.api.entity.BilibiliVideo] - Bilibili视频信息
 * - [TripleActionData][online.bingzi.bilibili.video.pro.api.entity.TripleActionData] - 三连操作数据
 *
 * ### 事件类 (Event)
 * - [BilibiliEvent][online.bingzi.bilibili.video.pro.api.event.BilibiliEvent] - 所有Bilibili事件的基类
 * - [PlayerBindBilibiliEvent][online.bingzi.bilibili.video.pro.api.event.PlayerBindBilibiliEvent] - 玩家绑定Bilibili账号事件
 * - [BilibiliTripleActionEvent][online.bingzi.bilibili.video.pro.api.event.BilibiliTripleActionEvent] - 三连操作事件
 * - [BilibiliRewardEvent][online.bingzi.bilibili.video.pro.api.event.BilibiliRewardEvent] - 奖励发放事件
 * - [BilibiliVideoDetectEvent][online.bingzi.bilibili.video.pro.api.event.BilibiliVideoDetectEvent] - 视频检测事件
 * - [BilibiliStreakEvent][online.bingzi.bilibili.video.pro.api.event.BilibiliStreakEvent] - 连续操作事件
 *
 * ## 使用示例
 *
 * ### 监听三连操作事件
 * ```kotlin
 * @EventHandler
 * fun onTripleAction(event: BilibiliTripleActionEvent) {
 *     if (event.isJustCompletedFullTriple()) {
 *         // 处理完整三连
 *         val player = event.user.minecraftUuid?.let { Bukkit.getPlayer(it) }
 *         player?.sendMessage("恭喜完成三连！")
 *     }
 * }
 * ```
 *
 * ### 监听奖励事件
 * ```kotlin
 * @EventHandler
 * fun onReward(event: BilibiliRewardEvent) {
 *     when (event.rewardType) {
 *         BilibiliRewardEvent.RewardType.FULL_TRIPLE_REWARD -> {
 *             // 处理完整三连奖励
 *             giveExtraReward(event.player)
 *         }
 *         else -> {
 *             // 处理其他奖励
 *         }
 *     }
 * }
 * ```
 *
 * ### 监听连续操作事件
 * ```kotlin
 * @EventHandler
 * fun onStreak(event: BilibiliStreakEvent) {
 *     if (event.isMilestone()) {
 *         // 连续操作达到里程碑
 *         val player = event.user.minecraftUuid?.let { Bukkit.getPlayer(it) }
 *         player?.sendMessage("连续操作${event.streakCount}次！")
 *     }
 * }
 * ```
 *
 * ## 设计原则
 *
 * 1. **最小化信息原则**: 事件只包含必要的信息，避免冗余数据
 * 2. **扩展性**: 提供丰富的工具方法，支持各种业务场景
 * 3. **类型安全**: 使用枚举和数据类确保类型安全
 * 4. **文档完整**: 每个类和方法都有详细的KDoc文档
 * 5. **性能优化**: 事件数据结构轻量级，适合高频触发
 *
 * ## Bilibili API集成
 *
 * 本事件系统基于Bilibili API文档设计，支持以下功能：
 * - 用户三连操作检测（点赞、投币、收藏）
 * - 视频信息获取和缓存
 * - 用户行为分析和连续操作追踪
 * - 多种奖励类型和自定义奖励逻辑
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 * @see <a href="https://socialsisteryi.github.io/bilibili-API-collect/">Bilibili API文档</a>
 */
package online.bingzi.bilibili.video.pro.api.event 