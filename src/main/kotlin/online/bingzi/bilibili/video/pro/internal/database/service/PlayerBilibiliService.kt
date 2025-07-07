package online.bingzi.bilibili.video.pro.internal.database.service

import com.j256.ormlite.dao.Dao
import online.bingzi.bilibili.video.pro.internal.database.entity.PlayerBilibili
import online.bingzi.bilibili.video.pro.internal.database.transaction.SimpleTransactionManager
import online.bingzi.bilibili.video.pro.internal.error.ErrorHandler
import online.bingzi.bilibili.video.pro.internal.validation.InputValidator
import taboolib.common.platform.function.console
import taboolib.module.lang.sendInfo

/**
 * Player bilibili service impl
 * 玩家 Bilibili 服务实现
 *
 * @constructor Create empty Player bilibili service impl
 */
object PlayerBilibiliService {


    lateinit var playerBilibiliDao: Dao<PlayerBilibili, Long>

    /**
     * 根据玩家UUID查找绑定记录
     */
    fun findByPlayerUuid(uuid: String): PlayerBilibili? {
        return try {
            // 验证输入参数
            val validation = InputValidator.validatePlayerUuid(uuid)
            if (!validation.isValid) {
                console().sendInfo("uuidValidationFailed", validation.errorMessage ?: "unknown")
                return null
            }

            playerBilibiliDao.queryForEq("player_uuid", uuid).firstOrNull()
        } catch (e: Exception) {
            ErrorHandler.handleError(
                type = ErrorHandler.ErrorType.DATABASE,
                component = "PlayerBilibiliService",
                operation = "findByPlayerUuid",
                exception = e,
                metadata = mapOf("uuid" to uuid)
            )
            null
        }
    }

    /**
     * 根据Bilibili用户ID查找绑定记录
     */
    fun findByBilibiliUserId(userId: Long): PlayerBilibili? {
        return try {
            // 验证输入参数
            val validation = InputValidator.validateBilibiliUid(userId)
            if (!validation.isValid) {
                console().sendInfo("bilibiliUidValidationFailed", validation.errorMessage ?: "unknown")
                return null
            }

            playerBilibiliDao.queryForEq("bilibili_uid", userId).firstOrNull()
        } catch (e: Exception) {
            ErrorHandler.handleError(
                type = ErrorHandler.ErrorType.DATABASE,
                component = "PlayerBilibiliService",
                operation = "findByBilibiliUserId",
                exception = e,
                metadata = mapOf("userId" to userId.toString())
            )
            null
        }
    }

    /**
     * 创建绑定记录（使用事务）
     */
    fun createBinding(
        playerUuid: String,
        playerName: String,
        bilibiliUserId: Long,
        bilibiliUsername: String,
        sessdata: String,
        biliJct: String,
        dedeUserId: String,
        dedeUserIdMd5: String
    ): PlayerBilibili? {
        return SimpleTransactionManager.executeTransaction {
            // 验证所有输入参数
            val validationResult = InputValidator.validateAll(
                { InputValidator.validatePlayerUuid(playerUuid) },
                { InputValidator.validateUsername(playerName) },
                { InputValidator.validateBilibiliUid(bilibiliUserId) },
                { InputValidator.validateUsername(bilibiliUsername) },
                { InputValidator.validateCookie("SESSDATA", sessdata) },
                { InputValidator.validateCookie("bili_jct", biliJct) },
                { InputValidator.validateCookie("DedeUserID", dedeUserId) },
                { InputValidator.validateCookie("DedeUserID__ckMd5", dedeUserIdMd5) }
            )

            if (!validationResult.isValid) {
                throw IllegalArgumentException("输入验证失败: ${validationResult.errorMessage}")
            }

            // 检查是否已经存在绑定
            val existingByUuid = findByPlayerUuid(playerUuid)
            if (existingByUuid != null) {
                throw IllegalStateException("玩家已绑定Bilibili账户")
            }

            val existingByUid = findByBilibiliUserId(bilibiliUserId)
            if (existingByUid != null) {
                throw IllegalStateException("该Bilibili账户已被其他玩家绑定")
            }

            // 创建新绑定
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
            console().sendInfo("playerBindingCreated", playerName, bilibiliUsername)

            playerBilibili
        }.let { result ->
            when (result) {
                is SimpleTransactionManager.TransactionResult.Success -> result.result
                is SimpleTransactionManager.TransactionResult.Failure -> {
                    ErrorHandler.handleError(
                        type = ErrorHandler.ErrorType.DATABASE,
                        component = "PlayerBilibiliService",
                        operation = "createBinding",
                        exception = result.exception,
                        metadata = mapOf(
                            "playerUuid" to playerUuid,
                            "playerName" to playerName,
                            "bilibiliUserId" to bilibiliUserId.toString(),
                            "bilibiliUsername" to bilibiliUsername
                        )
                    )
                    null
                }
            }
        }
    }

    fun updateCookie(uuid: String, sessdata: String, biliJct: String, dedeUserId: String, dedeUserIdMd5: String) {
        val playerBilibili = findByPlayerUuid(uuid) ?: return
        playerBilibili.updateCookies(sessdata, biliJct, dedeUserId, dedeUserIdMd5)
        playerBilibiliDao.update(playerBilibili)
    }

    fun deleteBinding(uuid: String) {
        val playerBilibili = findByPlayerUuid(uuid) ?: return
        playerBilibiliDao.delete(playerBilibili)
    }

    /**
     * 查找所有绑定的玩家
     */
    fun findAll(): List<PlayerBilibili> {
        return try {
            val queryBuilder = playerBilibiliDao.queryBuilder()
            queryBuilder.where().eq(PlayerBilibili.IS_ACTIVE, true)
            queryBuilder.query()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 获取总玩家数
     */
    fun getTotalPlayerCount(): Int {
        return try {
            val queryBuilder = playerBilibiliDao.queryBuilder()
            queryBuilder.where().eq(PlayerBilibili.IS_ACTIVE, true)
            queryBuilder.countOf().toInt()
        } catch (e: Exception) {
            0
        }
    }
}