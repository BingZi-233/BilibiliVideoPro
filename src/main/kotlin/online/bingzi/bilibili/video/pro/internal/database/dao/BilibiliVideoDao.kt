package online.bingzi.bilibili.video.pro.internal.database.dao

import com.j256.ormlite.dao.Dao
import online.bingzi.bilibili.video.pro.internal.database.DatabaseManager
import online.bingzi.bilibili.video.pro.internal.database.entity.BilibiliVideoEntity
import java.sql.SQLException

/**
 * Bilibili视频数据访问对象
 *
 * 提供视频相关的数据库操作方法
 *
 * @property databaseManager 数据库管理器
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
class BilibiliVideoDao(private val databaseManager: DatabaseManager) {

    /**
     * 获取ORMLite DAO实例
     */
    private val dao: Dao<BilibiliVideoEntity, Long>
        get() = databaseManager.getVideoDao()

    /**
     * 创建或更新视频
     *
     * @param entity 视频实体
     * @return 操作结果
     */
    fun createOrUpdate(entity: BilibiliVideoEntity): Boolean {
        return try {
            dao.createOrUpdate(entity)
            true
        } catch (e: SQLException) {
            false
        }
    }

    /**
     * 根据BV号查询视频
     *
     * @param bvid 视频BV号
     * @return 视频实体
     */
    fun findByBvid(bvid: String): BilibiliVideoEntity? {
        return try {
            val queryBuilder = dao.queryBuilder()
            queryBuilder.where().eq("bvid", bvid)
            queryBuilder.queryForFirst()
        } catch (e: SQLException) {
            null
        }
    }

    /**
     * 根据AV号查询视频
     *
     * @param aid 视频AV号
     * @return 视频实体
     */
    fun findByAid(aid: Long): BilibiliVideoEntity? {
        return try {
            val queryBuilder = dao.queryBuilder()
            queryBuilder.where().eq("aid", aid)
            queryBuilder.queryForFirst()
        } catch (e: SQLException) {
            null
        }
    }

    /**
     * 根据作者UID查询视频
     *
     * @param authorUid 作者UID
     * @return 视频列表
     */
    fun findByAuthor(authorUid: String): List<BilibiliVideoEntity> {
        return try {
            val queryBuilder = dao.queryBuilder()
            queryBuilder.where().eq("author_uid", authorUid)
            queryBuilder.orderBy("upload_time", false)
            queryBuilder.query()
        } catch (e: SQLException) {
            emptyList()
        }
    }

    /**
     * 查询最近上传的视频
     *
     * @param hours 小时数
     * @return 视频列表
     */
    fun findRecentVideos(hours: Int = 24): List<BilibiliVideoEntity> {
        return try {
            val cutoffTime = System.currentTimeMillis() - (hours * 60 * 60 * 1000L)
            val queryBuilder = dao.queryBuilder()
            queryBuilder.where().ge("upload_time", cutoffTime)
            queryBuilder.orderBy("upload_time", false)
            queryBuilder.query()
        } catch (e: SQLException) {
            emptyList()
        }
    }

    /**
     * 统计视频数量
     *
     * @return 视频总数
     */
    fun count(): Long {
        return try {
            dao.countOf()
        } catch (e: SQLException) {
            0L
        }
    }
} 