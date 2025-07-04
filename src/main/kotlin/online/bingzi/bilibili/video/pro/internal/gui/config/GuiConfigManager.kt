package online.bingzi.bilibili.video.pro.internal.gui.config

import org.bukkit.Material
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import java.io.File

/**
 * GUI配置管理器
 * 负责加载和管理分离的GUI配置文件
 */
object GuiConfigManager {
    
    @Config("gui/config.yml")
    lateinit var globalConfig: Configuration
    
    @Config("gui/themes.yml")
    lateinit var themesConfig: Configuration
    
    @Config("gui/effects.yml")
    lateinit var effectsConfig: Configuration
    
    // GUI配置缓存
    private val guiConfigs = mutableMapOf<String, Configuration>()
    
    /**
     * GUI布局配置数据类
     */
    data class GuiLayout(
        val title: String,
        val size: Int,
        val layout: List<String>,
        val items: Map<String, GuiItem>
    )
    
    /**
     * GUI物品配置数据类
     */
    data class GuiItem(
        val type: String,
        val material: Material,
        val name: String,
        val lore: List<String> = emptyList(),
        val permission: String? = null,
        val action: String? = null,
        val amount: Int = 1,
        val customModelData: Int? = null,
        val enchanted: Boolean = false
    )
    
    /**
     * 主题配置数据类
     */
    data class GuiTheme(
        val name: String,
        val description: String,
        val borderMaterial: Material,
        val accentColor: String,
        val textColor: String,
        val successColor: String,
        val warningColor: String,
        val errorColor: String,
        val infoColor: String,
        val effectsEnabled: Map<String, Boolean>
    )
    
    /**
     * 声音配置数据类
     */
    data class SoundConfig(
        val enabled: Boolean,
        val volume: Float,
        val pitch: Float,
        val effects: Map<String, String>,
        val actions: Map<String, String>
    )
    
    /**
     * 获取指定GUI的配置
     */
    private fun getGuiConfig(guiName: String): Configuration? {
        return guiConfigs.getOrPut(guiName) {
            try {
                val fileName = globalConfig.getString("files.$guiName") ?: return null
                Configuration.loadFromFile(File("plugins/BilibiliVideoPro/$fileName"))
            } catch (e: Exception) {
                println("Failed to load GUI config for $guiName: ${e.message}")
                return null
            }
        }
    }
    
    /**
     * 获取指定GUI的布局配置
     */
    fun getGuiLayout(guiName: String): GuiLayout? {
        val config = getGuiConfig(guiName) ?: return null
        
        val title = config.getString("title", "")?.replace("&", "§") ?: ""
        val size = config.getInt("size", 3)
        val layout = config.getStringList("layout")
        
        val items = mutableMapOf<String, GuiItem>()
        val itemsSection = config.getConfigurationSection("items")
        
        itemsSection?.getKeys(false)?.forEach { key ->
            val itemSection = itemsSection.getConfigurationSection(key) ?: return@forEach
            
            val type = itemSection.getString("type", "")!!
            val materialName = itemSection.getString("material", "STONE")!!
            val material = try {
                Material.valueOf(materialName)
            } catch (e: Exception) {
                Material.STONE
            }
            
            val name = itemSection.getString("name", "")?.replace("&", "§") ?: ""
            val lore = itemSection.getStringList("lore").map { it.replace("&", "§") }
            val permission = itemSection.getString("permission")
            val action = itemSection.getString("action")
            val amount = itemSection.getInt("amount", 1)
            val customModelData = if (itemSection.contains("custom_model_data")) {
                itemSection.getInt("custom_model_data")
            } else null
            val enchanted = itemSection.getBoolean("enchanted", false)
            
            items[key] = GuiItem(
                type = type,
                material = material,
                name = name,
                lore = lore,
                permission = permission,
                action = action,
                amount = amount,
                customModelData = customModelData,
                enchanted = enchanted
            )
        }
        
        return GuiLayout(title, size, layout, items)
    }
    
    /**
     * 获取当前主题配置
     */
    fun getCurrentTheme(): GuiTheme {
        val themeName = globalConfig.getString("current.theme", "default")!!
        return getTheme(themeName) ?: getTheme("default")!!
    }
    
    /**
     * 获取指定主题配置
     */
    fun getTheme(themeName: String): GuiTheme? {
        val themeSection = themesConfig.getConfigurationSection(themeName) ?: return null
        
        val name = themeSection.getString("name", themeName)!!
        val description = themeSection.getString("description", "")!!
        
        val colorsSection = themeSection.getConfigurationSection("colors") ?: return null
        val borderMaterialName = colorsSection.getString("border_material", "GRAY_STAINED_GLASS_PANE")!!
        val borderMaterial = try {
            Material.valueOf(borderMaterialName)
        } catch (e: Exception) {
            Material.GRAY_STAINED_GLASS_PANE
        }
        
        val effectsSection = themeSection.getConfigurationSection("effects")
        val effectsEnabled = mutableMapOf<String, Boolean>()
        effectsSection?.getKeys(false)?.forEach { key ->
            effectsEnabled[key] = effectsSection.getBoolean(key, false)
        }
        
        return GuiTheme(
            name = name,
            description = description,
            borderMaterial = borderMaterial,
            accentColor = colorsSection.getString("accent_color", "&6")!!,
            textColor = colorsSection.getString("text_color", "&f")!!,
            successColor = colorsSection.getString("success_color", "&a")!!,
            warningColor = colorsSection.getString("warning_color", "&e")!!,
            errorColor = colorsSection.getString("error_color", "&c")!!,
            infoColor = colorsSection.getString("info_color", "&b")!!,
            effectsEnabled = effectsEnabled
        )
    }
    
