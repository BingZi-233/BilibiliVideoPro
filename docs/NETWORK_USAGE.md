# Bilibili网络功能使用指南

本文档介绍如何使用BilibiliVideoPro插件的网络功能，包括二维码登录、视频互动状态检查、关注状态检查、评论检查和Cookie管理等功能。

## 目录

1. [功能概述](#功能概述)
2. [快速开始](#快速开始)
3. [二维码登录](#二维码登录)
4. [视频互动状态检查](#视频互动状态检查)
5. [关注状态检查](#关注状态检查)
6. [评论状态检查](#评论状态检查)
7. [Cookie管理](#cookie管理)
8. [API参考](#api参考)
9. [错误处理](#错误处理)
10. [最佳实践](#最佳实践)

## 功能概述

### 已实现的功能

- ✅ **二维码扫码登录**: 生成二维码，轮询登录状态
- ✅ **三连状态检查**: 获取视频的点赞、投币、收藏状态
- ✅ **关注状态检查**: 检查是否关注指定UP主
- ✅ **评论状态检查**: 检查用户是否在评论区留言
- ✅ **Cookie轮换刷新**: 自动刷新Cookie保持登录状态
- ✅ **统一网络管理**: 提供单例模式的网络管理器

### 技术特性

- 基于OkHttp3的HTTP客户端
- 使用Gson进行JSON解析
- 线程安全的Cookie管理
- 自动Cookie刷新机制
- 完善的错误处理

## 快速开始

### 1. 获取网络管理器实例

```kotlin
val networkManager = BilibiliNetworkManager.getInstance()
```

### 2. 初始化网络管理器

```kotlin
networkManager.initialize()
```

### 3. 检查登录状态

```kotlin
if (networkManager.isLoggedIn()) {
    println("用户已登录")
} else {
    println("用户未登录，需要先登录")
}
```

## 二维码登录

### 生成二维码并开始登录流程

```kotlin
networkManager.qrCodeLogin.startQRCodeLogin(
    onQRCodeGenerated = { qrData ->
        println("二维码生成成功:")
        println("二维码URL: ${qrData.url}")
        println("请使用B站手机APP扫描二维码")
        
        // 在这里可以将二维码URL转换为图像显示给用户
        // 或者直接输出URL让用户手动输入到浏览器
    },
    onStatusChanged = { status ->
        println("登录状态: $status")
    },
    onLoginSuccess = {
        println("登录成功!")
        // 启动自动Cookie刷新
        networkManager.cookieRefresh.startAutoRefresh()
    },
    onError = { error ->
        println("登录失败: $error")
    }
)
```

### 手动控制二维码登录

```kotlin
// 1. 生成二维码
val qrResult = networkManager.qrCodeLogin.generateQRCode()
when (qrResult) {
    is QRCodeResult.Success -> {
        val qrData = qrResult.data
        println("二维码URL: ${qrData.url}")
        
        // 2. 轮询登录状态
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val pollResult = networkManager.qrCodeLogin.pollLoginStatus(qrData.qrcodeKey)
                when (pollResult) {
                    is LoginPollResult.Success -> {
                        timer.cancel()
                        println("登录成功!")
                    }
                    is LoginPollResult.Expired -> {
                        timer.cancel()
                        println("二维码已过期")
                    }
                    is LoginPollResult.Error -> {
                        timer.cancel()
                        println("登录错误: ${pollResult.message}")
                    }
                    is LoginPollResult.WaitingScan -> {
                        println("等待扫描...")
                    }
                    is LoginPollResult.WaitingConfirm -> {
                        println("等待确认...")
                    }
                }
            }
        }, 0, 3000) // 每3秒检查一次
    }
    is QRCodeResult.Error -> {
        println("生成二维码失败: ${qrResult.message}")
    }
}
```

## 视频互动状态检查

### 获取完整的视频互动状态

```kotlin
val bvid = "BV1xx411c7mD" // 替换为实际的BVID

when (val result = networkManager.videoInteraction.getVideoInteractionStatus(bvid)) {
    is VideoInteractionResult.Success -> {
        val status = result.status
        val video = status.videoData
        val triple = status.tripleAction
        
        println("视频信息:")
        println("  标题: ${video.title}")
        println("  UP主ID: ${video.ownerMid}")
        
        println("三连状态:")
        println("  已点赞: ${triple.isLiked}")
        println("  已投币: ${triple.isCoined} (${triple.coinCount}枚)")
        println("  已收藏: ${triple.isFavorited}")
        
        println("其他状态:")
        println("  已关注UP主: ${status.isFollowingUp}")
        println("  已评论: ${status.hasCommented}")
    }
    is VideoInteractionResult.Error -> {
        println("获取视频状态失败: ${result.message}")
    }
}
```

### 仅检查三连状态

```kotlin
when (val result = networkManager.videoInteraction.getTripleActionStatus(bvid)) {
    is TripleActionResult.Success -> {
        val status = result.status
        println("点赞: ${if (status.isLiked) "✓" else "✗"}")
        println("投币: ${if (status.isCoined) "✓ (${status.coinCount}枚)" else "✗"}")
        println("收藏: ${if (status.isFavorited) "✓" else "✗"}")
    }
    is TripleActionResult.Error -> {
        println("获取三连状态失败: ${result.message}")
    }
}
```

## 关注状态检查

```kotlin
val upMid = 123456789L // UP主的用户ID

when (val result = networkManager.videoInteraction.checkFollowStatus(upMid)) {
    is FollowResult.Success -> {
        if (result.isFollowing) {
            println("已关注该UP主")
        } else {
            println("未关注该UP主")
        }
    }
    is FollowResult.Error -> {
        println("检查关注状态失败: ${result.message}")
    }
}
```

## 评论状态检查

### 检查当前登录用户是否评论

```kotlin
when (val result = networkManager.videoInteraction.checkUserCommented(bvid)) {
    is CommentCheckResult.Success -> {
        if (result.hasCommented) {
            println("当前用户已在该视频评论区留言")
        } else {
            println("当前用户未在该视频评论区留言")
        }
    }
    is CommentCheckResult.Error -> {
        println("检查评论状态失败: ${result.message}")
    }
}
```

### 检查指定用户是否评论

```kotlin
val targetUserId = 123456789L

when (val result = networkManager.videoInteraction.checkUserCommented(bvid, targetUserId)) {
    is CommentCheckResult.Success -> {
        if (result.hasCommented) {
            println("用户 $targetUserId 已在该视频评论区留言")
        } else {
            println("用户 $targetUserId 未在该视频评论区留言")
        }
    }
    is CommentCheckResult.Error -> {
        println("检查评论状态失败: ${result.message}")
    }
}
```

## Cookie管理

### 手动刷新Cookie

```kotlin
when (val result = networkManager.cookieRefresh.refreshCookie()) {
    is CookieRefreshResult.Success -> {
        println("Cookie刷新成功: ${result.message}")
    }
    is CookieRefreshResult.Error -> {
        println("Cookie刷新失败: ${result.message}")
    }
}
```

### 启动自动Cookie刷新

```kotlin
// 使用默认间隔（25天）
networkManager.cookieRefresh.startAutoRefresh()

// 或者自定义间隔（单位：小时）
networkManager.cookieRefresh.startAutoRefresh(intervalHours = 12 * 24) // 12天
```

### 停止自动Cookie刷新

```kotlin
networkManager.cookieRefresh.stopAutoRefresh()
```

### 检查Cookie状态

```kotlin
// 检查Cookie是否有效
if (networkManager.cookieRefresh.validateCookie()) {
    println("Cookie有效")
} else {
    println("Cookie无效")
}

// 检查Cookie是否即将过期
if (networkManager.cookieRefresh.isCookieExpiringSoon()) {
    println("Cookie即将过期，建议刷新")
}
```

### 手动设置Cookie

```kotlin
val sessdata = "your_sessdata_value"
val biliJct = "your_bili_jct_value"
val dedeUserId = "your_user_id"
val dedeUserIdMd5 = "your_user_id_md5"

networkManager.setCookies(sessdata, biliJct, dedeUserId, dedeUserIdMd5)

// 验证设置的Cookie
if (networkManager.cookieRefresh.validateCookie()) {
    println("Cookie设置成功并验证有效")
    networkManager.cookieRefresh.startAutoRefresh()
} else {
    println("设置的Cookie无效")
}
```

## API参考

### BilibiliNetworkManager

主要的网络管理器，提供统一的访问接口。

#### 方法

- `getInstance()`: 获取单例实例
- `initialize()`: 初始化网络管理器
- `isLoggedIn()`: 检查是否已登录
- `setCookies(cookies: Map<String, String>)`: 设置Cookie
- `getCookies()`: 获取所有Cookie
- `logout()`: 登出并清理资源
- `destroy()`: 销毁网络管理器

#### 服务访问

- `qrCodeLogin`: 二维码登录服务
- `videoInteraction`: 视频互动服务  
- `cookieRefresh`: Cookie刷新服务

### QRCodeLoginService

二维码登录相关功能。

#### 方法

- `generateQRCode()`: 生成二维码
- `pollLoginStatus(qrcodeKey: String)`: 轮询登录状态
- `startQRCodeLogin(...)`: 开始完整的登录流程

### VideoInteractionService

视频互动状态检查功能。

#### 方法

- `getTripleActionStatus(bvid: String)`: 获取三连状态
- `checkFollowStatus(mid: Long)`: 检查关注状态
- `checkUserCommented(bvid: String, mid: Long?)`: 检查评论状态
- `getVideoInteractionStatus(bvid: String)`: 获取完整互动状态

### CookieRefreshService

Cookie管理和刷新功能。

#### 方法

- `refreshCookie()`: 手动刷新Cookie
- `startAutoRefresh(intervalHours: Long)`: 启动自动刷新
- `stopAutoRefresh()`: 停止自动刷新
- `validateCookie()`: 验证Cookie有效性
- `checkLoginStatus()`: 检查登录状态
- `isCookieExpiringSoon(thresholdDays: Int)`: 检查Cookie是否即将过期

## 错误处理

### 常见错误类型

1. **网络错误**: 网络连接失败、超时等
2. **API错误**: B站API返回错误码
3. **认证错误**: Cookie无效、登录状态过期
4. **数据格式错误**: API响应格式不符合预期

### 错误处理示例

```kotlin
when (val result = networkManager.videoInteraction.getTripleActionStatus(bvid)) {
    is TripleActionResult.Success -> {
        // 处理成功结果
        val status = result.status
        // ...
    }
    is TripleActionResult.Error -> {
        // 处理错误
        val errorMessage = result.message
        
        when {
            errorMessage.contains("未登录") -> {
                println("用户未登录，请先登录")
                // 可以启动登录流程
            }
            errorMessage.contains("网络") -> {
                println("网络错误，请检查网络连接")
                // 可以重试
            }
            errorMessage.contains("API错误") -> {
                println("API调用失败: $errorMessage")
                // 记录日志或上报错误
            }
            else -> {
                println("未知错误: $errorMessage")
            }
        }
    }
}
```

## 最佳实践

### 1. 资源管理

```kotlin
try {
    val networkManager = BilibiliNetworkManager.getInstance()
    networkManager.initialize()
    
    // 使用网络功能
    // ...
    
} finally {
    // 确保清理资源
    BilibiliNetworkManager.destroyInstance()
}
```

### 2. 登录状态检查

在执行需要登录的操作前，始终检查登录状态：

```kotlin
if (!networkManager.isLoggedIn()) {
    // 启动登录流程
    return
}

// 执行需要登录的操作
```

### 3. Cookie管理

- 登录成功后立即启动自动Cookie刷新
- 定期验证Cookie有效性
- 在Cookie即将过期时提前刷新

```kotlin
// 登录成功后
networkManager.cookieRefresh.startAutoRefresh()

// 定期检查
if (networkManager.cookieRefresh.isCookieExpiringSoon(7)) { // 7天内过期
    networkManager.cookieRefresh.refreshCookie()
}
```

### 4. 错误重试

对于网络错误，可以实现重试机制：

```kotlin
fun getTripleActionStatusWithRetry(bvid: String, maxRetries: Int = 3): TripleActionResult {
    repeat(maxRetries) { attempt ->
        val result = networkManager.videoInteraction.getTripleActionStatus(bvid)
        
        if (result is TripleActionResult.Success) {
            return result
        }
        
        if (result is TripleActionResult.Error && !result.message.contains("网络")) {
            // 非网络错误，不重试
            return result
        }
        
        if (attempt < maxRetries - 1) {
            Thread.sleep(1000 * (attempt + 1)) // 递增延迟
        }
    }
    
    return TripleActionResult.Error("重试${maxRetries}次后仍然失败")
}
```

### 5. 批量操作

对于需要检查多个视频的情况，建议实现批量操作以提高效率：

```kotlin
fun checkMultipleVideos(bvids: List<String>): Map<String, VideoInteractionResult> {
    return bvids.associateWith { bvid ->
        networkManager.videoInteraction.getVideoInteractionStatus(bvid)
    }
}
```

## 注意事项

1. **API限流**: B站API有调用频率限制，避免过于频繁的请求
2. **Cookie安全**: 妥善保管Cookie信息，避免泄露
3. **异步操作**: 二维码登录是异步的，注意处理回调
4. **线程安全**: 网络管理器是线程安全的，可以在多线程环境中使用
5. **资源清理**: 使用完毕后记得调用`destroyInstance()`清理资源

## 更新日志

- v1.0.0: 实现基础的网络功能
  - 二维码登录
  - 视频互动状态检查
  - Cookie管理
  - 统一的网络管理器

---

如有问题或建议，请在项目的Issue中提出。 