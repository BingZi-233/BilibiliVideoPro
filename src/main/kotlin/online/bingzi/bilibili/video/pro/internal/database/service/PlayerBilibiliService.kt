package online.bingzi.bilibili.video.pro.internal.database.service

import com.j256.ormlite.stmt.QueryBuilder
import online.bingzi.bilibili.video.pro.internal.database.DatabaseManager
import online.bingzi.bilibili.video.pro.internal.database.entity.PlayerBilibili
import online.bingzi.bilibili.video.pro.internal.network.BilibiliNetworkManager
import online.bingzi.bilibili.video.pro.internal.network.auth.LoginStatusResult
import online.bingzi.bilibili.video.pro.internal.network.auth.LoginUserInfo
import java.sql.SQLException
import java.util.*

/**
 * 玩家Bilibili绑定服务
 * 提供玩家与Bilibili账户绑定相关的业务逻辑
 */
class PlayerBilibiliService {
    
    private val dao = DatabaseManager.getPlayerBilibiliDao()
    private val networkManager = BilibiliNetworkManager.getInstance()
    
    /**
     * 根据玩家UUID查找绑定记录
     */
    fun findByPlayerUuid(playerUuid: String): PlayerBilibili? {
        return try {
            val queryBuilder: QueryBuilder<PlayerBilibili, Long> = dao.queryBuilder()
            queryBuilder.where()
                .eq(PlayerBilibili.FIELD_PLAYER_UUID, playerUuid)
                .and()
                .eq(PlayerBilibili.FIELD_IS_ACTIVE, true)
            
            queryBuilder.queryForFirst()
        } catch (e: SQLException) {
            throw DatabaseServiceException("查询玩家绑定记录失败", e)
        }
    }
    
    /**
     * 根据Bilibili UID查找绑定记录
     */
    fun findByBilibiliUid(bilibiliUid: Long): PlayerBilibili? {
        return try {
            val queryBuilder: QueryBuilder<PlayerBilibili, Long> = dao.queryBuilder()
            queryBuilder.where()
                .eq(PlayerBilibili.FIELD_BILIBILI_UID, bilibiliUid)
                .and()
                .eq(PlayerBilibili.FIELD_IS_ACTIVE, true)
            
            queryBuilder.queryForFirst()
        } catch (e: SQLException) {
            throw DatabaseServiceException("查询Bilibili绑定记录失败", e)
        }
    }
    
    /**
     * 创建新的绑定记录
     */
    fun createBinding(
        playerUuid: String,
        playerName: String,
        userInfo: LoginUserInfo,
        cookies: Map<String, String>
    ): BindingResult {
        return try {
            // 检查玩家是否已经绑定
            val existingPlayer = findByPlayerUuid(playerUuid)
            if (existingPlayer != null) {
                return BindingResult.AlreadyBound("玩家已绑定Bilibili账户: ${existingPlayer.bilibiliUsername}")
            }
            
            // 检查Bilibili账户是否已被其他玩家绑定
            val existingBilibili = findByBilibiliUid(userInfo.mid)
            if (existingBilibili != null) {
                return BindingResult.AccountOccupied("该Bilibili账户已被其他玩家绑定: ${existingBilibili.playerName}")
            }
            
            // 验证Cookie完整性
            val requiredCookies = listOf("SESSDATA", "bili_jct", "DedeUserID", "DedeUserID__ckMd5")
            val missingCookies = requiredCookies.filter { !cookies.containsKey(it) || cookies[it].isNullOrEmpty() }
            if (missingCookies.isNotEmpty()) {
                return BindingResult.InvalidCookies("缺少必要的Cookie: ${missingCookies.joinToString(", ")}")
            }
            
            // 创建新的绑定记录
            val playerBilibili = PlayerBilibili(
                playerUuid = playerUuid,
                playerName = playerName,
                bilibiliUid = userInfo.mid,
                bilibiliUsername = userInfo.username,
                sessdata = cookies["SESSDATA"]!!,
                biliJct = cookies["bili_jct"]!!,
                dedeUserId = cookies["DedeUserID"]!!,
                dedeUserIdMd5 = cookies["DedeUserID__ckMd5"]!!
            )
            
            // 更新Bilibili用户信息
            playerBilibili.updateBilibiliInfo(
                username = userInfo.username,
                face = userInfo.face,
                level = userInfo.level,
                isVip = userInfo.isVip
            )
            
            dao.create(playerBilibili)
            
            BindingResult.Success("绑定成功", playerBilibili)
            
        } catch (e: SQLException) {
            throw DatabaseServiceException("创建绑定记录失败", e)
        }
    }
    
    /**
     * 更新绑定记录的Cookie
     */
    fun updateCookies(playerUuid: String, cookies: Map<String, String>): Boolean {
        return try {
            val playerBilibili = findByPlayerUuid(playerUuid)
                ?: throw IllegalArgumentException("未找到玩家的绑定记录")
            
            // 验证Cookie完整性
            val requiredCookies = listOf("SESSDATA", "bili_jct", "DedeUserID", "DedeUserID__ckMd5")
            val missingCookies = requiredCookies.filter { !cookies.containsKey(it) || cookies[it].isNullOrEmpty() }
            if (missingCookies.isNotEmpty()) {
                return false
            }
            
            playerBilibili.updateCookies(
                sessdata = cookies["SESSDATA"]!!,
                biliJct = cookies["bili_jct"]!!,
                dedeUserId = cookies["DedeUserID"]!!,
                dedeUserIdMd5 = cookies["DedeUserID__ckMd5"]!!
            )
            
            dao.update(playerBilibili)
            true
            
        } catch (e: SQLException) {
            throw DatabaseServiceException("更新Cookie失败", e)
        }
    }
    
