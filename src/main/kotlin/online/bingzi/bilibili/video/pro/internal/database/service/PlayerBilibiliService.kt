package online.bingzi.bilibili.video.pro.internal.database.service

import com.j256.ormlite.dao.Dao
import online.bingzi.bilibili.video.pro.api.database.service.IPlayerBilibiliService
import online.bingzi.bilibili.video.pro.internal.database.entity.PlayerBilibili
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

import taboolib.common.platform.service.PlatformExecutor

/**
 * Player bilibili service impl
 * 玩家 Bilibili 服务实现
 *
 * @constructor Create empty Player bilibili service impl
 */
class PlayerBilibiliServiceImpl : IPlayerBilibiliService {

    
    lateinit var playerBilibiliDao: Dao<PlayerBilibili, Long>

    override fun findByPlayerUuid(uuid: String): PlayerBilibili? {
        return playerBilibiliDao.queryForEq("player_uuid", uuid).firstOrNull()
    }

    override fun findByBilibiliUserId(userId: Long): PlayerBilibili? {
        return playerBilibiliDao.queryForEq("bilibili_uid", userId).firstOrNull()
    }

    override fun createBinding(playerUuid: String, playerName: String, bilibiliUserId: Long, bilibiliUsername: String, sessdata: String, biliJct: String, dedeUserId: String, dedeUserIdMd5: String): PlayerBilibili {
        val playerBilibili = PlayerBilibili(
            playerUuid,
            playerName,
            bilibiliUserId,
            bilibiliUsername,
            sessdata,
            biliJct,
            dedeUserId,
            dedeUserIdMd5
        )
        playerBilibiliDao.create(playerBilibili)
        return playerBilibili
    }

    override fun updateCookie(uuid: String, sessdata: String, biliJct: String, dedeUserId: String, dedeUserIdMd5: String) {
        val playerBilibili = findByPlayerUuid(uuid) ?: return
        playerBilibili.updateCookies(sessdata, biliJct, dedeUserId, dedeUserIdMd5)
        playerBilibiliDao.update(playerBilibili)
    }

    override fun deleteBinding(uuid: String) {
        val playerBilibili = findByPlayerUuid(uuid) ?: return
        playerBilibiliDao.delete(playerBilibili)
    }

    @Awake(LifeCycle.ENABLE)
    companion object {
        
        
    }
}