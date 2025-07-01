package online.bingzi.bilibili.video.pro.internal.entity.netwrk.auth

/**
 * 二维码数据
 */
data class QRCodeData(
    val url: String,        // 二维码内容URL
    val qrcodeKey: String   // 二维码密钥
)