    /**
     * 更新Bilibili用户信息
     */
    fun updateBilibiliInfo(playerUuid: String, userInfo: LoginUserInfo): Boolean {
        return try {
            val playerBilibili = findByPlayerUuid(playerUuid)
                ?: throw IllegalArgumentException("未找到玩家的绑定记录")
            
            playerBilibili.updateBilibiliInfo(
                username = userInfo.username,
                face = userInfo.face,
                level = userInfo.level,
                isVip = userInfo.isVip
            )
            
            dao.update(playerBilibili)
            true
            
        } catch (e: SQLException) {
            throw DatabaseServiceException("更新Bilibili用户信息失败", e)
        }
    }
    
    /**
     * 刷新玩家的登录状态和用户信息
     */
    fun refreshPlayerStatus(playerUuid: String): RefreshResult {
        return try {
            val playerBilibili = findByPlayerUuid(playerUuid)
                ?: return RefreshResult.NotBound("玩家未绑定Bilibili账户")
            
            // 设置网络管理器的Cookie
            networkManager.setCookies(playerBilibili.getCookieMap())
            
            // 检查登录状态
            when (val loginResult = networkManager.cookieRefresh.checkLoginStatus()) {
                is LoginStatusResult.Success -> {
                    // 更新用户信息
                    updateBilibiliInfo(playerUuid, loginResult.userInfo)
                    RefreshResult.Success("登录状态刷新成功", loginResult.userInfo)
                }
                is LoginStatusResult.NotLoggedIn -> {
                    RefreshResult.LoginExpired("登录状态已过期: ${loginResult.message}")
                }
                is LoginStatusResult.Error -> {
                    RefreshResult.Error("检查登录状态失败: ${loginResult.message}")
                }
            }
            
        } catch (e: Exception) {
            RefreshResult.Error("刷新状态时发生异常: ${e.message}")
        }
    }
    
    /**
     * 解除绑定（软删除）
     */
    fun unbind(playerUuid: String): Boolean {
        return try {
            val playerBilibili = findByPlayerUuid(playerUuid)
                ?: throw IllegalArgumentException("未找到玩家的绑定记录")
            
            playerBilibili.softDelete()
            dao.update(playerBilibili)
            true
            
        } catch (e: SQLException) {
            throw DatabaseServiceException("解除绑定失败", e)
        }
    }
    
    /**
     * 获取所有活跃的绑定记录
     */
    fun getAllActiveBindings(): List<PlayerBilibili> {
        return try {
            val queryBuilder: QueryBuilder<PlayerBilibili, Long> = dao.queryBuilder()
            queryBuilder.where().eq(PlayerBilibili.FIELD_IS_ACTIVE, true)
            queryBuilder.orderBy(PlayerBilibili.FIELD_CREATED_TIME, false)
            
            queryBuilder.query()
        } catch (e: SQLException) {
            throw DatabaseServiceException("查询所有绑定记录失败", e)
        }
    }
    
    /**
     * 获取绑定统计信息
     */
    fun getBindingStatistics(): BindingStatistics {
        return try {
            val totalBindings = dao.queryBuilder()
                .where().eq(PlayerBilibili.FIELD_IS_ACTIVE, true)
                .countOf()
            
            val vipCount = dao.queryBuilder()
                .where()
                .eq(PlayerBilibili.FIELD_IS_ACTIVE, true)
                .and()
                .eq(PlayerBilibili.FIELD_IS_VIP, true)
                .countOf()
            
            val recentBindings = dao.queryBuilder()
                .where()
                .eq(PlayerBilibili.FIELD_IS_ACTIVE, true)
                .and()
                .ge(PlayerBilibili.FIELD_CREATED_TIME, Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000))
                .countOf()
            
            BindingStatistics(
                totalBindings = totalBindings,
                vipCount = vipCount,
                recentBindings = recentBindings
            )
            
        } catch (e: SQLException) {
            throw DatabaseServiceException("获取绑定统计信息失败", e)
        }
    }
    
    /**
     * 清理过期的登录状态
     */
    fun cleanupExpiredLogins(): Int {
        // 这里可以实现清理逻辑，比如检查长时间未登录的账户
        // 暂时返回0，表示没有清理任何记录
        return 0
    }
}

/**
 * 绑定结果
 */
sealed class BindingResult {
    data class Success(val message: String, val playerBilibili: PlayerBilibili) : BindingResult()
    data class AlreadyBound(val message: String) : BindingResult()
    data class AccountOccupied(val message: String) : BindingResult()
    data class InvalidCookies(val message: String) : BindingResult()
    data class Error(val message: String) : BindingResult()
}

/**
 * 刷新结果
 */
sealed class RefreshResult {
    data class Success(val message: String, val userInfo: LoginUserInfo) : RefreshResult()
    data class NotBound(val message: String) : RefreshResult()
    data class LoginExpired(val message: String) : RefreshResult()
    data class Error(val message: String) : RefreshResult()
}

/**
 * 绑定统计信息
 */
data class BindingStatistics(
    val totalBindings: Long,
    val vipCount: Long,
    val recentBindings: Long
)

/**
 * 数据库服务异常
 */
class DatabaseServiceException(message: String, cause: Throwable? = null) : Exception(message, cause) 