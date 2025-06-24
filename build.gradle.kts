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
        install(BukkitHook)
        install(CommandHelper)
        install(I18n)
        install(Metrics)
        install(MinecraftChat)
    }
    description {
        name = "BilibiliVideoPro"
        contributors {
            name("BingZi-233")
        }
        links {
            name("https://www.bingzi.online")
        }
    }
    version { taboolib = "6.2.3-20d868d" }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms.core:v12004:12004:mapped")
    compileOnly("ink.ptms.core:v12004:12004:universal")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))

    // HTTP客户端 - OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JSON处理 - Gson (轻量级，与OkHttp配合好)
    implementation("com.google.code.gson:gson:2.10.1")

    // 可选：OkHttp日志拦截器 (用于调试)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ORM数据库持久化 - ORMLite
    implementation("com.j256.ormlite:ormlite-core:6.1")
    implementation("com.j256.ormlite:ormlite-jdbc:6.1")

    // 数据库驱动
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")    // SQLite驱动
    implementation("mysql:mysql-connector-java:8.0.33")  // MySQL驱动

    // 连接池
    implementation("com.zaxxer:HikariCP:5.1.0")
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
