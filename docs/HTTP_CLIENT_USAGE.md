# Bilibili API HTTP客户端使用指南

## 📚 概述

本项目使用 **OkHttp** 作为HTTP客户端库来与Bilibili API交互。OkHttp是一个高效、易用的HTTP客户端，非常适合Android和服务端应用。

## 🚀 快速开始

### 1. 基本API调用

```kotlin
import online.bingzi.bilibili.video.pro.internal.network.BilibiliApiManager

// 获取视频信息
BilibiliApiManager.getVideoInfoAsync("BV1xx411c7mD") { videoInfo ->
    if (videoInfo != null) {
        println("视频标题: ${videoInfo.title}")
        println("作者: ${videoInfo.owner.name}")
    } else {
        println("获取视频信息失败")
    }
}

// 检查API状态
BilibiliApiManager.checkApiStatusAsync { isOnline ->
    if (isOnline) {
        println("Bilibili API 连接正常")
    } else {
        println("Bilibili API 连接失败")
    }
}
```

### 2. 用户Cookie管理

```kotlin
// 添加用户Cookie（需要用户登录Bilibili后获取）
val userCookie = "SESSDATA=xxx; bili_jct=xxx; DedeUserID=xxx"
BilibiliApiManager.addUserCookie("123456789", userCookie)

// 检查用户三连状态
BilibiliApiManager.getTripleStatusAsync("123456789", "BV1xx411c7mD") { tripleStatus ->
    tripleStatus?.let {
        println("点赞: ${it.like == 1}")
        println("投币: ${it.coin}个")
        println("收藏: ${it.favorite == 1}")
    }
}
```

### 3. 批量操作

```kotlin
// 批量检查多个用户的三连状态
val userIds = listOf("123456789", "987654321", "111222333")
BilibiliApiManager.batchCheckTripleStatusAsync(userIds, "BV1xx411c7mD") { results ->
    results.forEach { (uid, status) ->
        println("用户 $uid: ${status?.let { "已操作" } ?: "未操作"}")
    }
}
```

## 🔧 工具类使用

### BilibiliHelper 工具方法

```kotlin
import online.bingzi.bilibili.video.pro.internal.helper.BilibiliHelper

// BV号和AV号转换
val bvid = "BV1xx411c7mD"
val aid = BilibiliHelper.bv2av(bvid)
println("AV号: $aid")

val convertedBv = BilibiliHelper.av2bv(aid!!)
println("转换回BV号: $convertedBv")

// 验证格式
if (BilibiliHelper.isValidBvid(bvid)) {
    println("BV号格式正确")
}

// 从文本中提取视频ID
val text = "推荐视频：BV1xx411c7mD 和 av123456789"
val bvids = BilibiliHelper.extractBvids(text)
val aids = BilibiliHelper.extractAids(text)
println("提取到的BV号: $bvids")
println("提取到的AV号: $aids")

// 格式化播放量
val viewCount = 1234567
println("播放量: ${BilibiliHelper.formatViewCount(viewCount)}")

// 时间格式化
val timestamp = System.currentTimeMillis() - 3600000 // 1小时前
println("发布时间: ${BilibiliHelper.formatTimeAgo(timestamp)}")
```

### 数据转换

```kotlin
// API响应转换为实体类
BilibiliApiManager.getVideoInfoAsync("BV1xx411c7mD") { apiVideoInfo ->
    apiVideoInfo?.let {
        // 转换为插件的实体类
        val videoEntity = BilibiliHelper.convertToEntity(it)

        // 现在可以用于事件系统
        val user = BilibiliUser("123456789", "测试用户")
        val tripleEvent = BilibiliTripleActionEvent(
            user = user,
            video = videoEntity,
            actionData = TripleActionData(liked = true, coined = true, coinCount = 2)
        )
    }
}
```

## 📊 监控和调试

### API调用统计

```kotlin
// 获取API调用统计
val stats = BilibiliApiManager.getApiStats()
println("API调用统计: $stats")

// 获取详细报告
val report = BilibiliApiManager.getApiStatsReport()
println(report)

// 重置统计
BilibiliApiManager.resetApiStats()
```

### 连接池监控

```kotlin
// 查看当前管理的用户数
val userCount = BilibiliApiManager.getOnlineUserCount()
println("当前在线用户数: $userCount")

// 获取所有用户ID
val allUsers = BilibiliApiManager.getAllUserIds()
println("所有用户: $allUsers")
```

## ⚙️ 配置选项

### 1. 日志级别调整

在 `BilibiliApiClient` 中修改日志级别：

```kotlin
val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY // 查看完整请求和响应
    // level = HttpLoggingInterceptor.Level.BASIC // 只看基本信息
    // level = HttpLoggingInterceptor.Level.NONE // 不记录日志
}
```

### 2. 超时设置

```kotlin
httpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)    // 连接超时
    .readTimeout(60, TimeUnit.SECONDS)       // 读取超时
    .writeTimeout(15, TimeUnit.SECONDS)      // 写入超时
    .build()
```

### 3. 重试策略

```kotlin
httpClient = OkHttpClient.Builder()
    .retryOnConnectionFailure(true)          // 连接失败时重试
    .addInterceptor(customRetryInterceptor)  // 自定义重试逻辑
    .build()
```

## 🛡️ 错误处理

### 常见错误和解决方案

