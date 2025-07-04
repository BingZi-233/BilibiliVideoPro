# BilibiliVideoPro

<div align="center">

![Bilibili](https://img.shields.io/badge/Bilibili-FC4D9A?style=for-the-badge&logo=bilibili&logoColor=white)
![Minecraft](https://img.shields.io/badge/Minecraft-62B47A?style=for-the-badge&logo=minecraft&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)

**一个强大的 Minecraft 哔哩哔哩互动插件**

支持QR码登录、一键三联检查、自定义奖励系统

[特性介绍](#-特性介绍) • [快速开始](#-快速开始) • [命令使用](#-命令使用) • [配置说明](#-配置说明) • [常见问题](#-常见问题)

</div>

---

## 🌟 特性介绍

### 🔐 账户绑定系统
- **QR码登录**: 游戏内生成二维码地图物品，使用哔哩哔哩APP扫码登录
- **一对一绑定**: 每个玩家只能绑定一个哔哩哔哩账户
- **安全可靠**: 采用官方API，支持Cookie自动管理和刷新

### 📹 一键三联检查
- **智能检测**: 自动检查指定BV号的点赞、投币、收藏状态
- **实时反馈**: 显示详细的三联完成情况
- **防刷机制**: 内置冷却时间，防止重复刷奖励

### 🎁 自定义奖励系统
- **Kether脚本**: 支持强大的Kether脚本执行奖励
- **多样化配置**: 不同BV号可配置不同奖励
- **灵活控制**: 支持启用/禁用特定视频的奖励

### 📊 数据统计
- **完整记录**: 记录玩家的所有互动数据
- **统计分析**: 查看个人一键三联次数和总互动数
- **数据持久化**: 支持SQLite和MySQL数据库

---

## 🚀 快速开始

### 📋 前置要求

- **Minecraft服务端**: Spigot/Paper 1.8+ 
- **Java版本**: Java 8+
- **必需插件**: ProtocolLib
- **可选插件**: PlaceholderAPI

### 📦 安装步骤

1. **下载插件**
   ```bash
   # 下载最新版本
   wget https://github.com/BingZi-233/BilibiliVideoPro/releases/latest/download/BilibiliVideoPro.jar
   ```

2. **安装依赖**
   - 下载并安装 [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)
   - 将两个插件放入 `plugins` 文件夹

3. **启动服务器**
   ```bash
   # 启动服务器，插件将自动生成配置文件
   java -jar server.jar
   ```

4. **配置插件**
   - 编辑 `plugins/BilibiliVideoPro/config.yml`
   - 编辑 `plugins/BilibiliVideoPro/database.yml` (可选)

---

## 🎮 命令使用

### 基础命令

| 命令 | 权限 | 描述 | 示例 |
|------|------|------|------|
| `/bvp` | `bilibilipro.use` | 显示帮助信息 | `/bvp` |
| `/bvp login` | `bilibilipro.use` | 开始登录流程 | `/bvp login` |
| `/bvp check <BV号>` | `bilibilipro.use` | 检查一键三联 | `/bvp check BV1234567890` |
| `/bvp status` | `bilibilipro.use` | 查看个人状态 | `/bvp status` |
| `/bvp info` | `bilibilipro.use` | 查看插件信息 | `/bvp info` |

### 管理员命令

| 命令 | 权限 | 描述 | 示例 |
|------|------|------|------|
| `/bvp reload` | `bilibilipro.admin` | 重载配置文件 | `/bvp reload` |
| `/bvp unbind <玩家>` | `bilibilipro.admin` | 解绑玩家账户 | `/bvp unbind Steve` |

### 📖 使用教程

#### 1. 🔑 首次登录
```bash
# 1. 输入登录命令
/bvp login

# 2. 查看物品栏中的地图物品 (二维码)
# 3. 使用哔哩哔哩手机APP扫描二维码
# 4. 在手机上确认登录
# 5. 等待登录成功提示
```

#### 2. 📹 检查视频一键三联
```bash
# 检查指定BV号的一键三联状态
/bvp check BV1234567890

# 系统会显示:
# ✓ 点赞状态
# ✓ 投币状态  
# ✓ 收藏状态
# 如果全部完成，将获得奖励！
```

#### 3. 📊 查看个人状态
```bash
# 查看绑定信息和统计数据
/bvp status

# 显示内容包括:
# - 绑定的哔哩哔哩账户
# - 绑定时间
# - 总互动次数
# - 一键三联完成次数
```

---

## ⚙️ 配置说明

### 🎁 奖励配置 (config.yml)

#### 默认奖励设置
```yaml
triple_action_rewards:
  enabled: true
  # 默认奖励 (当BV号没有特定配置时使用)
  default:
    enabled: true
    reward_script: |
      tell *"&a恭喜！您已完成一键三联！"
      give diamond 1
      sound ENTITY_EXPERIENCE_ORB_PICKUP 1 1
```

#### 特定BV号奖励
```yaml
  # 特定BV号的奖励配置
  specific_videos:
    # 高价值奖励示例
    "BV1234567890":
      enabled: true
      reward_script: |
        tell *"&6&l恭喜完成特殊视频的一键三联！"
        tell *"&e获得豪华奖励包！"
        give diamond 5
        give emerald 3
        give gold_ingot 10
        sound ENTITY_PLAYER_LEVELUP 1 1
        
    # 经验奖励示例
    "BV0987654321":
      enabled: true
      reward_script: |
        tell *"&b恭喜完成教程视频的一键三联！"
        give experience 100
        sound ENTITY_EXPERIENCE_ORB_PICKUP 1 2
        
    # 禁用奖励示例
    "BV1111111111":
      enabled: false
      reward_script: |
        tell *"&c此视频暂不提供奖励"
```

#### 冷却时间设置
```yaml
  cooldown:
    enabled: true
    per_video: 3600  # 每个BV号1小时冷却
    global: 300      # 全局5分钟冷却
```

### 🔐 登录配置
```yaml
login:
  qr_expire_time: 180      # QR码过期时间(秒)
  check_interval: 2000     # 登录检查间隔(毫秒)
  max_check_attempts: 90   # 最大检查次数
```

### 🗄️ 数据库配置 (database.yml)
```yaml
# 数据库类型选择
database:
  type: "sqlite"  # 或 "mysql"
  
  # SQLite配置 (推荐新手使用)
  sqlite:
    file: "data.db"
    
  # MySQL配置 (适合多服务器)
  mysql:
    host: "localhost"
    port: 3306
    database: "bilibili_video_pro"
    username: "root"
    password: "password"
```

---

## 🎨 Kether脚本示例

### 💎 基础奖励
```yaml
reward_script: |
  tell *"&a恭喜完成一键三联！"
  give diamond 1
  sound ENTITY_EXPERIENCE_ORB_PICKUP 1 1
```

### 🏆 豪华奖励包
```yaml
reward_script: |
  tell *"&6&l豪华奖励包！"
  give diamond 5
  give emerald 3
  give gold_ingot 10
  give experience 200
  sound ENTITY_PLAYER_LEVELUP 1 1
  title *"" "&6&l恭喜获得豪华奖励！" 10 60 10
```

### 💰 经济奖励 (需要经济插件)
```yaml
reward_script: |
  tell *"&e获得金币奖励！"
  eco give *player 1000
  sound BLOCK_ANVIL_USE 1 1
```

### 🎉 随机奖励
```yaml
reward_script: |
  tell *"&d幸运抽奖开始！"
  random 3
  case 0:
    give diamond 3
    tell *"&b获得钻石奖励！"
  case 1:
    give emerald 5
    tell *"&a获得绿宝石奖励！"
  case 2:
    give experience 150
    tell *"&e获得经验奖励！"
  sound ENTITY_EXPERIENCE_ORB_PICKUP 1 1
```

---

## 🛠️ 常见问题

### ❓ 登录相关

**Q: 二维码无法显示或显示异常？**
```
A: 1. 确保已安装ProtocolLib插件
   2. 检查服务器版本兼容性
   3. 重新输入 /bvp login 命令
```

**Q: 扫码后显示登录失败？**
```
A: 1. 检查网络连接是否正常
   2. 确认使用官方哔哩哔哩APP扫码
   3. 重试登录流程
```

**Q: 提示"已绑定账户"但想更换？**
```
A: 请联系管理员使用 /bvp unbind <你的用户名> 解绑后重新登录
```

### ❓ 奖励相关

**Q: 完成一键三联但没有获得奖励？**
```
A: 1. 检查是否在冷却时间内
   2. 确认config.yml中奖励已启用
   3. 查看控制台是否有脚本执行错误
```

**Q: 如何为特定视频设置特殊奖励？**
```
A: 在config.yml的specific_videos部分添加对应BV号的配置
   参考上方"特定BV号奖励"示例
```

**Q: Kether脚本执行失败？**
```
A: 1. 检查脚本语法是否正确
   2. 确认使用的命令和物品ID是否有效
   3. 查看控制台错误日志
```

### ❓ 配置相关

**Q: 如何修改冷却时间？**
```
A: 编辑config.yml中的cooldown部分
   per_video: 每个视频的冷却时间(秒)
   global: 全局冷却时间(秒)
```

**Q: 支持哪些数据库？**
```
A: 支持SQLite(默认)和MySQL
   新手推荐使用SQLite，多服务器环境推荐MySQL
```

**Q: 如何备份数据？**
```
A: SQLite: 备份plugins/BilibiliVideoPro/data.db文件
   MySQL: 使用mysqldump备份数据库
```

---

## 🔧 开发信息

### 📁 项目结构
```
src/main/kotlin/online/bingzi/bilibili/video/pro/internal/
├── command/          # 命令处理器
├── database/         # 数据库相关
├── network/          # 网络API
├── helper/           # 工具类
└── manager/          # 管理器
```

### 🧩 核心依赖
- **TabooLib 6.2.3**: 插件框架
- **OkHttp3 4.12.0**: HTTP客户端
- **ORMLite**: ORM数据库
- **Google ZXing**: 二维码生成
- **ProtocolLib**: 数据包处理

### 🏗️ 构建项目
```bash
# 克隆项目
git clone https://github.com/BingZi-233/BilibiliVideoPro.git

# 构建插件
cd BilibiliVideoPro
./gradlew build

# 输出文件位置
# build/libs/BilibiliVideoPro-*.jar
```

---

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

### 贡献指南
1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

---

## 📞 支持

- **文档**: [项目Wiki](https://github.com/BingZi-233/BilibiliVideoPro/wiki)
- **问题反馈**: [GitHub Issues](https://github.com/BingZi-233/BilibiliVideoPro/issues)
- **讨论**: [GitHub Discussions](https://github.com/BingZi-233/BilibiliVideoPro/discussions)

---

<div align="center">

**如果这个项目对你有帮助，请给个 ⭐ Star 支持一下！**

Made with ❤️ by [BingZi-233](https://github.com/BingZi-233)

</div>