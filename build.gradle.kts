import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    java
    id("io.izzel.taboolib") version "2.0.22"
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
}

taboolib {
    env {
        install(Basic)
        install(Bukkit)
        install(BukkitUtil)
        install(BukkitHook)
        install(CommandHelper)
        install(I18n)
        install(Metrics)
        install(MinecraftChat)
        install(BukkitNMS)
        install(BukkitNMSUtil)
        install(BukkitNMSItemTag)
        install(CommandHelper)
        install(Kether)
    }
    
    // 依赖重定向配置 - 将第三方库重定向到项目专用包名下
    relocate("com.squareup.okhttp3", "online.bingzi.bilibili.video.pro.libs.okhttp3")
    relocate("com.squareup.okio", "online.bingzi.bilibili.video.pro.libs.okio")
    relocate("com.google.code.gson", "online.bingzi.bilibili.video.pro.libs.gson")
    relocate("com.j256.ormlite", "online.bingzi.bilibili.video.pro.libs.ormlite")
    relocate("com.zaxxer.hikari", "online.bingzi.bilibili.video.pro.libs.hikari")
    relocate("com.google.zxing", "online.bingzi.bilibili.video.pro.libs.zxing")
    relocate("com.mysql", "online.bingzi.bilibili.video.pro.libs.mysql")
    relocate("xyz.xenondevs.invui", "online.bingzi.bilibili.video.pro.libs.invui")

    description {
        name = "BilibiliVideoPro"
        contributors {
            name("BingZi-233")
        }
        links {
            name("https://www.bingzi.online")
        }
        dependencies {
            name("PlaceholderAPI").optional(true)
            name("ProtocolLib")
        }
    }
    version { taboolib = "6.2.3-20d868d" }
}

repositories {
    mavenCentral()
    maven("https://repo.dmulloy2.net/repository/public/") // ProtocolLib repository
    maven("https://repo.xenondevs.xyz/releases")
}

dependencies {
    compileOnly("ink.ptms.core:v12004:12004:mapped")
    compileOnly("ink.ptms.core:v12004:12004:universal")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))

    // HTTP客户端 - OkHttp
    taboo("com.squareup.okhttp3:okhttp:4.12.0")

    // JSON处理 - Gson (轻量级，与OkHttp配合好)
    taboo("com.google.code.gson:gson:2.10.1")

    // 可选：OkHttp日志拦截器 (用于调试)
    taboo("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ORM数据库持久化 - ORMLite
    taboo("com.j256.ormlite:ormlite-core:6.1")
    taboo("com.j256.ormlite:ormlite-jdbc:6.1")

    // 数据库驱动
    taboo("org.xerial:sqlite-jdbc:3.44.1.0")    // SQLite驱动
    taboo("mysql:mysql-connector-java:8.0.33")  // MySQL驱动

    // 连接池
    taboo("com.zaxxer:HikariCP:5.1.0")

    // 二维码生成库
    taboo("com.google.zxing:core:3.5.2")

    // ProtocolLib for packet handling
    implementation("com.comphenix.protocol:ProtocolLib:5.3.0")
    
    // InvUI - GUI库
    taboo("xyz.xenondevs.invui:invui:1.38")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
