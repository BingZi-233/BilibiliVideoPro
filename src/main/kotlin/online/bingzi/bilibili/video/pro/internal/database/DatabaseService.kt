package online.bingzi.bilibili.video.pro.internal.database

import online.bingzi.bilibili.video.pro.internal.config.DatabaseConfig
import online.bingzi.bilibili.video.pro.internal.database.dao.BilibiliUserDao
import online.bingzi.bilibili.video.pro.internal.database.dao.BilibiliVideoDao
import online.bingzi.bilibili.video.pro.internal.database.dao.TripleActionRecordDao
import online.bingzi.bilibili.video.pro.internal.database.entity.BilibiliUserEntity
import online.bingzi.bilibili.video.pro.internal.database.entity.BilibiliVideoEntity
import online.bingzi.bilibili.video.pro.internal.database.entity.TripleActionRecordEntity
import taboolib.common.platform.function.console
import java.sql.SQLException
import java.util.*

/**
 * 数据库服务类
 *
 * 提供统一的数据库操作接口，封装DAO层操作
 * 支持SQLite和MySQL数据源的自动切换
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
object DatabaseService {

    /**
     * 数据库管理器
     */
    private var databaseManager: DatabaseManager? = null

    /**
     * 用户DAO
     */
    private var userDao: BilibiliUserDao? = null

    /**
     * 视频DAO
     */
    private var videoDao: BilibiliVideoDao? = null

    /**
     * 三连操作记录DAO
     */
    private var tripleActionDao: TripleActionRecordDao? = null

    /**
     * 是否已初始化
     */
    private var initialized = false

    /**
     * 初始化数据库服务
     *
     * @param config 数据库配置
     * @throws SQLException 初始化失败
     */
    @Throws(SQLException::class)
    fun initialize(config: DatabaseConfig) {
        if (initialized) {
            console().sendMessage("数据库服务已经初始化")
            return
        }

        try {
            console().sendMessage("初始化数据库服务...")

            // 创建数据库管理器
            databaseManager = DatabaseManager(config)
            databaseManager!!.initialize()

            // 创建DAO实例
            userDao = BilibiliUserDao(databaseManager!!)
            videoDao = BilibiliVideoDao(databaseManager!!)
            tripleActionDao = TripleActionRecordDao(databaseManager!!)

            initialized = true
            console().sendMessage("数据库服务初始化成功")

        } catch (e: Exception) {
            console().sendMessage("数据库服务初始化失败: ${e.message}")
            cleanup()
            throw SQLException("数据库服务初始化失败", e)
        }
    }

    /**
     * 使用默认SQLite配置初始化
     */
    fun initializeWithSqlite(filePath: String = "plugins/BilibiliVideoPro/data.db") {
        val config = DatabaseConfig.sqlite(filePath)
        initialize(config)
    }

    /**
     * 使用MySQL配置初始化
     */
    fun initializeWithMysql(
        host: String = "localhost",
        port: Int = 3306,
        database: String = "bilibili_video_pro",
        username: String,
        password: String
    ) {
        val config = DatabaseConfig.mysql(host, port, database, username, password)
        initialize(config)
    }

    /**
     * 检查是否已初始化
     */
    private fun checkInitialized() {
        if (!initialized) {
            throw IllegalStateException("数据库服务未初始化")
        }
    }

    // ==================== 用户相关操作 ====================

    /**
     * 创建或更新用户
     *
     * @param uid 用户UID
     * @param nickname 用户昵称
     * @param minecraftUuid Minecraft UUID（可选）
     * @return 操作结果
     */
    fun createOrUpdateUser(uid: String, nickname: String, minecraftUuid: UUID? = null): Boolean {
        checkInitialized()

        val existingUser = userDao!!.findByUid(uid)
        return if (existingUser != null) {
            // 更新现有用户
            existingUser.nickname = nickname
            minecraftUuid?.let { existingUser.setMinecraftUUID(it) }
            existingUser.updateLastUpdateTime()
            userDao!!.createOrUpdate(existingUser)
        } else {
            // 创建新用户
            val newUser = BilibiliUserEntity(uid, nickname, minecraftUuid)
            userDao!!.createOrUpdate(newUser)
        }
    }

    /**
     * 绑定Minecraft账号
     *
     * @param uid Bilibili用户UID
     * @param minecraftUuid Minecraft玩家UUID
     * @return 操作结果
     */
    fun bindMinecraftAccount(uid: String, minecraftUuid: UUID): Boolean {
        checkInitialized()
        return userDao!!.bindMinecraft(uid, minecraftUuid)
    }

    /**
     * 解绑Minecraft账号
     *
     * @param uid Bilibili用户UID
     * @return 操作结果
     */
    fun unbindMinecraftAccount(uid: String): Boolean {
        checkInitialized()
        return userDao!!.unbindMinecraft(uid)
    }

    /**
     * 根据UID查询用户
     *
     * @param uid 用户UID
     * @return 用户实体
     */
    fun getUserByUid(uid: String): BilibiliUserEntity? {
        checkInitialized()
        return userDao!!.findByUid(uid)
    }

    /**
     * 根据Minecraft UUID查询用户
     *
     * @param minecraftUuid Minecraft玩家UUID
     * @return 用户实体
     */
    fun getUserByMinecraftUuid(minecraftUuid: UUID): BilibiliUserEntity? {
        checkInitialized()
        return userDao!!.findByMinecraftUuid(minecraftUuid)
    }

    /**
     * 获取所有活跃用户
     *
     * @return 活跃用户列表
     */
    fun getActiveUsers(): List<BilibiliUserEntity> {
        checkInitialized()
        return userDao!!.findActiveUsers()
    }

    /**
     * 检查用户是否存在
     *
     * @param uid 用户UID
     * @return 是否存在
     */
    fun userExists(uid: String): Boolean {
        checkInitialized()
        return userDao!!.exists(uid)
    }

    // ==================== 视频相关操作 ====================

    /**
     * 创建或更新视频
     *
     * @param aid 视频AV号
     * @param bvid 视频BV号
     * @param title 视频标题
     * @param authorUid 作者UID
     * @param authorName 作者昵称
     * @param uploadTime 上传时间
     * @return 操作结果
     */
    fun createOrUpdateVideo(
        aid: Long,
        bvid: String,
        title: String,
        authorUid: String,
        authorName: String,
        uploadTime: Long
    ): Boolean {
        checkInitialized()

        val existingVideo = videoDao!!.findByBvid(bvid)
        return if (existingVideo != null) {
            // 更新现有视频
            existingVideo.title = title
            existingVideo.authorUid = authorUid
            existingVideo.authorName = authorName
            existingVideo.uploadTime = uploadTime
            existingVideo.updateLastUpdateTime()
            videoDao!!.createOrUpdate(existingVideo)
        } else {
            // 创建新视频
            val newVideo = BilibiliVideoEntity(aid, bvid, title, authorUid, authorName, uploadTime)
            videoDao!!.createOrUpdate(newVideo)
        }
    }

    /**
     * 根据BV号查询视频
     *
     * @param bvid 视频BV号
     * @return 视频实体
     */
    fun getVideoByBvid(bvid: String): BilibiliVideoEntity? {
        checkInitialized()
        return videoDao!!.findByBvid(bvid)
    }

    /**
     * 根据AV号查询视频
     *
     * @param aid 视频AV号
     * @return 视频实体
     */
    fun getVideoByAid(aid: Long): BilibiliVideoEntity? {
        checkInitialized()
        return videoDao!!.findByAid(aid)
    }

    /**
     * 根据作者查询视频
     *
     * @param authorUid 作者UID
     * @return 视频列表
     */
    fun getVideosByAuthor(authorUid: String): List<BilibiliVideoEntity> {
        checkInitialized()
        return videoDao!!.findByAuthor(authorUid)
    }

    /**
     * 获取最近上传的视频
     *
     * @param hours 小时数
     * @return 视频列表
     */
    fun getRecentVideos(hours: Int = 24): List<BilibiliVideoEntity> {
        checkInitialized()
        return videoDao!!.findRecentVideos(hours)
    }

    // ==================== 三连操作记录相关操作 ====================

    /**
     * 记录三连操作
     *
     * @param userUid 用户UID
     * @param videoBvid 视频BV号
     * @param videoAid 视频AV号
     * @param liked 是否点赞
     * @param coined 是否投币
     * @param coinCount 投币数量
     * @param favorited 是否收藏
     * @return 操作结果
     */
    fun recordTripleAction(
        userUid: String,
        videoBvid: String,
        videoAid: Long,
        liked: Boolean,
        coined: Boolean,
        coinCount: Int,
        favorited: Boolean
    ): Boolean {
        checkInitialized()

        val existingRecord = tripleActionDao!!.findByUserAndVideo(userUid, videoBvid)
        return if (existingRecord != null) {
            // 更新现有记录
            existingRecord.setLiked(liked)
            existingRecord.setCoined(coined)
            existingRecord.coinCount = coinCount
            existingRecord.setFavorited(favorited)
            existingRecord.setActionType(TripleActionRecordEntity.ActionType.UPDATE)
            tripleActionDao!!.createOrUpdate(existingRecord)
        } else {
            // 创建新记录
            val newRecord = TripleActionRecordEntity(
                userUid, videoBvid, videoAid, liked, coined, coinCount, favorited
            )
            tripleActionDao!!.createOrUpdate(newRecord)
        }
    }

    /**
     * 获取用户对视频的操作记录
     *
     * @param userUid 用户UID
     * @param videoBvid 视频BV号
     * @return 操作记录
     */
    fun getTripleActionRecord(userUid: String, videoBvid: String): TripleActionRecordEntity? {
        checkInitialized()
        return tripleActionDao!!.findByUserAndVideo(userUid, videoBvid)
    }

    /**
     * 获取用户的所有操作记录
     *
     * @param userUid 用户UID
     * @return 操作记录列表
     */
    fun getUserTripleActions(userUid: String): List<TripleActionRecordEntity> {
        checkInitialized()
        return tripleActionDao!!.findByUser(userUid)
    }

    /**
     * 获取视频的所有操作记录
     *
     * @param videoBvid 视频BV号
     * @return 操作记录列表
     */
    fun getVideoTripleActions(videoBvid: String): List<TripleActionRecordEntity> {
        checkInitialized()
        return tripleActionDao!!.findByVideo(videoBvid)
    }

    /**
     * 获取所有完整三连记录
     *
     * @return 完整三连记录列表
     */
    fun getFullTripleRecords(): List<TripleActionRecordEntity> {
        checkInitialized()
        return tripleActionDao!!.findFullTripleRecords()
    }

    /**
     * 获取未发放奖励的记录
     *
     * @return 未发放奖励的记录列表
     */
    fun getUnrewardedRecords(): List<TripleActionRecordEntity> {
        checkInitialized()
        return tripleActionDao!!.findUnrewardedRecords()
    }

    /**
     * 标记记录为已发放奖励
     *
     * @param recordId 记录ID
     * @param rewardType 奖励类型
     * @param rewardContent 奖励内容
     * @return 操作结果
     */
    fun markAsRewarded(recordId: Long, rewardType: String, rewardContent: String): Boolean {
        checkInitialized()

        return try {
            val record = databaseManager!!.getTripleActionDao().queryForId(recordId)
            if (record != null) {
                record.setRewardStatus(TripleActionRecordEntity.RewardStatus.REWARDED)
                record.rewardType = rewardType
                record.rewardContent = rewardContent
                tripleActionDao!!.createOrUpdate(record)
            } else {
                false
            }
        } catch (e: SQLException) {
            false
        }
    }

    // ==================== 统计相关操作 ====================

    /**
     * 获取数据库统计信息
     *
     * @return 统计信息
     */
    fun getDatabaseStats(): DatabaseManager.DatabaseStats? {
        checkInitialized()
        return databaseManager?.getDatabaseStats()
    }

    /**
     * 获取用户统计信息
     *
     * @return 用户统计信息
     */
    fun getUserStats(): BilibiliUserDao.UserStats? {
        checkInitialized()
        return userDao?.getUserStats()
    }

    /**
     * 检查数据库连接状态
     *
     * @return 连接状态
     */
    fun checkConnection(): Boolean {
        checkInitialized()
        return databaseManager?.checkConnection() ?: false
    }

    /**
     * 执行数据库备份（仅SQLite）
     *
     * @param backupPath 备份文件路径
     * @return 备份结果
     */
    fun backup(backupPath: String): Boolean {
        checkInitialized()
        return databaseManager?.backup(backupPath) ?: false
    }

    /**
     * 清理资源
     */
    fun cleanup() {
        try {
            databaseManager?.cleanup()
            userDao = null
            videoDao = null
            tripleActionDao = null
            databaseManager = null
            initialized = false
            console().sendMessage("数据库服务资源清理完成")
        } catch (e: Exception) {
            console().sendMessage("数据库服务资源清理失败: ${e.message}")
        }
    }

    /**
     * 获取当前配置
     *
     * @return 数据库配置
     */
    fun getCurrentConfig(): DatabaseConfig? {
        return databaseManager?.getConfig()
    }

    /**
     * 是否已初始化
     *
     * @return 初始化状态
     */
    fun isInitialized(): Boolean = initialized
} 