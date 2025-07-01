package online.bingzi.bilibili.video.pro.internal.entity.netwrk.video

/**
 * 三连状态数据类
 */
data class TripleActionStatus(
    val isLiked: Boolean,      // 是否已点赞
    val isCoined: Boolean,     // 是否已投币
    val isFavorited: Boolean,  // 是否已收藏
    val coinCount: Int         // 投币数量
)