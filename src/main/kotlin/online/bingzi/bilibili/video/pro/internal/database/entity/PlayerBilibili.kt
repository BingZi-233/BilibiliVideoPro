package online.bingzi.bilibili.video.pro.internal.database.entity

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import online.bingzi.bilibili.video.pro.internal.config.DatabaseConfig
import java.util.*

/**
 * 玩家Bilibili账户绑定实体
 * 存储玩家与Bilibili账户的绑定关系和登录信息
 */
@DatabaseTable(tableName = "player_bilibili")
class PlayerBilibili {
    
    companion object {
        const val TABLE_NAME = "player_bilibili"
        const val FIELD_ID = "id"
        const val FIELD_PLAYER_UUID = "player_uuid"
        const val FIELD_PLAYER_NAME = "player_name"
        const val FIELD_BILIBILI_UID = "bilibili_uid"
        const val FIELD_BILIBILI_USERNAME = "bilibili_username"
        const val FIELD_BILIBILI_FACE = "bilibili_face"
        const val FIELD_BILIBILI_LEVEL = "bilibili_level"
        const val FIELD_IS_VIP = "is_vip"
        const val FIELD_SESSDATA = "sessdata"
        const val FIELD_BILI_JCT = "bili_jct"
        const val FIELD_DEDE_USER_ID = "dede_user_id"
        const val FIELD_DEDE_USER_ID_MD5 = "dede_user_id_md5"
        const val FIELD_CREATED_TIME = "created_time"
        const val FIELD_UPDATED_TIME = "updated_time"
        const val FIELD_LAST_LOGIN_TIME = "last_login_time"
        const val FIELD_IS_ACTIVE = "is_active"
        
        /**
         * 获取完整的表名（包含前缀）
         */
        fun getFullTableName(): String {
            return DatabaseConfig.getTableName(TABLE_NAME)
        }
    }
    
    /**
     * 主键ID
     */
    @DatabaseField(generatedId = true, columnName = FIELD_ID)
    var id: Long = 0
    
    /**
     * 玩家UUID
     */
    @DatabaseField(columnName = FIELD_PLAYER_UUID, canBeNull = false, unique = true, width = 36)
    var playerUuid: String = ""
    
    /**
     * 玩家名称
     */
    @DatabaseField(columnName = FIELD_PLAYER_NAME, canBeNull = false, width = 16)
    var playerName: String = ""
    
    /**
     * Bilibili用户ID
     */
    @DatabaseField(columnName = FIELD_BILIBILI_UID, canBeNull = false, index = true)
    var bilibiliUid: Long = 0
    
    /**
     * Bilibili用户名
     */
    @DatabaseField(columnName = FIELD_BILIBILI_USERNAME, canBeNull = false, width = 50)
    var bilibiliUsername: String = ""
    
    /**
     * Bilibili头像URL
     */
    @DatabaseField(columnName = FIELD_BILIBILI_FACE, canBeNull = true, width = 255)
    var bilibiliFace: String? = null
    
    /**
     * Bilibili用户等级
     */
    @DatabaseField(columnName = FIELD_BILIBILI_LEVEL, canBeNull = false)
    var bilibiliLevel: Int = 0
    
    /**
     * 是否为大会员
     */
    @DatabaseField(columnName = FIELD_IS_VIP, canBeNull = false)
    var isVip: Boolean = false
    
    /**
     * SESSDATA Cookie
     */
    @DatabaseField(columnName = FIELD_SESSDATA, canBeNull = false, width = 500)
    var sessdata: String = ""
    
    /**
     * bili_jct Cookie
     */
    @DatabaseField(columnName = FIELD_BILI_JCT, canBeNull = false, width = 100)
    var biliJct: String = ""
    
    /**
     * DedeUserID Cookie
     */
    @DatabaseField(columnName = FIELD_DEDE_USER_ID, canBeNull = false, width = 50)
    var dedeUserId: String = ""
    
    /**
     * DedeUserID__ckMd5 Cookie
     */
    @DatabaseField(columnName = FIELD_DEDE_USER_ID_MD5, canBeNull = false, width = 50)
    var dedeUserIdMd5: String = ""
    
    /**
     * 创建时间
     */
    @DatabaseField(columnName = FIELD_CREATED_TIME, canBeNull = false)
    var createdTime: Date = Date()
    
    /**
     * 更新时间
     */
    @DatabaseField(columnName = FIELD_UPDATED_TIME, canBeNull = false)
    var updatedTime: Date = Date()
    
    /**
     * 最后登录时间
     */
    @DatabaseField(columnName = FIELD_LAST_LOGIN_TIME, canBeNull = true)
    var lastLoginTime: Date? = null
    
    /**
     * 是否激活（用于软删除）
     */
    @DatabaseField(columnName = FIELD_IS_ACTIVE, canBeNull = false)
    var isActive: Boolean = true
    
    /**
     * 默认构造函数（ORMLite需要）
     */
    constructor()
    
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
    ) {
        this.playerUuid = playerUuid
        this.playerName = playerName
        this.bilibiliUid = bilibiliUid
        this.bilibiliUsername = bilibiliUsername
        this.sessdata = sessdata
        this.biliJct = biliJct
        this.dedeUserId = dedeUserId
        this.dedeUserIdMd5 = dedeUserIdMd5
        this.createdTime = Date()
        this.updatedTime = Date()
    }
    
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
    
    override fun toString(): String {
        return "PlayerBilibili(id=$id, playerUuid='$playerUuid', playerName='$playerName', " +
                "bilibiliUid=$bilibiliUid, bilibiliUsername='$bilibiliUsername', " +
                "bilibiliLevel=$bilibiliLevel, isVip=$isVip, isActive=$isActive)"
    }
} 