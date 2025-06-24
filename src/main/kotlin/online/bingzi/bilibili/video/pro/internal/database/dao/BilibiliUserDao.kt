package online.bingzi.bilibili.video.pro.internal.database.dao

import com.j256.ormlite.dao.Dao
import online.bingzi.bilibili.video.pro.internal.database.DatabaseManager
import online.bingzi.bilibili.video.pro.internal.database.entity.BilibiliUserEntity
import java.sql.SQLException
import java.util.*

/**
 * Bilibili用户数据访问对象
 *
 * 提供用户相关的数据库操作方法，包括CRUD操作和复杂查询
 *
 * @property databaseManager 数据库管理器
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
class BilibiliUserDao(private val databaseManager: DatabaseManager) {

    /**
     * 获取ORMLite DAO实例
     */
    private val dao: Dao<BilibiliUserEntity, Long>
        get() = databaseManager.getUserDao()

    /**
     * 创建或更新用户
     *
     * @param entity 用户实体
     * @return 操作结果，true表示成功
     */
    fun createOrUpdate(entity: BilibiliUserEntity): Boolean {
        return try {
            dao.createOrUpdate(entity)
            true
        } catch (e: SQLException) {
            false
        }
    }

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户实体，不存在时返回null
     */
    fun findById(id: Long): BilibiliUserEntity? {
        return try {
            dao.queryForId(id)
        } catch (e: SQLException) {
            null
        }
    }

    /**
     * 根据UID查询用户
     *
     * @param uid Bilibili用户UID
     * @return 用户实体，不存在时返回null
     */
    fun findByUid(uid: String): BilibiliUserEntity? {
        return try {
            val queryBuilder = dao.queryBuilder()
            queryBuilder.where().eq("uid", uid)
            queryBuilder.queryForFirst()
        } catch (e: SQLException) {
            null
        }
    }

    /**
     * 根据Minecraft UUID查询用户
     *
     * @param minecraftUuid Minecraft玩家UUID
     * @return 用户实体，不存在时返回null
     */
    fun findByMinecraftUuid(minecraftUuid: UUID): BilibiliUserEntity? {
        return try {
            val queryBuilder = dao.queryBuilder()
            queryBuilder.where().eq("minecraft_uuid", minecraftUuid.toString())
            queryBuilder.queryForFirst()
        } catch (e: SQLException) {
            null
        }
    }

    /**
     * 查询所有用户
     *
     * @return 用户列表
     */
    fun findAll(): List<BilibiliUserEntity> {
        return try {
            dao.queryForAll()
        } catch (e: SQLException) {
            emptyList()
        }
    }

    /**
     * 查询活跃用户
     *
     * @return 活跃用户列表
     */
    fun findActiveUsers(): List<BilibiliUserEntity> {
        return try {
            val queryBuilder = dao.queryBuilder()
            queryBuilder.where().eq("status", 1)
            queryBuilder.query()
        } catch (e: SQLException) {
            emptyList()
        }
    }

    /**
     * 查询已绑定Minecraft的用户
     *
     * @return 已绑定用户列表
     */
    fun findBoundUsers(): List<BilibiliUserEntity> {
        return try {
            val queryBuilder = dao.queryBuilder()
            queryBuilder.where().isNotNull("minecraft_uuid")
            queryBuilder.query()
        } catch (e: SQLException) {
            emptyList()
        }
    }

    /**
     * 查询未绑定Minecraft的用户
     *
     * @return 未绑定用户列表
     */
    fun findUnboundUsers(): List<BilibiliUserEntity> {
        return try {
            val queryBuilder = dao.queryBuilder()
            queryBuilder.where().isNull("minecraft_uuid")
            queryBuilder.query()
        } catch (e: SQLException) {
            emptyList()
        }
    }

    /**
     * 根据昵称模糊搜索用户
     *
     * @param nickname 昵称关键词
     * @return 匹配的用户列表
     */
    fun searchByNickname(nickname: String): List<BilibiliUserEntity> {
        return try {
            val queryBuilder = dao.queryBuilder()
            queryBuilder.where().like("nickname", "%$nickname%")
            queryBuilder.query()
        } catch (e: SQLException) {
            emptyList()
        }
    }

    /**
     * 查询最近绑定的用户
     *
     * @param days 天数（默认7天）
     * @return 最近绑定的用户列表
     */
    fun findRecentlyBoundUsers(days: Int = 7): List<BilibiliUserEntity> {
        return try {
            val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
            val queryBuilder = dao.queryBuilder()
            queryBuilder.where().ge("bind_time", cutoffTime)
            queryBuilder.orderBy("bind_time", false) // 降序排列
            queryBuilder.query()
        } catch (e: SQLException) {
            emptyList()
        }
    }

    /**
     * 分页查询用户
     *
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 用户列表
     */
    fun findByPage(page: Int, pageSize: Int): List<BilibiliUserEntity> {
        return try {
            val offset = (page - 1) * pageSize
            val queryBuilder = dao.queryBuilder()
            queryBuilder.orderBy("create_time", false) // 按创建时间降序
            queryBuilder.offset(offset.toLong())
            queryBuilder.limit(pageSize.toLong())
            queryBuilder.query()
        } catch (e: SQLException) {
            emptyList()
        }
    }

    /**
     * 统计用户数量
     *
     * @return 用户总数
     */
    fun count(): Long {
        return try {
            dao.countOf()
        } catch (e: SQLException) {
            0L
        }
    }

    /**
     * 统计活跃用户数量
     *
     * @return 活跃用户数
     */
    fun countActiveUsers(): Long {
        return try {
            val queryBuilder = dao.queryBuilder()
            queryBuilder.where().eq("status", 1)
            queryBuilder.countOf()
        } catch (e: SQLException) {
            0L
        }
    }

    /**
     * 统计绑定用户数量
     *
     * @return 绑定用户数
     */
    fun countBoundUsers(): Long {
        return try {
            val queryBuilder = dao.queryBuilder()
            queryBuilder.where().isNotNull("minecraft_uuid")
            queryBuilder.countOf()
        } catch (e: SQLException) {
            0L
        }
    }

    /**
     * 更新用户状态
     *
     * @param uid 用户UID
     * @param active 是否激活
     * @return 操作结果
     */
    fun updateStatus(uid: String, active: Boolean): Boolean {
        return try {
            val user = findByUid(uid)
            if (user != null) {
                user.setActive(active)
                dao.update(user) == 1
            } else {
                false
            }
        } catch (e: SQLException) {
            false
        }
    }

    /**
     * 绑定Minecraft账号
     *
     * @param uid Bilibili用户UID
     * @param minecraftUuid Minecraft玩家UUID
     * @return 操作结果
     */
    fun bindMinecraft(uid: String, minecraftUuid: UUID): Boolean {
        return try {
            val user = findByUid(uid)
            if (user != null) {
                user.setMinecraftUUID(minecraftUuid)
                dao.update(user) == 1
            } else {
                false
            }
        } catch (e: SQLException) {
            false
        }
    }

    /**
     * 解绑Minecraft账号
     *
     * @param uid Bilibili用户UID
     * @return 操作结果
     */
    fun unbindMinecraft(uid: String): Boolean {
        return try {
            val user = findByUid(uid)
            if (user != null) {
                user.setMinecraftUUID(null)
                dao.update(user) == 1
            } else {
                false
            }
        } catch (e: SQLException) {
            false
        }
    }

    /**
     * 更新用户信息
     *
     * @param uid 用户UID
     * @param nickname 新昵称
     * @param level 用户等级
     * @param avatarUrl 头像URL
     * @return 操作结果
     */
    fun updateUserInfo(uid: String, nickname: String? = null, level: Int? = null, avatarUrl: String? = null): Boolean {
        return try {
            val user = findByUid(uid)
            if (user != null) {
                nickname?.let { user.nickname = it }
                level?.let { user.level = it }
                avatarUrl?.let { user.avatarUrl = it }
                user.updateLastUpdateTime()
                dao.update(user) == 1
            } else {
                false
            }
        } catch (e: SQLException) {
            false
        }
    }

    /**
     * 删除用户
     *
     * @param uid 用户UID
     * @return 操作结果
     */
    fun delete(uid: String): Boolean {
        return try {
            val user = findByUid(uid)
            if (user != null) {
                dao.delete(user) == 1
            } else {
                false
            }
        } catch (e: SQLException) {
            false
        }
    }

    /**
     * 批量删除用户
     *
     * @param uids 用户UID列表
     * @return 删除的用户数量
     */
    fun batchDelete(uids: List<String>): Int {
        return try {
            var deletedCount = 0
            for (uid in uids) {
                if (delete(uid)) {
                    deletedCount++
                }
            }
            deletedCount
        } catch (e: SQLException) {
            0
        }
    }

    /**
     * 检查用户是否存在
     *
     * @param uid 用户UID
     * @return 是否存在
     */
    fun exists(uid: String): Boolean {
        return findByUid(uid) != null
    }

    /**
     * 检查Minecraft UUID是否已绑定
     *
     * @param minecraftUuid Minecraft玩家UUID
     * @return 是否已绑定
     */
    fun isMinecraftUuidBound(minecraftUuid: UUID): Boolean {
        return findByMinecraftUuid(minecraftUuid) != null
    }

    /**
     * 获取用户统计信息
     *
     * @return 统计信息
     */
    fun getUserStats(): UserStats {
        return try {
            val totalUsers = count()
            val activeUsers = countActiveUsers()
            val boundUsers = countBoundUsers()
            val recentUsers = findRecentlyBoundUsers(7).size.toLong()

            UserStats(
                totalUsers = totalUsers,
                activeUsers = activeUsers,
                inactiveUsers = totalUsers - activeUsers,
                boundUsers = boundUsers,
                unboundUsers = totalUsers - boundUsers,
                recentlyBoundUsers = recentUsers
            )
        } catch (e: SQLException) {
            UserStats()
        }
    }

    /**
     * 用户统计信息
     *
     * @property totalUsers 总用户数
     * @property activeUsers 活跃用户数
     * @property inactiveUsers 非活跃用户数
     * @property boundUsers 已绑定用户数
     * @property unboundUsers 未绑定用户数
     * @property recentlyBoundUsers 最近绑定用户数
     */
    data class UserStats(
        val totalUsers: Long = 0,
        val activeUsers: Long = 0,
        val inactiveUsers: Long = 0,
        val boundUsers: Long = 0,
        val unboundUsers: Long = 0,
        val recentlyBoundUsers: Long = 0
    )
}