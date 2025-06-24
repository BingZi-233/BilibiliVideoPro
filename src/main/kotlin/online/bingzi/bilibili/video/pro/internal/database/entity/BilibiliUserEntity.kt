package online.bingzi.bilibili.video.pro.internal.database.entity

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.util.*

/**
 * Bilibili用户数据库实体
 *
 * 使用ORMLite注解进行数据库映射
 * 对应数据库表：bilibili_users
 *
 * @author BilibiliVideoPro
 * @since 1.0.0
 */
@DatabaseTable(tableName = "bilibili_users")
class BilibiliUserEntity {

    /**
     * 主键ID（自增）
     */
    @DatabaseField(generatedId = true, columnName = "id")
    var id: Long = 0

    /**
     * Bilibili用户UID（唯一索引）
     */
    @DatabaseField(canBeNull = false, unique = true, columnName = "uid", index = true)
    var uid: String = ""

    /**
     * 用户昵称
     */
    @DatabaseField(canBeNull = false, columnName = "nickname")
    var nickname: String = ""

    /**
     * 绑定的Minecraft玩家UUID
     */
    @DatabaseField(columnName = "minecraft_uuid", index = true)
    var minecraftUuid: String? = null

    /**
     * 绑定时间（毫秒时间戳）
     */
    @DatabaseField(canBeNull = false, columnName = "bind_time")
    var bindTime: Long = 0

    /**
     * 最后更新时间（毫秒时间戳）
     */
    @DatabaseField(canBeNull = false, columnName = "last_update")
    var lastUpdate: Long = 0

    /**
     * 用户状态（1：正常，0：禁用）
     */
    @DatabaseField(canBeNull = false, columnName = "status")
    var status: Int = 1

    /**
     * 用户头像URL
     */
    @DatabaseField(columnName = "avatar_url")
    var avatarUrl: String? = null

    /**
     * 用户等级
     */
    @DatabaseField(columnName = "level")
    var level: Int = 0

    /**
     * 扩展数据（JSON格式）
     */
    @DatabaseField(columnName = "extra_data")
    var extraData: String? = null

    /**
     * 创建时间（毫秒时间戳）
     */
    @DatabaseField(canBeNull = false, columnName = "create_time")
    var createTime: Long = 0

    /**
     * 无参构造函数（ORMLite要求）
     */
    constructor()

    /**
     * 构造函数
     *
     * @param uid Bilibili用户UID
     * @param nickname 用户昵称
     * @param minecraftUuid Minecraft玩家UUID（可选）
     */
    constructor(uid: String, nickname: String, minecraftUuid: UUID? = null) {
        this.uid = uid
        this.nickname = nickname
        this.minecraftUuid = minecraftUuid?.toString()
        val now = System.currentTimeMillis()
        this.bindTime = now
        this.lastUpdate = now
        this.createTime = now
    }

    /**
     * 更新最后更新时间
     */
    fun updateLastUpdateTime() {
        this.lastUpdate = System.currentTimeMillis()
    }

    /**
     * 是否已绑定Minecraft玩家
     *
     * @return true表示已绑定
     */
    fun isBoundToMinecraft(): Boolean {
        return !minecraftUuid.isNullOrBlank()
    }

    /**
     * 获取Minecraft UUID
     *
     * @return UUID对象，未绑定时返回null
     */
    fun getMinecraftUUID(): UUID? {
        return minecraftUuid?.let { UUID.fromString(it) }
    }

    /**
     * 设置Minecraft UUID
     *
     * @param uuid UUID对象
     */
    fun setMinecraftUUID(uuid: UUID?) {
        this.minecraftUuid = uuid?.toString()
        updateLastUpdateTime()
    }

    /**
     * 是否为正常状态
     *
     * @return true表示正常状态
     */
    fun isActive(): Boolean {
        return status == 1
    }

    /**
     * 设置用户状态
     *
     * @param active true为正常，false为禁用
     */
    fun setActive(active: Boolean) {
        this.status = if (active) 1 else 0
        updateLastUpdateTime()
    }

    /**
     * 获取绑定持续时间（毫秒）
     *
     * @return 绑定持续时间
     */
    fun getBindDuration(): Long {
        return System.currentTimeMillis() - bindTime
    }

    override fun toString(): String {
        return "BilibiliUserEntity(id=$id, uid='$uid', nickname='$nickname', minecraftUuid=$minecraftUuid, bindTime=$bindTime, status=$status)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BilibiliUserEntity) return false
        return uid == other.uid
    }

    override fun hashCode(): Int {
        return uid.hashCode()
    }
} 