# Helper 使用说明

本项目包含了一系列Helper类，用于简化常见的功能开发。所有Helper都位于 `internal.helper` 包下。

## 二维码相关Helper

### QRCodeHelper - 二维码生成Helper

提供将字符串转换为二维码图像的功能。

#### 主要功能

1. **生成二维码图像**
   ```kotlin
   val qrImage = QRCodeHelper.generateQRCode("Hello World!", 128)
   ```

2. **支持多种内容类型**
   - 普通文本
   - URL链接
   - 中文内容
   - JSON数据

#### 技术特性

- 使用 Google ZXing 库生成标准QR码
- 提供简化版本作为后备方案
- 支持自定义大小和错误纠正级别
- UTF-8编码支持中文

### MapItemHelper - 地图物品构建Helper

提供构建包含二维码的Minecraft地图物品功能，基于TabooLib的物品构建系统。

#### 主要方法

1. **创建基础二维码地图**
   ```kotlin
   val qrMap = MapItemHelper.createQRCodeMapItem(
       text = "https://www.bilibili.com",
       displayName = "&a哔哩哔哩官网",
       lore = listOf("&7扫描二维码访问网站")
   )
   ```

2. **创建样式化二维码地图**
   ```kotlin
   val styledMap = MapItemHelper.createStyledQRCodeMapItem(
       text = "https://www.bilibili.com",
       title = "哔哩哔哩",
       description = "弹幕视频网站",
       category = "网站"
   )
   ```

3. **创建哔哩哔哩登录二维码**
   ```kotlin
   val loginMap = MapItemHelper.createBilibiliLoginQRCodeMap(
       qrCodeUrl = "https://passport.bilibili.com/qrcode/xxx",
       qrKey = "qrcode_key_123"
   )
   ```

4. **创建视频分享二维码**
   ```kotlin
   val videoMap = MapItemHelper.createVideoShareQRCodeMap(
       videoUrl = "https://www.bilibili.com/video/BV1234567890",
       videoTitle = "精彩视频标题",
       videoAuthor = "UP主名称"
   )
   ```

5. **批量创建二维码地图**
   ```kotlin
   val batchMaps = MapItemHelper.createBatchQRCodeMaps(listOf(
       "https://github.com" to "GitHub",
       "https://www.google.com" to "谷歌搜索"
   ))
   ```

6. **工具方法**
   ```kotlin
   // 检查物品是否为二维码地图
   val isQRMap = MapItemHelper.isQRCodeMap(itemStack)
   
   // 从二维码地图中提取内容
   val content = MapItemHelper.extractQRCodeContent(itemStack)
   ```

### QRCodeExampleHelper - 二维码功能示例

展示如何在实际场景中使用二维码功能的示例Helper。

#### 使用示例

1. **给玩家发送登录二维码**
   ```kotlin
   QRCodeExampleHelper.giveBilibiliLoginQRCode(
       player = player,
       qrCodeUrl = "https://passport.bilibili.com/qrcode/xxx",
       qrKey = "qrcode_key_123"
   )
   ```

2. **给玩家发送视频分享二维码**
   ```kotlin
   QRCodeExampleHelper.giveVideoShareQRCode(
       player = player,
       videoUrl = "https://www.bilibili.com/video/BV1234567890",
       videoTitle = "我的视频作品",
       videoAuthor = "创作者"
   )
   ```

3. **给玩家发送自定义二维码**
   ```kotlin
   QRCodeExampleHelper.giveCustomInfoQRCode(
       player = player,
       content = "自定义信息内容",
       title = "信息标题",
       description = "详细描述"
   )
   ```

4. **检查玩家手中的二维码**
   ```kotlin
   if (QRCodeExampleHelper.isPlayerHoldingQRCodeMap(player)) {
       val content = QRCodeExampleHelper.getQRCodeContentFromPlayerHand(player)
       // 处理二维码内容
   }
   ```

## NMSHelper - 地图数据包处理Helper

提供使用 ProtocolLib 发送地图数据包的可靠功能。已重构以正确使用 ProtocolLib 4.8.0+ 和现代 Minecraft 版本 (1.19.4+)。

#### 主要功能

1.  **发送地图像素数据**
    用于在地图上绘制图像。
    ```kotlin
    NMSHelper.sendMapData(player, mapId, data)
    ```

2.  **更新地图属性**
    例如缩放级别或锁定状态，而不改变像素数据。
    ```kotlin
    NMSHelper.updateMapProperties(player, mapId, scale, trackingPosition, locked)
    ```

#### 技术特性

-   使用 ProtocolLib 库处理地图数据包。
-   支持发送完整的地图数据包，包括地图ID、缩放级别、锁定状态、更新区域的像素数据等。
-   包含错误处理机制。

## 集成建议

### 在现有登录系统中使用

在 `QRCodeLoginService` 中集成二维码地图功能：

```kotlin
// 生成登录二维码后
val qrCodeMap = MapItemHelper.createBilibiliLoginQRCodeMap(qrCodeUrl, qrKey)
player.inventory.addItem(qrCodeMap)
```

### 在视频分享功能中使用

```kotlin
// 在视频互动服务中添加
fun shareVideoAsQRCode(player: Player, videoUrl: String, videoTitle: String) {
    val shareMap = MapItemHelper.createVideoShareQRCodeMap(videoUrl, videoTitle)
    player.inventory.addItem(shareMap)
}
```

### 命令行集成

可以创建命令来快速生成二维码：

```kotlin
@CommandBody
fun qrcode(sender: CommandSender, @Argument text: String) {
    if (sender is Player) {
        QRCodeExampleHelper.giveCustomInfoQRCode(sender, text, "自定义二维码")
    }
}
```

## 依赖说明

项目已在 `build.gradle.kts` 中添加了必要的依赖：

```kotlin
// 二维码生成库
implementation("com.google.zxing:core:3.5.2")
implementation("com.google.zxing:javase:3.5.2")

// ProtocolLib for packet handling
implementation("com.comphenix.protocol:ProtocolLib:4.8.0")
```

## 注意事项

1. **性能考虑**: 二维码生成是CPU密集型操作，建议在异步线程中执行
2. **内存管理**: 大量生成二维码时注意内存使用
3. **错误处理**: 已实现了异常捕获和后备方案
4. **编码支持**: 完全支持UTF-8编码的中文内容
5. **地图限制**: Minecraft地图大小固定为128x128像素

## 扩展建议

1. 可以添加二维码扫描功能（需要额外的图像识别库）
2. 可以支持更多的二维码样式和颜色
3. 可以添加二维码内容的加密/解密功能
4. 可以集成到GUI界面中提供更好的用户体验 