package online.bingzi.bilibili.video.pro.internal.security

import taboolib.common.platform.function.console
import taboolib.module.lang.sendInfo
import java.io.File
import java.security.SecureRandom
import javax.crypto.SecretKey

/**
 * 安全密钥管理器
 * 负责生成、存储和管理加密密钥
 */
object SecureKeyManager {
    
    private const val KEY_FILE_NAME = "security.key"
    private const val KEY_FILE_PERMISSIONS = "600" // 仅所有者可读写
    
    private var encryptionKey: SecretKey? = null
    private val secureRandom = SecureRandom()
    
    /**
     * 初始化密钥管理器
     */
    fun initialize(pluginDataFolder: File): Boolean {
        return try {
            console().sendInfo("正在初始化安全密钥管理器...")
            
            val keyFile = File(pluginDataFolder, KEY_FILE_NAME)
            
            encryptionKey = if (keyFile.exists()) {
                // 加载现有密钥
                loadKeyFromFile(keyFile)
            } else {
                // 生成新密钥
                val newKey = CookieEncryption.generateKey()
                saveKeyToFile(keyFile, newKey)
                newKey
            }
            
            console().sendInfo("安全密钥管理器初始化完成")
            true
            
        } catch (e: Exception) {
            console().sendInfo("安全密钥管理器初始化失败: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 获取加密密钥
     */
    fun getEncryptionKey(): SecretKey {
        return encryptionKey ?: throw IllegalStateException("密钥管理器未初始化")
    }
    
    /**
     * 从文件加载密钥
     */
    private fun loadKeyFromFile(keyFile: File): SecretKey {
        try {
            val keyString = keyFile.readText().trim()
            return CookieEncryption.stringToKey(keyString)
        } catch (e: Exception) {
            throw SecurityException("无法从文件加载密钥: ${e.message}", e)
        }
    }
    
    /**
     * 保存密钥到文件
     */
    private fun saveKeyToFile(keyFile: File, key: SecretKey) {
        try {
            // 确保父目录存在
            keyFile.parentFile.mkdirs()
            
            val keyString = CookieEncryption.keyToString(key)
            keyFile.writeText(keyString)
            
            // 设置文件权限（仅Unix系统）
            try {
                val process = Runtime.getRuntime().exec(arrayOf("chmod", KEY_FILE_PERMISSIONS, keyFile.absolutePath))
                process.waitFor()
            } catch (e: Exception) {
                console().sendInfo("无法设置密钥文件权限: ${e.message}")
            }
            
        } catch (e: Exception) {
            throw SecurityException("无法保存密钥到文件: ${e.message}", e)
        }
    }
    
    /**
     * 重新生成密钥
     * 警告：这将使所有现有的加密数据无法解密
     */
    fun regenerateKey(pluginDataFolder: File): Boolean {
        return try {
            console().sendInfo("正在重新生成加密密钥...")
            
            val keyFile = File(pluginDataFolder, KEY_FILE_NAME)
            val newKey = CookieEncryption.generateKey()
            
            saveKeyToFile(keyFile, newKey)
            encryptionKey = newKey
            
            console().sendInfo("加密密钥重新生成完成")
            true
            
        } catch (e: Exception) {
            console().sendInfo("重新生成密钥失败: ${e.message}")
            false
        }
    }
    
    /**
     * 验证密钥完整性
     */
    fun verifyKeyIntegrity(): Boolean {
        return try {
            val key = getEncryptionKey()
            val testData = "integrity_test_" + secureRandom.nextLong()
            val encrypted = CookieEncryption.encrypt(testData, key)
            val decrypted = CookieEncryption.decrypt(encrypted, key)
            
            testData == decrypted
            
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取密钥信息
     */
    fun getKeyInfo(): Map<String, Any> {
        return try {
            val key = getEncryptionKey()
            mapOf(
                "algorithm" to key.algorithm,
                "format" to (key.format ?: "unknown"),
                "keyLength" to key.encoded.size * 8,
                "isValid" to verifyKeyIntegrity()
            )
        } catch (e: Exception) {
            mapOf(
                "error" to (e.message ?: "unknown error"),
                "isValid" to false
            )
        }
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        try {
            encryptionKey = null
            console().sendInfo("安全密钥管理器已清理")
        } catch (e: Exception) {
            console().sendInfo("清理密钥管理器时出错: ${e.message}")
        }
    }
}