package online.bingzi.bilibili.video.pro.internal.database.entity

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import online.bingzi.bilibili.video.pro.internal.config.DatabaseConfig
import java.util.*

/**
 * 玩家Bilibili账户绑定实体
 * 存储玩家与Bilibili账户的绑定关系和登录信息
 */
@DatabaseTable(tableName = PlayerBilibili.TABLE_NAME)
data class PlayerBilibili(
    /**
     * 主键ID
     */
    @DatabaseField(generatedId = true, columnName = ID)
    var id: Long = 0,
    /**
     * 玩家UUID
     */
    @DatabaseField(columnName = PLAYER_UUID, canBeNull = false, unique = true, width = 36)
    var playerUuid: String = "",
    /**
     * 玩家名称
     */
    @DatabaseField(columnName = PLAYER_NAME, canBeNull = false, width = 16)
    var playerName: String = "",
    /**
     * Bilibili用户ID
     */
    @DatabaseField(columnName = BILIBILI_UID, canBeNull = false, index = true)
    var bilibiliUid: Long = 0,
    /**
     * Bilibili用户名
     */
    @DatabaseField(columnName = BILIBILI_USERNAME, canBeNull = false, width = 50)
    var bilibiliUsername: String = "",
    /**
     * Bilibili头像URL
     */
    @DatabaseField(columnName = BILIBILI_FACE, canBeNull = true, width = 255)
    var bilibiliFace: String? = null,
    /**
     * Bilibili用户等级
     */
    @DatabaseField(columnName = BILIBILI_LEVEL, canBeNull = false)
    var bilibiliLevel: Int = 0,
    /**
     * 是否为大会员
     */
    @DatabaseField(columnName = IS_VIP, canBeNull = false)
    var isVip: Boolean = false,
    /**
     * SESSDATA Cookie
     */
    @DatabaseField(columnName = SESSDATA, canBeNull = false, width = 500)
    var sessdata: String = "",
    /**
     * bili_jct Cookie
     */
    @DatabaseField(columnName = BILI_JCT, canBeNull = false, width = 100)
    var biliJct: String = "",
    /**
     * DedeUserID Cookie
     */
    @DatabaseField(columnName = DEDE_USER_ID, canBeNull = false, width = 50)
    var dedeUserId: String = "",
    /**
     * DedeUserID__ckMd5 Cookie
     */
    @DatabaseField(columnName = DEDE_USER_ID_MD5, canBeNull = false, width = 50)
    var dedeUserIdMd5: String = "",
    /**
     * 创建时间
     */
    @DatabaseField(columnName = CREATED_TIME, canBeNull = false)
    var createdTime: Date = Date(),
    /**
     * 更新时间
     */
    @DatabaseField(columnName = UPDATED_TIME, canBeNull = false)
    var updatedTime: Date = Date(),
    /**
     * 最后登录时间
     */
    @DatabaseField(columnName = LAST_LOGIN_TIME, canBeNull = true)
    var lastLoginTime: Date? = null,
    /**
     * 是否激活（用于软删除）
     */
    @DatabaseField(columnName = IS_ACTIVE, canBeNull = false)
    var isActive: Boolean = true
) {
    // ORMLite requires a no-arg constructor.
    // By providing default values for all properties in the data class's primary constructor,
    // the Kotlin compiler generates a no-arg constructor automatically.
    companion object {
        const val TABLE_NAME = "player_bilibili"
        const val ID = "id"
        const val PLAYER_UUID = "player_uuid"
        const val PLAYER_NAME = "player_name"
        const val BILIBILI_UID = "bilibili_uid"
        const val BILIBILI_USERNAME = "bilibili_username"
        const val BILIBILI_FACE = "bilibili_face"
        const val BILIBILI_LEVEL = "bilibili_level"
        const val IS_VIP = "is_vip"
        const val SESSDATA = "sessdata"
        const val BILI_JCT = "bili_jct"
        const val DEDE_USER_ID = "dede_user_id"
        const val DEDE_USER_ID_MD5 = "dede_user_id_md5"
        const val CREATED_TIME = "created_time"
        const val UPDATED_TIME = "updated_time"
        const val LAST_LOGIN_TIME = "last_login_time"
        const val IS_ACTIVE = "is_active"

        /**
         * 获取完整的表名（包含前缀）
         */
        fun getFullTableName(): String {
            return DatabaseConfig.getTableName(TABLE_NAME)
        }
    }

    /**
     * 创建新绑定的构造函数
     */
    constructor(
        playerUuid: String,
        playerName: String,
        bilibiliUid: Long,
        bilibiliUsername: String,
        sessdata: String,
        biliJct: String,
        dedeUserId: String,
        dedeUserIdMd5: String
    ) : this(
        playerUuid = playerUuid,
        playerName = playerName,
        bilibiliUid = bilibiliUid,
        bilibiliUsername = bilibiliUsername,
        sessdata = sessdata,
        biliJct = biliJct,
        dedeUserId = dedeUserId,
        dedeUserIdMd5 = dedeUserIdMd5,
        createdTime = Date(),
        updatedTime = Date()
    )

    /**
     * 更新Bilibili用户信息
     */
    fun updateBilibiliInfo(
        username: String,
        face: String? = null,
        level: Int = 0,
        isVip: Boolean = false
    ) {
        this.bilibiliUsername = username
        this.bilibiliFace = face
        this.bilibiliLevel = level
        this.isVip = isVip
        this.updatedTime = Date()
    }

    /**
     * 更新Cookie信息
     */
    fun updateCookies(
        sessdata: String,
        biliJct: String,
        dedeUserId: String,
        dedeUserIdMd5: String
    ) {
        this.sessdata = sessdata
        this.biliJct = biliJct
        this.dedeUserId = dedeUserId
        this.dedeUserIdMd5 = dedeUserIdMd5
        this.updatedTime = Date()
        this.lastLoginTime = Date()
    }

    /**
     * 获取Cookie映射
     */
    fun getCookieMap(): Map<String, String> {
        return mapOf(
            "SESSDATA" to sessdata,
            "bili_jct" to biliJct,
            "DedeUserID" to dedeUserId,
            "DedeUserID__ckMd5" to dedeUserIdMd5
        )
    }

    /**
     * 检查Cookie是否完整
     */
    fun hasValidCookies(): Boolean {
        return sessdata.isNotEmpty() &&
                biliJct.isNotEmpty() &&
                dedeUserId.isNotEmpty() &&
                dedeUserIdMd5.isNotEmpty()
    }

    /**
     * 软删除（设置为不活跃）
     */
    fun softDelete() {
        this.isActive = false
        this.updatedTime = Date()
    }
} 