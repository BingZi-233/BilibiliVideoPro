package online.bingzi.bilibili.video.pro.internal.validation

import online.bingzi.bilibili.video.pro.internal.error.ErrorHandler
import java.util.regex.Pattern

/**
 * 输入验证工具类
 * 提供各种输入参数的验证功能
 */
object InputValidator {

    // BV号格式验证正则表达式
    private val BV_PATTERN = Pattern.compile("^BV[a-zA-Z0-9]{10}$")

    // UUID格式验证正则表达式
    private val UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

    // 用户名格式验证（允许中文、英文、数字、下划线，长度3-20）
    private val USERNAME_PATTERN = Pattern.compile("^[\\u4e00-\\u9fa5\\w]{3,20}$")

    /**
     * 验证结果
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    ) {
        companion object {
            fun success() = ValidationResult(true)
            fun failure(message: String) = ValidationResult(false, message)
        }
    }

    /**
     * 验证BV号格式
     */
    fun validateBvid(bvid: String?): ValidationResult {
        return try {
            when {
                bvid.isNullOrBlank() -> ValidationResult.failure("BV号不能为空")
                bvid.length != 12 -> ValidationResult.failure("BV号长度必须为12位")
                !BV_PATTERN.matcher(bvid).matches() -> ValidationResult.failure("BV号格式不正确，应为BV开头的12位字符")
                else -> ValidationResult.success()
            }
        } catch (e: Exception) {
            ErrorHandler.handleError(
                type = ErrorHandler.ErrorType.VALIDATION,
                component = "InputValidator",
                operation = "validateBvid",
                exception = e,
                metadata = mapOf("bvid" to (bvid ?: "null"))
            )
            ValidationResult.failure("BV号验证时发生错误")
        }
    }

    /**
     * 验证玩家UUID格式
     */
    fun validatePlayerUuid(uuid: String?): ValidationResult {
        return try {
            when {
                uuid.isNullOrBlank() -> ValidationResult.failure("玩家UUID不能为空")
                !UUID_PATTERN.matcher(uuid).matches() -> ValidationResult.failure("玩家UUID格式不正确")
                else -> ValidationResult.success()
            }
        } catch (e: Exception) {
            ErrorHandler.handleError(
                type = ErrorHandler.ErrorType.VALIDATION,
                component = "InputValidator",
                operation = "validatePlayerUuid",
                exception = e,
                metadata = mapOf("uuid" to (uuid ?: "null"))
            )
            ValidationResult.failure("UUID验证时发生错误")
        }
    }

    /**
     * 验证用户名格式
     */
    fun validateUsername(username: String?): ValidationResult {
        return try {
            when {
                username.isNullOrBlank() -> ValidationResult.failure("用户名不能为空")
                username.length < 3 -> ValidationResult.failure("用户名长度不能少于3位")
                username.length > 20 -> ValidationResult.failure("用户名长度不能超过20位")
                !USERNAME_PATTERN.matcher(username).matches() -> ValidationResult.failure("用户名只能包含中文、英文、数字和下划线")
                else -> ValidationResult.success()
            }
        } catch (e: Exception) {
            ErrorHandler.handleError(
                type = ErrorHandler.ErrorType.VALIDATION,
                component = "InputValidator",
                operation = "validateUsername",
                exception = e,
                metadata = mapOf("username" to (username ?: "null"))
            )
            ValidationResult.failure("用户名验证时发生错误")
        }
    }

    /**
     * 验证Cookie值
     */
    fun validateCookie(name: String, value: String?): ValidationResult {
        return try {
            when {
                value.isNullOrBlank() -> ValidationResult.failure("Cookie值不能为空")
                value.length > 1000 -> ValidationResult.failure("Cookie值过长")
                value.contains('\n') || value.contains('\r') -> ValidationResult.failure("Cookie值不能包含换行符")
                else -> ValidationResult.success()
            }
        } catch (e: Exception) {
            ErrorHandler.handleError(
                type = ErrorHandler.ErrorType.VALIDATION,
                component = "InputValidator",
                operation = "validateCookie",
                exception = e,
                metadata = mapOf(
                    "cookie_name" to name,
                    "cookie_value_length" to (value?.length ?: 0)
                )
            )
            ValidationResult.failure("Cookie验证时发生错误")
        }
    }

    /**
     * 验证Bilibili UID
     */
    fun validateBilibiliUid(uid: Long?): ValidationResult {
        return try {
            when {
                uid == null -> ValidationResult.failure("Bilibili UID不能为空")
                uid <= 0 -> ValidationResult.failure("Bilibili UID必须为正数")
                uid > 999999999999L -> ValidationResult.failure("Bilibili UID超出有效范围")
                else -> ValidationResult.success()
            }
        } catch (e: Exception) {
            ErrorHandler.handleError(
                type = ErrorHandler.ErrorType.VALIDATION,
                component = "InputValidator",
                operation = "validateBilibiliUid",
                exception = e,
                metadata = mapOf("uid" to (uid?.toString() ?: "null"))
            )
            ValidationResult.failure("UID验证时发生错误")
        }
    }

    /**
     * 验证QR码Key
     */
    fun validateQrCodeKey(qrCodeKey: String?): ValidationResult {
        return try {
            when {
                qrCodeKey.isNullOrBlank() -> ValidationResult.failure("QR码密钥不能为空")
                qrCodeKey.length < 10 -> ValidationResult.failure("QR码密钥长度不足")
                qrCodeKey.length > 100 -> ValidationResult.failure("QR码密钥长度过长")
                else -> ValidationResult.success()
            }
        } catch (e: Exception) {
            ErrorHandler.handleError(
                type = ErrorHandler.ErrorType.VALIDATION,
                component = "InputValidator",
                operation = "validateQrCodeKey",
                exception = e,
                metadata = mapOf("key_length" to (qrCodeKey?.length ?: 0))
            )
            ValidationResult.failure("QR码密钥验证时发生错误")
        }
    }

    /**
     * 验证URL格式
     */
    fun validateUrl(url: String?): ValidationResult {
        return try {
            when {
                url.isNullOrBlank() -> ValidationResult.failure("URL不能为空")
                !url.startsWith("http://") && !url.startsWith("https://") ->
                    ValidationResult.failure("URL必须以http://或https://开头")

                url.length > 2000 -> ValidationResult.failure("URL长度过长")
                else -> ValidationResult.success()
            }
        } catch (e: Exception) {
            ErrorHandler.handleError(
                type = ErrorHandler.ErrorType.VALIDATION,
                component = "InputValidator",
                operation = "validateUrl",
                exception = e,
                metadata = mapOf("url_length" to (url?.length ?: 0))
            )
            ValidationResult.failure("URL验证时发生错误")
        }
    }

    /**
     * 批量验证多个条件
     */
    fun validateAll(vararg validations: () -> ValidationResult): ValidationResult {
        return try {
            for (validation in validations) {
                val result = validation()
                if (!result.isValid) {
                    return result
                }
            }
            ValidationResult.success()
        } catch (e: Exception) {
            ErrorHandler.handleError(
                type = ErrorHandler.ErrorType.VALIDATION,
                component = "InputValidator",
                operation = "validateAll",
                exception = e
            )
            ValidationResult.failure("批量验证时发生错误")
        }
    }
}