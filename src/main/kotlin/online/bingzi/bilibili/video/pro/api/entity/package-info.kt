/**
 * Bilibili视频一键三连奖励系统实体包
 *
 * 本包包含了Bilibili插件的核心数据实体，用于表示用户、视频、操作数据等信息。
 * 所有实体都采用不可变设计，确保数据一致性和线程安全性。
 *
 * ## 主要实体
 *
 * ### [BilibiliUser][online.bingzi.bilibili.video.pro.api.entity.BilibiliUser]
 * 表示Bilibili用户的基本信息，包括：
 * - UID和昵称
 * - Minecraft账号绑定状态
 * - 绑定时间记录
 *
 * ### [BilibiliVideo][online.bingzi.bilibili.video.pro.api.entity.BilibiliVideo]
 * 表示Bilibili视频信息，包括：
 * - AV号和BV号
 * - 视频标题和作者信息
 * - 上传时间
 *
 * ### [TripleActionData][online.bingzi.bilibili.video.pro.api.entity.TripleActionData]
 * 表示用户的三连操作数据，包括：
 * - 点赞、投币、收藏状态
 * - 投币数量详情
 * - 操作时间记录
 * - 得分计算逻辑
 *
 * ## 设计特点
 *
 * 1. **数据类设计**: 使用Kotlin data class，自动生成equals、hashCode、toString等方法
 * 2. **不可变性**: 所有属性都是val，确保数据一致性
 * 3. **工具方法**: 提供丰富的工具方法，方便业务逻辑使用
 * 4. **类型安全**: 合理使用可空类型，避免NPE问题
 * 5. **文档完整**: 详细的KDoc文档，包含使用示例
 *
 * ## 使用示例
 *
 * ```kotlin
 * // 创建用户
 * val user = BilibiliUser(
 *     uid = "123456789",
 *     nickname = "测试用户",
 *     minecraftUuid = player.uniqueId
 * )
 *
 * // 创建视频
 * val video = BilibiliVideo(
 *     aid = 1234567890L,
 *     bvid = "BV1xx411c7mD",
 *     title = "测试视频",
 *     authorUid = "987654321",
 *     authorName = "UP主"
 * )
 *
 * // 创建三连数据
 * val actionData = TripleActionData(
 *     liked = true,
 *     coined = true,
 *     coinCount = 2,
 *     favorited = true
 * )
 *
 * // 检查操作状态
 * if (actionData.isFullTriple()) {
 *     println("完成完整三连，得分：${actionData.getScore()}")
 * }
 * ```
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
package online.bingzi.bilibili.video.pro.api.entity 