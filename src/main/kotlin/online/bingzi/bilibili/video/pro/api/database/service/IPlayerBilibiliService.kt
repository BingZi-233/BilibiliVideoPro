
package online.bingzi.bilibili.video.pro.api.database.service

import online.bingzi.bilibili.video.pro.internal.database.entity.PlayerBilibili

/**
 * Player bilibili service
 * 玩家 Bilibili 服务
 *
 * @constructor Create empty Player bilibili service
 */
interface IPlayerBilibiliService {
    /**
     * Find by player uuid
     * 通过玩家 UUID 查找
     *
     * @param uuid 玩家 UUID
     * @return 玩家 Bilibili 实体
     */
    fun findByPlayerUuid(uuid: String): PlayerBilibili?

    /**
     * Find by bilibili user id
     * 通过 Bilibili 用户 ID 查找
     *
     * @param userId Bilibili 用户 ID
     * @return 玩家 Bilibili 实体
     */
    fun findByBilibiliUserId(userId: Long): PlayerBilibili?

    /**
     * Create binding
     * 创建绑定
     *
     * @param playerUuid 玩家 UUID
     * @param playerName 玩家名称
     * @param bilibiliUserId Bilibili 用户 ID
     * @param bilibiliUsername Bilibili 用户名
     * @param cookie Bilibili Cookie
     * @return 玩家 Bilibili 实体
     */
    fun createBinding(playerUuid: String, playerName: String, bilibiliUserId: Long, bilibiliUsername: String, cookie: String): PlayerBilibili

    /**
     * Update cookie
     * 更新 Cookie
     *
     * @param uuid 玩家 UUID
     * @param cookie Bilibili Cookie
     */
    fun updateCookie(uuid: String, cookie: String)

    /**
     * Delete binding
     * 删除绑定
     *
     * @param uuid 玩家 UUID
     */
    fun deleteBinding(uuid: String)
}
