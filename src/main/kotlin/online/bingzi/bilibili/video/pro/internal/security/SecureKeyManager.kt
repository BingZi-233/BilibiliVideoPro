package online.bingzi.bilibili.video.pro.internal.security

import taboolib.common.platform.function.console
import taboolib.module.lang.sendInfo
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.*
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
            console().sendInfo("securityKeyManagerInitializing")
            
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
            
            console().sendInfo("securityKeyManagerInitialized")
            true
            
        } catch (e: Exception) {
            console().sendInfo("securityKeyManagerInitFailed", e.message ?: "unknown")
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
            
            // 使用Java NIO.2 API设置文件权限（更安全的方式）
            try {
                if (System.getProperty("os.name").lowercase().contains("windows")) {
                    // Windows系统：设置只有当前用户可访问
                    setWindowsFilePermissions(keyFile)
                } else {
                    // Unix系统：设置权限为600（只有所有者可读写）
                    setUnixFilePermissions(keyFile)
                }
            } catch (e: Exception) {
                console().sendInfo("securityKeyPermissionError", e.message ?: "unknown")
                // 权限设置失败时抛出异常，因为这是安全关键操作
                throw SecurityException("无法设置安全的文件权限，密钥文件可能不安全", e)
            }
            
        } catch (e: Exception) {
            throw SecurityException("无法保存密钥到文件: ${e.message}", e)
        }
    }
    
    /**
     * 设置Windows文件权限
     */
    private fun setWindowsFilePermissions(file: File) {
        try {
            val path = file.toPath()
            val acl = Files.getFileAttributeView(path, AclFileAttributeView::class.java)
            
            if (acl != null) {
                // 获取当前用户
                val currentUser = path.fileSystem.userPrincipalLookupService
                    .lookupPrincipalByName(System.getProperty("user.name"))
                
                // 清除现有权限
                acl.acl = emptyList()
                
                // 设置只有当前用户的完全控制权限
                val entry = AclEntry.newBuilder()
                    .setType(AclEntryType.ALLOW)
                    .setPrincipal(currentUser)
                    .setPermissions(
                        AclEntryPermission.READ_DATA,
                        AclEntryPermission.WRITE_DATA,
                        AclEntryPermission.READ_ATTRIBUTES,
                        AclEntryPermission.WRITE_ATTRIBUTES,
                        AclEntryPermission.READ_ACL,
                        AclEntryPermission.WRITE_ACL,
                        AclEntryPermission.DELETE
                    )
                    .build()
                
                acl.acl = listOf(entry)
                console().sendInfo("securityKeyWindowsPermissionSet")
            }
        } catch (e: Exception) {
            throw SecurityException("设置Windows文件权限失败: ${e.message}", e)
        }
    }
    
    /**
     * 设置Unix文件权限
     */
    private fun setUnixFilePermissions(file: File) {
        try {
            val path = file.toPath()
            val permissions = setOf(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE
            )
            Files.setPosixFilePermissions(path, permissions)
            console().sendInfo("securityKeyUnixPermissionSet")
        } catch (e: Exception) {
            throw SecurityException("设置Unix文件权限失败: ${e.message}", e)
        }
    }
    
    /**
     * 重新生成密钥
     * 警告：这将使所有现有的加密数据无法解密
     */
    fun regenerateKey(pluginDataFolder: File): Boolean {
        return try {
            console().sendInfo("securityKeyRegenerating")
            
            val keyFile = File(pluginDataFolder, KEY_FILE_NAME)
            val newKey = CookieEncryption.generateKey()
            
            saveKeyToFile(keyFile, newKey)
            encryptionKey = newKey
            
            console().sendInfo("securityKeyRegenerated")
            true
            
        } catch (e: Exception) {
            console().sendInfo("securityKeyRegenerateFailed", e.message ?: "unknown")
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
            console().sendInfo("securityKeyManagerCleaned")
        } catch (e: Exception) {
            console().sendInfo("securityKeyManagerCleanupError", e.message ?: "unknown")
        }
    }
}