```kotlin
BilibiliApiManager.getVideoInfoAsync("BV1xx411c7mD") { videoInfo ->
    when {
        videoInfo == null -> {
            // 可能的原因：
            // 1. 网络连接问题
            // 2. BV号不存在或格式错误
            // 3. API返回错误
            println("获取视频信息失败，请检查网络连接和BV号格式")
        }
        videoInfo.title.isEmpty() -> {
            println("视频可能被删除或设为私密")
        }
        else -> {
            println("视频信息获取成功: ${videoInfo.title}")
        }
    }
}
```

### Cookie相关错误

```kotlin
val cookie = "SESSDATA=xxx; bili_jct=xxx"

// 验证Cookie格式
if (!BilibiliHelper.isValidCookie(cookie)) {
    println("Cookie格式无效，请确保包含必要字段")
    return
}

// 添加Cookie并测试
BilibiliApiManager.addUserCookie("123456789", cookie)
BilibiliApiManager.getTripleStatusAsync("123456789", "BV1xx411c7mD") { status ->
    if (status == null) {
        println("Cookie可能已过期或无效，请重新获取")
    }
}
```

## 🔒 安全考虑

### 1. Cookie存储

```kotlin
// ❌ 不要在代码中硬编码Cookie
val cookie = "SESSDATA=abc123..."

// ✅ 从配置文件或数据库中读取
val cookie = loadCookieFromConfig(userId)

// ✅ 定期检查Cookie有效性
fun validateUserCookie(uid: String) {
    BilibiliApiManager.getTripleStatusAsync(uid, "BV1xx411c7mD") { status ->
        if (status == null) {
            // Cookie可能失效，通知用户重新登录
            notifyUserToRelogin(uid)
        }
    }
}
```

### 2. 请求频率控制

```kotlin
// 可以在拦截器中添加请求频率限制
class RateLimitInterceptor(private val maxRequestsPerSecond: Int) : Interceptor {
    private var lastRequestTime = 0L
    private val minInterval = 1000L / maxRequestsPerSecond

    override fun intercept(chain: Interceptor.Chain): Response {
        val now = System.currentTimeMillis()
        val elapsed = now - lastRequestTime

        if (elapsed < minInterval) {
            Thread.sleep(minInterval - elapsed)
        }

        lastRequestTime = System.currentTimeMillis()
        return chain.proceed(chain.request())
    }
}
```

## 📈 性能优化

### 1. 连接池复用

OkHttp自动管理连接池，但可以调整参数：

```kotlin
val connectionPool = ConnectionPool(
    maxIdleConnections = 10,      // 最大空闲连接数
    keepAliveDuration = 5,        // 连接保持时间
    TimeUnit.MINUTES
)

httpClient = OkHttpClient.Builder()
    .connectionPool(connectionPool)
    .build()
```

### 2. 响应缓存

```kotlin
val cache = Cache(File("http_cache"), 10 * 1024 * 1024) // 10MB缓存

httpClient = OkHttpClient.Builder()
    .cache(cache)
    .build()
```

### 3. 异步处理

```kotlin
// ✅ 使用异步方法，避免阻塞主线程
BilibiliApiManager.getVideoInfoAsync("BV1xx411c7mD") { videoInfo ->
    // 在主线程中处理结果
}

// ❌ 避免在主线程中同步调用
// val videoInfo = api.getVideoInfo("BV1xx411c7mD") // 会阻塞主线程
```

## 🧪 测试建议

### 单元测试示例

```kotlin
@Test
fun testBvToAvConversion() {
    val bvid = "BV1xx411c7mD"
    val aid = BilibiliHelper.bv2av(bvid)
    assertNotNull(aid)

    val convertedBv = BilibiliHelper.av2bv(aid!!)
    assertEquals(bvid, convertedBv)
}

@Test
fun testApiConnection() {
    var result: Boolean? = null
    val latch = CountDownLatch(1)

    BilibiliApiManager.checkApiStatusAsync { isOnline ->
        result = isOnline
        latch.countDown()
    }

    latch.await(10, TimeUnit.SECONDS)
    assertTrue(result ?: false)
}
```

## 🔄 其他HTTP客户端选择

如果您想更换HTTP客户端，这里是一些备选方案：

### 1. **Java 11+ HttpClient**

```kotlin
// 优点：JDK内置，无额外依赖
// 缺点：功能相对简单，Java 11+才支持

val httpClient = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .build()
```

### 2. **Apache HttpClient**

```kotlin
// 优点：功能丰富，配置灵活
// 缺点：依赖较大，API相对复杂

val httpClient = HttpClients.custom()
    .setConnectionTimeToLive(30, TimeUnit.SECONDS)
    .setMaxConnPerRoute(20)
    .build()
```

### 3. **Fuel (Kotlin专用)**

```kotlin
// 优点：Kotlin原生设计，API简洁
// 缺点：相对较新，社区较小

Fuel.get("/x/web-interface/view")
    .parameters(listOf("bvid" to "BV1xx411c7mD"))
    .responseJson { request, response, result ->
        // 处理结果
    }
```

## 📝 总结

OkHttp是目前最适合您项目的选择，因为它：

- ✅ **性能优秀**：连接池、HTTP/2支持
- ✅ **易于使用**：简洁的API设计
- ✅ **功能丰富**：拦截器、缓存、重试等
- ✅ **社区成熟**：广泛使用，文档完善
- ✅ **Kotlin友好**：与Kotlin协程配合良好

通过本指南，您应该能够：

1. 快速上手使用Bilibili API客户端
2. 理解各种配置选项和最佳实践
3. 处理常见错误和性能优化
4. 根据需要选择其他HTTP客户端

如有任何问题，请参考OkHttp官方文档或在项目中提出Issue！[喵~] 