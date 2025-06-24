package online.bingzi.bilibili.video.pro.internal.database.dao

import com.j256.ormlite.dao.Dao
import online.bingzi.bilibili.video.pro.internal.database.DatabaseManager
import online.bingzi.bilibili.video.pro.internal.database.entity.TripleActionRecordEntity
import java.sql.SQLException

/**
 * 三连操作记录数据访问对象
 *
 * 提供三连操作记录相关的数据库操作方法
 *
 * @property databaseManager 数据库管理器
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
class TripleActionRecordDao(private val databaseManager: DatabaseManager) {

    /**
     * 获取ORMLite DAO实例
     */
    private val dao: Dao<TripleActionRecordEntity, Long>
        get() = databaseManager.getTripleActionDao()

    /**
     * 创建或更新记录
     *
     * @param entity 记录实体
     * @return 操作结果
     */
    fun createOrUpdate(entity: TripleActionRecordEntity): Boolean {
        return try {
            dao.createOrUpdate(entity)
            true
        } catch (e: SQLException) {
            false
        }
    }

    /**
     * 查询用户对特定视频的操作记录
     *
     * @param userUid 用户UID
     * @param videoBvid 视频BV号
     * @return 操作记录
     */
    fun findByUserAndVideo(userUid: String, videoBvid: String): TripleActionRecordEntity? {
        return try {
            val queryBuilder = dao.queryBuilder()
            queryBuilder.where()
                .eq("user_uid", userUid)
                .and()
                .eq("video_bvid", videoBvid)
            queryBuilder.orderBy("action_time", false)
            queryBuilder.queryForFirst()
        } catch (e: SQLException) {
            null
        }
    }

    /**
     * 查询用户的所有操作记录
     *
     * @param userUid 用户UID
     * @return 操作记录列表
     */
    fun findByUser(userUid: String): List<TripleActionRecordEntity> {
        return try {
            val queryBuilder = dao.queryBuilder()
            queryBuilder.where().eq("user_uid", userUid)
            queryBuilder.orderBy("action_time", false)
            queryBuilder.query()
        } catch (e: SQLException) {
            emptyList()
        }
    }

    /**
     * 查询视频的所有操作记录
     *
     * @param videoBvid 视频BV号
     * @return 操作记录列表
     */
    fun findByVideo(videoBvid: String): List<TripleActionRecordEntity> {
        return try {
            val queryBuilder = dao.queryBuilder()
            queryBuilder.where().eq("video_bvid", videoBvid)
            queryBuilder.orderBy("action_time", false)
            queryBuilder.query()
        } catch (e: SQLException) {
            emptyList()
        }
    }

    /**
     * 查询完整三连记录
     *
     * @return 完整三连记录列表
     */
    fun findFullTripleRecords(): List<TripleActionRecordEntity> {
        return try {
            val queryBuilder = dao.queryBuilder()
            queryBuilder.where()
                .eq("liked", 1)
                .and()
                .eq("coined", 1)
                .and()
                .eq("favorited", 1)
            queryBuilder.orderBy("action_time", false)
            queryBuilder.query()
        } catch (e: SQLException) {
            emptyList()
        }
    }

    /**
     * 查询未发放奖励的记录
     *
     * @return 未发放奖励的记录列表
     */
    fun findUnrewardedRecords(): List<TripleActionRecordEntity> {
        return try {
            val queryBuilder = dao.queryBuilder()
            queryBuilder.where().eq("reward_status", 0)
            queryBuilder.orderBy("action_time", true)
            queryBuilder.query()
        } catch (e: SQLException) {
            emptyList()
        }
    }

    /**
     * 统计记录数量
     *
     * @return 记录总数
     */
    fun count(): Long {
        return try {
            dao.countOf()
        } catch (e: SQLException) {
            0L
        }
    }

    /**
     * 统计完整三连数量
     *
     * @return 完整三连数量
     */
    fun countFullTriple(): Long {
        return try {
            val queryBuilder = dao.queryBuilder()
            queryBuilder.where()
                .eq("liked", 1)
                .and()
                .eq("coined", 1)
                .and()
                .eq("favorited", 1)
            queryBuilder.countOf()
        } catch (e: SQLException) {
            0L
        }
    }
} 