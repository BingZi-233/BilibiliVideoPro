# BilibiliVideoPro

BilibiliVideoPro 是一个用于管理 Bilibili 视频互动和用户绑定的插件，支持二维码登录、视频互动状态检查、数据库管理等功能。

## 功能概述

### 网络功能
- **二维码扫码登录**: 生成二维码并轮询登录状态
- **视频互动状态检查**: 检查视频的点赞、投币、收藏状态
- **关注状态检查**: 检查是否关注指定UP主
- **评论状态检查**: 检查用户是否在评论区留言
- **Cookie管理**: 自动刷新Cookie保持登录状态

### 数据库功能
- **玩家绑定管理**: 存储玩家与Bilibili账户的绑定信息
- **视频互动记录**: 记录视频的互动状态
- **SQLite和MySQL支持**: 支持多种数据库类型

### Helper工具
- **二维码生成**: 生成二维码图像
- **地图物品构建**: 构建包含二维码的Minecraft地图物品

## 快速开始

1. **配置数据库**
   - 编辑 `database.yml` 文件，配置数据库类型和连接信息。

2. **使用网络功能**
   ```kotlin
   val networkManager = BilibiliNetworkManager.getInstance()
   networkManager.initialize()
   ```

3. **使用数据库功能**
   ```kotlin
   val playerService = PlayerBilibiliService()
   val binding = playerService.findByPlayerUuid(playerUuid)
   ```

4. **使用Helper工具**
   ```kotlin
   val qrImage = QRCodeHelper.generateQRCode("Hello World!", 128)
   ```

## 详细文档

- [网络功能使用指南](docs/NETWORK_USAGE.md)
- [数据库使用指南](docs/DATABASE_USAGE.md)
- [Helper工具使用说明](docs/HELPER_USAGE.md)
- [功能实现总结](docs/IMPLEMENTATION_SUMMARY.md)

## 依赖

- **Google ZXing**: 用于二维码生成
- **OkHttp3**: 用于HTTP请求
- **TabooLib**: 用于配置和生命周期管理

## 注意事项

- **API限流**: B站API有调用频率限制，避免过于频繁的请求
- **Cookie安全**: 妥善保管Cookie信息，避免泄露
- **异步操作**: 二维码登录是异步的，注意处理回调

## 后续扩展

- 支持更多B站API功能
- 添加二维码扫描功能
- 优化用户体验

---

如有问题或建议，请在项目的Issue中提出。