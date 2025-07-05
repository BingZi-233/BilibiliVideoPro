package online.bingzi.bilibili.video.pro.internal.expansion

import online.bingzi.bilibili.video.pro.internal.cache.CacheCleanupManager
import online.bingzi.bilibili.video.pro.internal.database.service.PlayerBilibiliService
import online.bingzi.bilibili.video.pro.internal.database.service.VideoInteractionService
import org.bukkit.OfflinePlayer
import taboolib.platform.compat.PlaceholderExpansion
import java.text.SimpleDateFormat

/**
 * BilibiliVideoPro PlaceholderAPI 扩展
 * 提供Bilibili相关的占位符变量
 */
object BilibiliVideoProExpansion : PlaceholderExpansion {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    override val identifier: String = "bilibiliVideoPro"

    override fun onPlaceholderRequest(player: OfflinePlayer?, args: String): String {
        if (player == null || !player.isOnline) return "N/A"

        val playerUuid = player.uniqueId.toString()

        return when (args.lowercase()) {
            "bound" -> {
                val binding = PlayerBilibiliService.findByPlayerUuid(playerUuid)
                if (binding?.isActive == true) "true" else "false"
            }

            "username" -> {
                val binding = PlayerBilibiliService.findByPlayerUuid(playerUuid)
                binding?.bilibiliUsername ?: "未绑定"
            }

            "uid" -> {
                val binding = PlayerBilibiliService.findByPlayerUuid(playerUuid)
                binding?.bilibiliUid?.toString() ?: "0"
            }

            "level" -> {
                val binding = PlayerBilibiliService.findByPlayerUuid(playerUuid)
                binding?.bilibiliLevel?.toString() ?: "0"
            }

            "vip" -> {
                val binding = PlayerBilibiliService.findByPlayerUuid(playerUuid)
                if (binding?.isVip == true) "是" else "否"
            }

            "bind_time" -> {
                val binding = PlayerBilibiliService.findByPlayerUuid(playerUuid)
                binding?.createdTime?.let { dateFormat.format(it) } ?: "未绑定"
            }

            "last_login" -> {
                val binding = PlayerBilibiliService.findByPlayerUuid(playerUuid)
                binding?.lastLoginTime?.let { dateFormat.format(it) } ?: "从未登录"
            }

            "cooldown" -> {
                if (CacheCleanupManager.isPlayerOnCooldown(playerUuid)) {
                    val remaining = CacheCleanupManager.getPlayerCooldownRemaining(playerUuid)
                    "${remaining}秒"
                } else {
                    "0秒"
                }
            }

            "total_videos" -> {
                val stats = VideoInteractionService.getPlayerStatistics(playerUuid)
                stats.totalVideos.toString()
            }

            "total_likes" -> {
                val stats = VideoInteractionService.getPlayerStatistics(playerUuid)
                stats.totalLikes.toString()
            }

            "total_coins" -> {
                val stats = VideoInteractionService.getPlayerStatistics(playerUuid)
                stats.totalCoins.toString()
            }

            "total_favorites" -> {
                val stats = VideoInteractionService.getPlayerStatistics(playerUuid)
                stats.totalFavorites.toString()
            }

            "triple_count" -> {
                val stats = VideoInteractionService.getPlayerStatistics(playerUuid)
                stats.tripleCompletedVideos.toString()
            }

            "triple_rate" -> {
                val stats = VideoInteractionService.getPlayerStatistics(playerUuid)
                if (stats.totalVideos > 0) {
                    val rate = (stats.tripleCompletedVideos.toDouble() / stats.totalVideos.toDouble()) * 100
                    String.format("%.1f%%", rate)
                } else {
                    "0.0%"
                }
            }

            "avg_triple_daily" -> {
                val stats = VideoInteractionService.getPlayerStatistics(playerUuid)
                String.format("%.2f", stats.averageTriplePerDay)
            }

            "total_rewards" -> {
                val stats = VideoInteractionService.getPlayerStatistics(playerUuid)
                stats.totalRewards.toString()
            }

            "online" -> {
                val binding = PlayerBilibiliService.findByPlayerUuid(playerUuid)
                if (binding?.hasValidCookies() == true) "在线" else "离线"
            }

            "status" -> {
                val binding = PlayerBilibiliService.findByPlayerUuid(playerUuid)
                when {
                    binding == null || !binding.isActive -> "未绑定"
                    !binding.hasValidCookies() -> "已绑定(离线)"
                    binding.isVip -> "已绑定(大会员)"
                    else -> "已绑定"
                }
            }

            else -> "未知参数"
        }
    }
}