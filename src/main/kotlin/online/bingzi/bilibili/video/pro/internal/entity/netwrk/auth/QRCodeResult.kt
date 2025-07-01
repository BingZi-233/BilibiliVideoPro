package online.bingzi.bilibili.video.pro.internal.entity.netwrk.auth

/**
 * 二维码生成结果
 */
sealed class QRCodeResult {
    data class Success(val data: QRCodeData) : QRCodeResult()
    data class Error(val message: String) : QRCodeResult()
}