# BilibiliVideoPro PlaceholderAPI 变量文档

BilibiliVideoPro 插件提供了丰富的 PlaceholderAPI 变量，可以在其他插件中使用来显示玩家的Bilibili账户信息和统计数据。

## 使用方法

所有变量都使用以下格式：
```
%bilibiliVideoPro_<变量名>%
```

## 可用变量

### 基础账户信息

| 变量名 | 描述 | 示例返回值 |
|--------|------|-----------|
| `%bilibiliVideoPro_bound%` | 账户是否已绑定 | `true` / `false` |
| `%bilibiliVideoPro_username%` | Bilibili用户名 | `用户名123` / `未绑定` |
| `%bilibiliVideoPro_uid%` | Bilibili用户ID | `123456789` / `0` |
| `%bilibiliVideoPro_level%` | 用户等级 | `5` / `0` |
| `%bilibiliVideoPro_vip%` | 是否为大会员 | `是` / `否` |

### 时间信息

| 变量名 | 描述 | 示例返回值 |
|--------|------|-----------|
| `%bilibiliVideoPro_bind_time%` | 账户绑定时间 | `2024-01-15 14:30:25` / `未绑定` |
| `%bilibiliVideoPro_last_login%` | 最后登录时间 | `2024-01-20 10:15:30` / `从未登录` |
| `%bilibiliVideoPro_cooldown%` | 冷却时间剩余 | `30秒` / `0秒` |

### 统计数据

| 变量名 | 描述 | 示例返回值 |
|--------|------|-----------|
| `%bilibiliVideoPro_total_videos%` | 总互动视频数 | `156` |
| `%bilibiliVideoPro_total_likes%` | 总点赞数 | `298` |
| `%bilibiliVideoPro_total_coins%` | 总投币数 | `89` |
| `%bilibiliVideoPro_total_favorites%` | 总收藏数 | `134` |
| `%bilibiliVideoPro_triple_count%` | 三连完成数 | `67` |
| `%bilibiliVideoPro_triple_rate%` | 三连完成率 | `42.9%` |
| `%bilibiliVideoPro_avg_triple_daily%` | 平均每日三连 | `2.15` |
| `%bilibiliVideoPro_total_rewards%` | 总奖励次数 | `23` |

### 状态信息

| 变量名 | 描述 | 示例返回值 |
|--------|------|-----------|
| `%bilibiliVideoPro_online%` | 在线状态 | `在线` / `离线` |
| `%bilibiliVideoPro_status%` | 账户状态摘要 | `已绑定(大会员)` / `未绑定` / `已绑定(离线)` |

## 使用示例

### 在Chat插件中使用
```yaml
# EssentialsX Chat配置示例
format: '&7[&6{DISPLAYNAME}&7] &r{MESSAGE} &8[B站: %bilibiliVideoPro_username%]'
```

### 在Tab插件中使用
```yaml
# TAB插件配置示例
tablist-name: "&a{player} &7[&bLv.%bilibiliVideoPro_level%&7]"
```

### 在计分板插件中使用
```yaml
# Scoreboard插件配置示例
lines:
  - "&6&lBilibili统计"
  - "&7用户名: &f%bilibiliVideoPro_username%"
  - "&7等级: &f%bilibiliVideoPro_level%"
  - "&7VIP: &f%bilibiliVideoPro_vip%"
  - "&7总视频: &f%bilibiliVideoPro_total_videos%"
  - "&7三连率: &f%bilibiliVideoPro_triple_rate%"
```

### 在称号插件中使用
```yaml
# DeluxeMenus配置示例
display_name: "&6B站达人 &7[%bilibiliVideoPro_username%]"
requirements:
  requirement_1:
    type: "placeholder"
    placeholder: "%bilibiliVideoPro_bound%"
    value: "true"
```

## 注意事项

1. **依赖要求**: 需要安装 PlaceholderAPI 插件才能使用这些变量
2. **在线要求**: 大部分变量需要玩家在线才能正确显示
3. **绑定要求**: 账户相关变量需要玩家先绑定Bilibili账户
4. **数据实时性**: 统计数据可能有短暂延迟，通常在操作后几秒内更新
5. **错误处理**: 当数据不可用时，变量会返回默认值（如"未绑定"、"0"等）

## 故障排除

### 变量不显示或显示为原始文本
1. 确认已安装 PlaceholderAPI 插件
2. 确认 BilibiliVideoPro 插件正常启动
3. 使用 `/papi parse <玩家名> %bilibiliVideoPro_bound%` 测试变量

### 数据显示不正确
1. 确认玩家已绑定Bilibili账户
2. 检查玩家是否在线
3. 查看插件控制台是否有错误信息

### 性能考虑
- 这些变量会查询数据库和缓存，建议合理使用，避免在高频更新的地方过度使用
- 统计类变量（如总视频数、三连率等）查询相对较重，建议在静态显示场景使用