    /**
     * 获取声音配置
     */
    fun getSoundConfig(): SoundConfig {
        val soundSection = effectsConfig.getConfigurationSection("sounds")!!
        
        val effects = mutableMapOf<String, String>()
        soundSection.getConfigurationSection("effects")?.getKeys(false)?.forEach { key ->
            effects[key] = soundSection.getString("effects.$key", "")!!
        }
        
        val actions = mutableMapOf<String, String>()
        soundSection.getConfigurationSection("actions")?.getKeys(false)?.forEach { key ->
            actions[key] = soundSection.getString("actions.$key", "")!!
        }
        
        return SoundConfig(
            enabled = soundSection.getBoolean("enabled", true),
            volume = soundSection.getDouble("volume", 1.0).toFloat(),
            pitch = soundSection.getDouble("pitch", 1.0).toFloat(),
            effects = effects,
            actions = actions
        )
    }
    
    /**
     * 获取所有可用主题
     */
    fun getAvailableThemes(): List<String> {
        return themesConfig.getKeys(false).filter { key ->
            !key.startsWith("holiday") && !key.startsWith("custom")
        }
    }
    
    /**
     * 获取节日主题
     */
    fun getHolidayThemes(): List<String> {
        val holidaySection = themesConfig.getConfigurationSection("holiday")
        return holidaySection?.getKeys(false)?.toList() ?: emptyList()
    }
    
    /**
     * 切换主题
     */
    fun switchTheme(themeName: String): Boolean {
        if (!themesConfig.contains(themeName)) {
            return false
        }
        
        globalConfig.set("current.theme", themeName)
        globalConfig.saveToFile()
        return true
    }
    
    /**
     * 检查动画是否启用
     */
    fun isAnimationEnabled(): Boolean {
        return effectsConfig.getBoolean("animations.enabled", true)
    }
    
    /**
     * 获取动画更新间隔
     */
    fun getAnimationInterval(): Long {
        return effectsConfig.getLong("animations.update_interval", 20)
    }
    
    /**
     * 获取启用的动画效果列表
     */
    fun getEnabledEffects(): List<String> {
        return effectsConfig.getStringList("animations.effects")
    }
    
    /**
     * 应用主题颜色到文本
     */
    fun applyThemeColors(text: String, theme: GuiTheme = getCurrentTheme()): String {
        return text
            .replace("{accent}", theme.accentColor)
            .replace("{text}", theme.textColor)
            .replace("{success}", theme.successColor)
            .replace("{warning}", theme.warningColor)
            .replace("{error}", theme.errorColor)
            .replace("{info}", theme.infoColor)
            .replace("&", "§")
    }
    
    /**
     * 重载所有配置
     */
    fun reload() {
        globalConfig.reload()
        themesConfig.reload()
        effectsConfig.reload()
        
        // 清空GUI配置缓存，强制重新加载
        guiConfigs.clear()
    }
    
    /**
     * 验证配置文件完整性
     */
    fun validateConfig(): List<String> {
        val errors = mutableListOf<String>()
        
        // 检查全局配置
        if (!globalConfig.contains("current.theme")) {
            errors.add("Missing current theme configuration")
        }
        
        // 检查GUI文件配置
        val requiredGuis = listOf("main_menu", "video_list", "player_stats", "admin_panel")
        for (guiName in requiredGuis) {
            if (!globalConfig.contains("files.$guiName")) {
                errors.add("Missing GUI file configuration for: $guiName")
            } else {
                // 检查GUI文件是否存在
                val config = getGuiConfig(guiName)
                if (config == null) {
                    errors.add("GUI configuration file not found or invalid: $guiName")
                }
            }
        }
        
        // 检查主题配置
        val currentTheme = globalConfig.getString("current.theme", "default")!!
        if (!themesConfig.contains(currentTheme)) {
            errors.add("Current theme '$currentTheme' not found in themes configuration")
        }
        
        // 检查声音配置
        if (!effectsConfig.contains("sounds.enabled")) {
            errors.add("Missing sounds configuration")
        }
        
        return errors
    }
    
    /**
     * 获取配置统计信息
     */
    fun getConfigStats(): Map<String, Any> {
        return mapOf(
            "loaded_guis" to guiConfigs.keys.size,
            "available_themes" to getAvailableThemes().size,
            "holiday_themes" to getHolidayThemes().size,
            "current_theme" to getCurrentTheme().name,
            "animations_enabled" to isAnimationEnabled(),
            "sounds_enabled" to getSoundConfig().enabled
        )
    }
}