package online.bingzi.bilibili.video.pro.internal.database.service

import com.j256.ormlite.dao.Dao
import online.bingzi.bilibili.video.pro.api.database.service.IPlayerBilibiliService
import online.bingzi.bilibili.video.pro.internal.database.entity.PlayerBilibili
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Inject
import taboolib.common.platform.Instance

/**
 * Player bilibili service impl
 * 玩家 Bilibili 服务实现
 *
 * @constructor Create empty Player bilibili service impl
 */
@Instance
class PlayerBilibiliServiceImpl : IPlayerBilibiliService {

    @Inject
    lateinit var playerBilibiliDao: Dao<PlayerBilibili, Long>

    override fun findByPlayerUuid(uuid: String): PlayerBilibili? {
        return playerBilibiliDao.queryForEq("player_uuid", uuid).firstOrNull()
    }

    override fun findByBilibiliUserId(userId: Long): PlayerBilibili? {
        return playerBilibiliDao.queryForEq("bilibili_uid", userId).firstOrNull()
    }

    override fun createBinding(playerUuid: String, playerName: String, bilibiliUserId: Long, bilibiliUsername: String, cookie: String): PlayerBilibili {
        val playerBilibili = PlayerBilibili(
            playerUuid = playerUuid,
            playerName = playerName,
            bilibiliUid = bilibiliUserId,
            bilibiliUsername = bilibiliUsername,
            cookie = cookie
        )
        playerBilibiliDao.create(playerBilibili)
        return playerBilibili
    }

    override fun updateCookie(uuid: String, cookie: String) {
        val playerBilibili = findByPlayerUuid(uuid) ?: return
        playerBilibili.cookie = cookie
        playerBilibiliDao.update(playerBilibili)
    }

    override fun deleteBinding(uuid: String) {
        val playerBilibili = findByPlayerUuid(uuid) ?: return
        playerBilibiliDao.delete(playerBilibili)
    }

    companion object {
        @Awake(LifeCycle.ENABLE)
        fun init() {
            // 注册服务
            IPlayerBilibiliService::class.java.let { it.getConstructor() }
        }
    }
}