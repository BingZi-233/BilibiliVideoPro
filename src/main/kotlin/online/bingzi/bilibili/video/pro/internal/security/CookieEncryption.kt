package online.bingzi.bilibili.video.pro.internal.security

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Cookie加密工具类
 * 使用AES-GCM加密算法保护敏感的Cookie数据
 */
object CookieEncryption {
    
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 16
    
    private val secureRandom = SecureRandom()
    
    /**
     * 生成随机密钥
     */
    fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
        keyGenerator.init(256) // 使用256位密钥
        return keyGenerator.generateKey()
    }
    
    /**
     * 将密钥转换为Base64字符串
     */
    fun keyToString(key: SecretKey): String {
        return Base64.getEncoder().encodeToString(key.encoded)
    }
    
    /**
     * 从Base64字符串恢复密钥
     */
    fun stringToKey(keyString: String): SecretKey {
        val keyBytes = Base64.getDecoder().decode(keyString)
        return SecretKeySpec(keyBytes, ALGORITHM)
    }
    
    /**
     * 加密Cookie数据
     */
    fun encrypt(data: String, key: SecretKey): String {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            
            // 生成随机IV
            val iv = ByteArray(GCM_IV_LENGTH)
            secureRandom.nextBytes(iv)
            
            val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec)
            
            // 加密数据
            val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            
            // 将IV和加密数据组合
            val combined = iv + encryptedData
            
            // 返回Base64编码的结果
            return Base64.getEncoder().encodeToString(combined)
            
        } catch (e: Exception) {
            throw SecurityException("Failed to encrypt cookie data: ${e.message}", e)
        }
    }
    
    /**
     * 解密Cookie数据
     */
    fun decrypt(encryptedData: String, key: SecretKey): String {
        try {
            val combined = Base64.getDecoder().decode(encryptedData)
            
            // 分离IV和加密数据
            val iv = combined.sliceArray(0 until GCM_IV_LENGTH)
            val cipherText = combined.sliceArray(GCM_IV_LENGTH until combined.size)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec)
            
            // 解密数据
            val decryptedData = cipher.doFinal(cipherText)
            
            return String(decryptedData, Charsets.UTF_8)
            
        } catch (e: Exception) {
            throw SecurityException("Failed to decrypt cookie data: ${e.message}", e)
        }
    }
    
    /**
     * 验证加密数据的完整性
     */
    fun verifyIntegrity(encryptedData: String, key: SecretKey): Boolean {
        return try {
            decrypt(encryptedData, key)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 安全地清除字符串内容
     */
    fun clearString(data: String) {
        // Kotlin字符串是不可变的，这里只是提供一个接口
        // 实际应用中，敏感数据应该使用CharArray或ByteArray
        // 在使用完毕后手动清零
    }
    
    /**
     * 安全地清除字节数组内容
     */
    fun clearByteArray(data: ByteArray) {
        data.fill(0)
    }
}