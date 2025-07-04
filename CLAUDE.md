# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

BilibiliVideoPro is a modular Bilibili interaction plugin/library for Minecraft built with TabooLib. It provides comprehensive integration with Bilibili's API features including user authentication, video interactions, and data persistence.

## Build and Development Commands

### Building the Project
```bash
./gradlew build
```

### Running Tests
```bash
./gradlew test
```

### Clean Build
```bash
./gradlew clean build
```

### Development Mode (with auto-reload)
```bash
./gradlew build --continuous
```

## High-Level Architecture

### Core Package Structure
All core functionality is located under `online.bingzi.bilibili.video.pro.internal`:

- **`command/`** - Command handlers using TabooLib CommandHelper
- **`database/`** - Data persistence layer with ORMLite and HikariCP
- **`network/`** - Bilibili API integration with OkHttp3
- **`helper/`** - Utility classes (QR code generation, Minecraft integration)
- **`manager/`** - Plugin lifecycle management
- **`cache/`** - Cache management and cleanup (CacheCleanupManager)
- **`security/`** - Security filters and data protection (LogSecurityFilter)

### Key Components

#### Database Layer
- **DatabaseManager**: Centralized database connection and lifecycle management
- **IDatabaseProvider**: Abstraction supporting SQLite and MySQL
- **Service Classes**: Business logic layer (`PlayerBilibiliService`, `VideoInteractionService`)
- **Entities**: Data models with ORMLite annotations

#### Network Layer
- **BilibiliNetworkManager**: Singleton network service manager
- **BilibiliApiClient**: HTTP client with cookie management
- **QRCodeLoginService**: Bilibili QR code authentication
- **VideoInteractionService**: Video interaction APIs (like, coin, favorite, follow)

#### Plugin Integration
- **PluginManager**: Handles TabooLib lifecycle events (`@Awake` annotations)
- **MapItemHelper**: Renders QR codes to Minecraft map items
- **NMSHelper**: ProtocolLib integration for packet handling

#### Cache Management Layer
- **CacheCleanupManager**: Automated cache cleanup and memory management
- **Player Cooldowns**: Manages command cooldowns with automatic expiration
- **Video Cooldowns**: Tracks per-video interaction limits with cleanup
- **Login Sessions**: Manages QR code login sessions with 10-minute expiration
- **Statistics**: Provides cache usage monitoring and memory estimation

#### Security Layer
- **LogSecurityFilter**: Filters sensitive information from logs and stack traces
- **Environment Filtering**: Different security levels for production vs development
- **Pattern Matching**: Regex-based detection of cookies, passwords, tokens, IPs, UUIDs
- **Safe Error Messages**: Converts technical errors to user-friendly messages

### Technology Stack

- **Framework**: TabooLib 6.2.3 for Minecraft plugin development
- **Language**: Kotlin 1.8.22 with JVM target 1.8
- **Database**: ORMLite ORM with HikariCP connection pooling
- **HTTP Client**: OkHttp3 4.12.0 with custom cookie management
- **QR Code**: Google ZXing 3.5.2
- **Packet Handling**: ProtocolLib 5.3.0

## Configuration

### Database Configuration
Edit `src/main/resources/database.yml` to configure:
- Database type (SQLite or MySQL)
- Connection parameters
- Connection pool settings
- Table prefix and auto-creation options

### Plugin Configuration
- Main config: `src/main/resources/config.yml` (currently empty)
- Localization: `src/main/resources/lang/zh_CN.yml`

## Development Guidelines

### Command Development
Commands use TabooLib's modern command framework:
- Use `@CommandHeader` for main command definition
- Use `@CommandBody` for subcommand methods
- Support both `Player` and `CommandSender` execution contexts
- Use `submit(async = true/false)` for proper thread handling

### Database Operations
- All database operations should use the service layer
- Use `@DatabaseTable` and `@DatabaseField` annotations for entities
- DatabaseManager handles connection pooling automatically
- Always check `DatabaseManager.initialize()` success before operations

### Network Operations
- Use `BilibiliNetworkManager.getInstance()` for all API calls
- All network operations are asynchronous by default
- Cookie management is handled automatically by `BilibiliCookieJar`
- Follow Bilibili API rate limiting guidelines

### Plugin Lifecycle
- Initialization: `PluginManager.initialize()` called on `LifeCycle.ENABLE`
- Health checks: Executed on `LifeCycle.ACTIVE`
- Cleanup: `PluginManager.cleanup()` called on `LifeCycle.DISABLE`

### Cache Management
- Use `CacheCleanupManager` for all caching operations
- Automatic cleanup runs every 5 minutes
- Player cooldowns expire after 10 minutes
- Video cooldowns expire after 1 hour
- Login sessions expire after 10 minutes
- Cache statistics available via `getCacheStatistics()`

### Security Guidelines
- All sensitive log output must use `LogSecurityFilter.filterSensitiveInfo()`
- Use `LogSecurityFilter.filterForEnvironment()` for environment-specific filtering
- Convert errors with `LogSecurityFilter.getSafeErrorMessage()` before user display
- Stack traces filtered with `LogSecurityFilter.filterStackTrace()`
- Production environment enforces strict filtering policies

## Common Development Patterns

### Async Operations
```kotlin
submit(async = true) {
    // Background work
    submit(async = false) {
        // Main thread updates
    }
}
```

### Database Service Usage
```kotlin
val binding = PlayerBilibiliService.findByPlayerUuid(playerUuid)
```

### Network Manager Usage
```kotlin
val networkManager = BilibiliNetworkManager.getInstance()
networkManager.initialize()
```

### Cache Usage
```kotlin
// Set player cooldown
CacheCleanupManager.setPlayerCooldown(playerUuid, 30)

// Check cooldown status
if (CacheCleanupManager.isPlayerOnCooldown(playerUuid)) {
    val remaining = CacheCleanupManager.getPlayerCooldownRemaining(playerUuid)
    // Handle cooldown
}

// Manage login sessions
CacheCleanupManager.setLoginSession(playerUuid, qrcodeKey)
val session = CacheCleanupManager.getLoginSession(playerUuid)
```

### Security Filtering
```kotlin
// Filter sensitive information from logs
val safeMessage = LogSecurityFilter.filterSensitiveInfo(originalMessage)

// Environment-specific filtering
val filteredMessage = LogSecurityFilter.filterForEnvironment(message, isProduction)

// Safe error messages for users
val userMessage = LogSecurityFilter.getSafeErrorMessage(exception.message)
```

## Important Notes

- **Thread Safety**: Database and network operations are designed to be thread-safe
- **Resource Management**: Always use the manager classes for proper resource cleanup
- **Error Handling**: Network and database operations include comprehensive error handling
- **Cookie Security**: Cookie data is sensitive and automatically managed
- **API Limitations**: Bilibili API has rate limiting - implement appropriate delays
- **Cache Management**: Automatic cleanup prevents memory leaks, all cache operations are thread-safe
- **Security Filtering**: All sensitive data is filtered before logging to prevent information leakage
- **Environment Awareness**: Different security policies for production vs development environments

## Documentation References

- Network usage: `docs/NETWORK_USAGE.md`
- Database usage: `docs/DATABASE_USAGE.md`
- Helper utilities: `docs/HELPER_USAGE.md`
- Implementation details: `docs/IMPLEMENTATION_SUMMARY.md`